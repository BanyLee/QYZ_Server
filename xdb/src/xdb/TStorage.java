package xdb;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.goldhuman.Common.Marshal.MarshalException;
import com.goldhuman.Common.Marshal.OctetsStream;

/**
 * ���ͻ��洢�ӿ�,Ӧ�ñ�
 *
 */
final class TStorage<K, V> extends Storage {
	//////////////////////////////////////////////////////////////////
	// management
	/**
	 * ���Ժ󣬲��ܸ������á�
	 */
	TStorage(Logger logger, XdbConf xconf, TableConf tconf) {
		super(logger, xconf, tconf);
	}

	////////////////////////////////////////////////////////
	// �����Ż��� �ο��ɰ棺 SVN revision 1512
	//
	// ���ݣ� changed(C) marshal(M) snapshot(S) �Ĳ�����
	// �̣߳� Cleaner, Checkpoint, Procedure(����߳�)
	//
	// -------------------------------------------------------------------
	// ��������              ����  �߳�          ��(NEW��ʾ������Ļ�ȡ��������)
	// -------------------------------------------------------------------
	// onRecordChange     CM   Procedure   flushReadLock + ��¼��
	// isChangedOrUnknown CM   Cleaner     flushReadLock(NEW) + TTableCache.lru
	// marshalN           CM   Checkpoint  ��¼�� + ����C������M���ҶԵ�ǰC��ϵ�л���¼��
	// snapshot marshal0  CMS  Checkpoint  flushWriteLock Ψ�Ҷ���
	// flush              S    Checkpoint  snapshotClearWriteLock(NEW) : ֻ�������˲����Ҫ�������
	// find + exist       S    Procedure   snapshotClearReadLock(NEW) + ��¼��
	// -------------------------------------------------------------------
	// ����
	// 1 ConcurrentMap ��������ֻ����һ���߳���ִ�У����Ժ�������������������CMS�ķ��ʶ�����Ҫ����������
	// 2 snapshotClearReadLock ���������˲�䣬snapshot �ķ������������ġ�

	private ConcurrentMap<K, TRecord<K, V>> changed  = xdb.util.Misc.newConcurrentMap();
	private ConcurrentMap<K, TRecord<K, V>> marshal  = xdb.util.Misc.newConcurrentMap();
	private ConcurrentMap<K, TRecord<K, V>> snapshot = xdb.util.Misc.newConcurrentMap();
	private final ReentrantReadWriteLock snapshotClearLock = new ReentrantReadWriteLock(); 

	// from TTable
	void onRecordChange(TRecord<K, V> r) {
		// �����ڵ��ã��ѻ����: flushReadLock + r�ļ�¼����
		// ʹ�� ConcurrentMap �Ĳ����еõ����ߵĲ�����
		if (r.getState() == TRecord.State.REMOVE) {
			changed.remove(r.getKey());
			marshal.remove(r.getKey());
		} else {
			changed.put(r.getKey(), r);
		}
	}

	// from TTableCache.Cleaner
	boolean isChangedOrUnknown(K key) {
		// �ѻ����: flushReadLock
		// ���뱣֤ dirty �ļ�¼���ܷ��� false�� ����Ѹɾ��ļ�¼��������ģ������������Ϊ�� Unknown��
		return changed.containsKey(key) || marshal.containsKey(key);
	}

	// ֻ�� Checkpoint �̻߳��޸���Щֵ�� JMX��Ҫ��ȡ���ø� volatile ��ʾһ�¡�
	private volatile long countMarshalN = 0;
	private volatile long countMarshal0 = 0;
	private volatile long countFlush = 0;
	private volatile long countSnapshot = 0;
	private volatile long countMarshalNTryFail = 0;
	// package private . �� TRecord.flush ����ʡ�
	volatile long flushKeySize = 0;
	volatile long flushValueSize = 0;

	/**
	 * ����ϵ�л���
	 */
	@Override
	int marshalN() {
		// ʲô����û�õ���

		/*
		 * <Ҳ���ȿ��� Checkpoint.java ��ͷ��˵����TStorage.isChangedOrUnknown�е�ע�ͱȽϺá�>
		 *
		 * REMOVE �Ż�
		 *     REMOVE ״̬���������ڱ���֮ǰɾ�����ļ�¼״̬Ϊ REMOVE��
		 *     ԭ�뷨�ǣ����е��޸Ĳ��������ڱ���ʱ����Ҳ����˵REMOVE״̬��¼��snapshotʱ�Ż��cache�������
		 *     Ϊ�˼ӿ��ڴ��ռ������ּ�¼�������ύʱ�ʹ�cache�������
		 *
		 * marshalN
		 *     �� changed �еļ�¼����ϵ�л�����Ȼ���Ƶ�marshal�С�α�������£�
		 *     for ( TRecord<K, V> r : changed.values() ) r.marshalN();
		 *     marshal.putAll(changed);
		 *     changed.clear();
		 *     ���� marshalN ��˵������������پͿ����ˡ�����Ҫ��ԭ�ӵ�������ϲ�����
		 *     ���� ConcurrentMap ����Ĳ����ԣ��Լ���������û���������������ġ�����г�������
		 *     ������ marshalN ������������
		 *
		 * ����г���
		 *     ������ʲô����г�Ľ���뿴��������е�ע�͡�
		 */

		int marshaled = 0;
		int tryFail = 0;

		// ���ﲻ��ʹ��putAll������ʹ�� iterator������Ϊû�����������ò��� putAll ��ȥ�ļ�¼����
		for (Iterator<TRecord<K, V>> it = changed.values().iterator(); it.hasNext(); /* none */) {
			/*
			 * ����ʱ���ܳ��� onRecordChange ���á�
			 * ������ put ʱ
			 *    a) ����� it ���棬��ô�Ժ�Ϳ��Դ�������û���⣻
			 *    b) ����ǵ�ǰ it �����Ͼʹ���������һ��㲻�ã����£�
			 *       ������Ҫȷ��һ�£���ǰ it.next() ���ص���putǰ���û������µ����á�
			 *       ���ڶ���ĳ��key��˵������Ӧ�ļ�¼�����Դ����𣬾���flush֮ǰ������䡣
			 *       ToDo ���ﲻ�ܷ����¾ɣ��߼��϶�����ȷ�ġ��Ժ���ȷ��������⡣
			 *    c) ����� it ֮ǰ����ô��һ�ξͲ������ˣ���һ�� marshalN �ٴ���û���⣻
			 *
			 * ������ remove ʱ
			 *    a) ����� it ���棬�Ժ󿴲�������û���⡣
			 *    b) ����ǵ�ǰ it ����õ��ɵļ�¼��Ȼ��ᱻ�������뵽marshal�С�������г���֮һ����
			 *       �����¼ֻ�ܵ� snapshot ��������ˡ�REMOVE ��ǰɾ���Ż�ʧЧ��
			 *       ��������˴˳�ͻ����ʾ��¼����ʹ�ã������� tryMarshalN һ���ʧ�ܣ�����������¼
			 *       �ӵ�(put)��marshal�У�ʵ���Ͻ��������ֳ�ͻ�����ĸ��ʡ�
			 *    c) ����� it ֮ǰ��������һ��ϵ�л��������ѣ�û���⡣marshalN�����ͻ������
			 */
			TRecord<K, V> r = it.next();
			if (r.tryMarshalN()) { // ����ʹ�������еļ�¼�������ʧ�ܣ������� changed �У�û���⡣
				/*
				 * <������ put �� remove>
				 *     �����¼����������Map�У����������¼���������κ�һ��Map�С�see isChangedOrUnknown��
				 */
				marshal.put(r.getKey(), r);
				/*
				 * ������г���֮����
				 * ��������ʱ�䴰�ڷ����� remove����ôҲ�����������¼��ֻ�ܵ� snapshot ������
				 */
				it.remove();
				++ marshaled;
			} else 
				++ tryFail;
		}
		this.countMarshalN += marshaled;
		this.countMarshalNTryFail += tryFail;
		return marshaled;
	}

	/**
	 * ϵ�л���snapshot��
	 */
	@Override
	int marshal0() {
		// snapshot ���衣 �ѻ�� flushWriteLock���ܰ�ȫ������á�
		marshal.putAll(changed);
		for (TRecord<K, V> r : changed.values())
			r.marshal0();
		final int size = changed.size();
		changed.clear();
		this.countMarshal0 += size;
		return size;
	}

	/**
	 * �����Ա�֤�㣬�����е�Storage�Ŀ��ս������ʱ��һ��XDB.checkpoint������ˡ���
	 */
	@Override
	int snapshot() {
		// flushWriteLock�� ˳��㡣
		final ConcurrentMap<K, TRecord<K, V>> tmp = this.snapshot;
		this.snapshot = this.marshal;
		this.marshal = tmp;
		for (TRecord<K, V> r : this.snapshot.values())
			r.snapshot();
		this.countSnapshot += snapshot.size();
		return snapshot.size();
	}

	@Override
	int flush() {
		// ��ʱ��ʲô����û���õ���

		// flush ��¼������ snapshot �ļ�¼�������ﲻͳ�� REMOVE ״̬�ļ�¼��
		// ������REMOVE״̬��¼���� = snapshotCount - flushCount��
		// ���������� marshalN ������������������
		int count = 0;
		for (TRecord<K, V> r : this.snapshot.values()) {
			// flush. �� snapshot ��������˵����ʱ��ֻ���ģ�Ҳ��������ȥ�޸ģ�����Ҫ������
			if (r.flush(this))
				++ count;
		}

		// clear. ȫ����д�� Storage �У���ʱ���԰�ȫ�������
		Map<K, TRecord<K, V>> tmp = null;
		// ����������þ��Ƕ� snapshot �������ṩ�������ܲ��ң������޷���volatile.
		// �� snapshot ȡ�������� clear�������ÿգ�ʹ�� Procedure ����ͬ�����ʡ�
		final Lock writeLock = snapshotClearLock.writeLock();
		writeLock.lock(); 
		try {
			tmp = this.snapshot;
			this.snapshot = xdb.util.Misc.newConcurrentMap();
		} finally {
			writeLock.unlock();
		}
		// ��������е�Procedure�Ѿ��ò�����Ҫ�����snapshot�ˡ����԰�ȫ�Ĳ������������¼��
		for (TRecord<K, V> r : tmp.values())
			r.clear();
		this.countFlush += count;
		return count;
	}

	
	/**
	 * �ڼ�¼����ʹ�á�
	 */
	final boolean exist(K key, TTable<K, V> table) {
		// �����ڣ��ѻ������ flushReadLock + ��¼����
		final Lock readLock = snapshotClearLock.readLock();
		readLock.lock(); // ����洢���̲������� snapshot����������� flush �� clear ���⡣
		try {
			TRecord<K, V> r = this.snapshot.get(key);
			if (null != r)
				return r.exist();
		} finally {
			readLock.unlock();
		}
		return super.exist(table.marshalKey(key));
	}

	/**
	 * �ڼ�¼����ʹ�á�
	 */
	V find(K key, TTable<K, V> table) {
		// �����ڣ��ѻ������ flushReadLock + ��¼����
		OctetsStream value = null;
		boolean foundInSnapshot = false;

		final Lock readLock = snapshotClearLock.readLock();
		readLock.lock(); // ����洢���̲������� snapshot����������� flush �� clear ���⡣
		try {
			TRecord<K, V> r = this.snapshot.get(key);
			if (null != r) {
				foundInSnapshot = true;
				// snapshot ������ֻ���ģ�clearֻ���ͷ����ã����Կ����ǲ��ɱ������
				// ����Ҫ������������������ unmarshalValue��
				value = r.find(); 
			}
		} finally {
			readLock.unlock();
		}

		try {
			if (foundInSnapshot) {
				if (null != value)
					return table.unmarshalValue(value);
				return null;
			}
			// find in storage
			value = super.find(table.marshalKey(key));
			if (null != value)
				return table.unmarshalValue(value);
			return null;
		} catch (MarshalException me) {
			throw new xio.MarshalError("unmarshal", me);
		}
	}

	public long getCountFlush() {
		return this.countFlush;
	}

	public long getCountMarshal0() {
		return this.countMarshal0;
	}

	public long getCountMarshalN() {
		return this.countMarshalN;
	}

	public long getCountMarshalNTryFail() {
		return this.countMarshalNTryFail;
	}

	public long getCountSnapshot() {
		return countSnapshot;
	}

	public long getFlushKeySize() {
		return flushKeySize;
	}

	public long getFlushValueSize() {
		return flushValueSize;
	}

	/*
	int flush() {
		//Trace.debug("flush=" + changed.values());
		changedLock.lock();
		try {
			for (TRecord<K, V> r : changed.values()) {
				switch (r.getState()) {
				case INDB_GET:
					replaceUnsafe(r.encodeKey(), r.encodeValue());
					break;
				case INDB_REMOVE:
					removeUnsafe(r.encodeKey());
					r.getTable().getCache().remove(r.getKey());
					break;
				case INDB_ADD:
					replaceUnsafe(r.encodeKey(), r.encodeValue());
					r.setState(TRecord.State.INDB_GET);
					break;
				case ADD:
					insertUnsafe(r.encodeKey(), r.encodeValue());
					r.setState(TRecord.State.INDB_GET);
					break; 
				case REMOVE:
					// REMOVE״̬,�����������ʱ���cache�б�ɾ��.�˴������ܳ���.�걸�����Ҳ��һ��ʵ�֡�
					Trace.warn("flush invalid record state: " + r);
					r.getTable().getCache().remove(r.getKey());
					break;
				}
			}
			int count = changed.size();
			changed.clear();
			return count;
		} finally {
			changedLock.unlock();
		}
	}
	*/
}
