package xdb.util;

import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * <b>�����̵߳������� ��������࣬��ͬ����������Բ���ִ�У�ͬһ�������񲻲���ִ�У�˳��ִ�С�</b>
 * <p>��ʵ����ɷ����Լ������ɷ����������Լ����̣߳������ⲿ��ִ����ִ������
 * 
 * <p><b>������Դ</b>
 * <p>
 * �ڿ�������Ӧ��ʱ��������������Э��˳������⡱���������ļ�����������Ҫ��֤Э���ִ��˳��
 * ����������Ⱥ��յ�����Э�� Open��Close���ڲ�����ִ��ʱ������ Close �ȱ�ִ�в���ɣ���ʱ
 * ����״̬�Ǵ򿪵ġ����ڿͻ��˷�������˵���õ������ս���Ͳ������������ġ���ʹ Close ִ��ʱ
 * ��Ҫ����Ҫ���жϣ��ᷢ�ֱ������ǹرյģ��������˴��󣬴�ʱ�ͻ����ܵõ�������ʾ�����Ѿ������޲��ˡ�
 * <p>
 * Э��˳������ı�����Э����ƵĲ��ã���ȷ�Ľ��������Э�������������Э�飬OpenResult, CloseResult��
 * �ͻ������Ƕ��ڵõ��������Ժ�ŷ����������󡣺���Ȼ��������Ȼ��û��Э��˳�������ˣ�������ȷ�Ľ��������
 * ������ȷ�Ľ��������ʱ����鷳����ĳЩ���⽻���£��磬ʱ����Ҫ��Ƚϸߣ���������£����Ѳ�������Э�����������
 * ���͵õ������ϵ������Ǿͱ�֤���յ���˳��ִ��Э��ɡ�
 * <p>
 * <b>���棺���������Э��ʱ���������Э��˳�����⣬��Ҫ�����������ִ��������֤˳��</b>
 * ���ִ������Ϊ�˼�ĳЩʵ�ں��ѽ����Э��˳�����⡱��Э�鸴�Ӷȣ���������Э����Ʋ����ö������ķ��ա�
 * ������Ϊһ�ֱ������ƣ����Ǻ���Ĺ�����ơ��������ִ��������һ����ϰ�ߡ�
 * 
 * <p><b>ʵ��˼·</b>
 * 
 * <p>ÿ������ʹ��һ�����У�Serial����ÿ���������ֻ�ɷ�һ�������ȥ���У�������ִ�����ʱ���ɷ���һ������
 * ��Ȼͬʱ���ڷ��������޵ģ������������������Ԥ֪�ģ������Ҫ���տյķ�����У��������տ��ܻ�ľ��ڴ档
 * �����ַ�ʽ��д��һ��ʵ��(SVN-revision 1619)��svn1619 ʵ��ʹ��һ�Ѵ������������ж����Լ����еĻ��գ�
 * �����ɹ���������Щ���� BUG û�д����������������������Ż��ռ䡣
 * 
 * <p>SVN-1619ʵ�ֵ��������ⲻ������ͻ���������л����У��ȴ����е�������������̫�࣬����ÿ��������У�
 * �����϶�ֻʹ��һ�ξͱ������ˣ����Ӳ���Ҫ���ڴ���������ȡ�Ͳ�����ķ������Ѷ�����ఴ�̶��Ĺ���ӳ�䵽��
 * ͬһ�����У�Serial���У�ÿ��������Ȼ���ֻ�ɷ�һ�������ȥ���С���Ȼ������Ȼ����ͬһ���಻�Ტ��ִ�У�
 * �����ڱ�ӳ���ͬһ�����еĲ�ͬ key Ҳ���Ტ���ˡ�����ӳ�䷽ʽ�Ժ󣬾Ϳ��԰Ѷ��е��������̶�������
 * ������Ҫ���ն��У�ֻҪ�ܵĶ����������㹻�Ĳ�������������Ӧ����˵Ӱ�첻��<code>������ж���ʹ��
 * ���鱣�棬�ܶ�������Ϊ length��ӳ��õ��Ķ����±�Ϊ�� key.hashCode() % length��</code> 
 * ���ַ�ʽ��Ҫ������µ����⣺
 * <ol>
 * <li>ӳ������ԡ�������е� key ����ӳ�䵽ͬһ�����У��������е����񶼱�˳��ִ�У�
 *     ��Ȼ���ֲ����ǲ��ܽ��ܵġ�Ϊ�˷�ֹ key.hashCode �����ã���ȡ�ٴ� hash �İ취��
 *     ������ hash �ķ�������һ���ܵõ��ض����������ŵĽ�����������Ͽ��á�
 *     see SerialKeyExecutor.hash
 * 
 * <li>�ܶ������������á�����Ƚϼ򵥣�һ��Ҫ�� service ���߳�������Ĭ��Ϊ 1024�������ˡ�
 *     ֻ���ڳ�ʼ��ʱ���ã�����ʱ�����޸ġ�
 * </ol>
 * 
 * <p><b>�����ԡ�</b>�ⲿ ExecutorService.shutdownNow ��ȡ����û�п�ʼ������
 *    SerialKeyExecutor ����������£��޷������������������Ǻ���ġ�SerialKeyExecutor
 *    �������ṩshutdown��������ȫ�������ⲿ service��
 * 
 * @see xdb.util.ProcedureOneByOne
 */
public final class SerialKeyExecutor<K> implements Executor {
	private final ExecutorService service;
	private final Serial[] serials;

	/**
	 * ����ͳ�ʼ��ϵ�л�ִ������
	 * ʹ�� Xdb �� ProcedureExecutor ����������ִ����.
	 * ����ϵ������Ϊ1024��
	 * @throws NullPointerException if Xdb is not start.
	 */
	public SerialKeyExecutor() {
		this(xdb.Xdb.executor());
	}

	/**
	 * ����ͳ�ʼ��ϵ�л�ִ������
	 * ����ϵ������Ϊ1024��
	 * @param service �������ⲿִ������
	 */
	public SerialKeyExecutor(ExecutorService service) {
		this(service, 1024);
	}

	/**
	 * ����ͳ�ʼ��ϵ�л�ִ������
	 * @param service �������ⲿִ������
	 * @param concurrencyLevel ����ϵ�С�
	 */
	@SuppressWarnings("unchecked")
	public SerialKeyExecutor(ExecutorService service, int concurrencyLevel) {
		if (concurrencyLevel < 0 || concurrencyLevel > 0x40000000)
			throw new IllegalArgumentException("Illegal concurrencyLevel: " + concurrencyLevel);
		if (null == service)
			throw new NullPointerException();
		this.service = service;
		int capacity = 1;
		while (capacity < concurrencyLevel)
			capacity <<= 1;
		this.serials = new SerialKeyExecutor.Serial[capacity];
		for (int i = 0; i < this.serials.length; ++i)
			this.serials[i] = new Serial();
	}

	/**
	 * @return �������ⲿִ������
	 */
	public ExecutorService getExecutorService() {
		return service;
	}

	////////////////////////////////////////////////
	// ��ӷ�������ķ������Ǳ�׼�ӿڣ�������ʽ���á�

	/**
	 * ������񵽶����С����ݲ���key���з��ࡣ
	 * @param key
	 * @param task
	 */
	public void execute(K key, Runnable task) {
		submit(key, task);
	}

	/**
	 * ������񵽶����С����ݲ���key���з��ࡣ
	 * @param key
	 * @param task
	 * @return
	 */
	public Future<?> submit(K key, Runnable task) {
		return submit(key, Executors.callable(task));
	}

	/**
	 * ������񵽶����С����ݲ���key���з��ࡣ
	 * 
	 * @param key
	 * @param task
	 * @return
	 */
	public <T> Future<T> submit(K key, Runnable task, T result) {
		return submit(key, Executors.callable(task, result));
	}

	/**
	 * ������񵽶����С����ݲ���key���з��ࡣ
	 * 
	 * @param <T>
	 * @param key
	 * @param callable
	 * @return
	 */
	public <T> Future<T> submit(K key, Callable<T> callable) {
		if (service.isShutdown())
			throw new RejectedExecutionException();
		return serialFor(key).submit(key, callable);
	}

	/**
	 * ����ִ������
	 */
	@Override
	public void execute(Runnable task) {
		service.execute(task);
	}

	/**
	 * ȡ��ָ�� key ���������񡣻᳢��ȡ������ִ�е����񣬵���ɾ������δ��ʼִ�е�����ȫ��ȡ����ɾ����
	 * <b>��Ҫ����ʵ�֣�Ч�ʲ��Ǻܸߡ�</b>
	 * @param key
	 * @return canceled number in this call.
	 */
	public int cancel(K key) {
		return serialFor(key).cancel(key);
	}

    /**
     * Because the states of tasks and threads
     * may change dynamically during computation, the returned value
     * is only an approximation, but one that does not ever decrease
     * across successive calls.
     *
     * @return the number of tasks
     */
	public int size() {
		int n = 0;
		for (Serial sq : serials)
			n += sq.size();
		return n;
	}

	/**
	 * cancel all
	 * @return the number of tasks that have canceled.
	 * @deprecated debug only
	 */
	public int purge() {
		if (false == service.isTerminated())
			throw new IllegalStateException("service is still running.");
		int n = 0;
		for (Serial sq : serials)
			n += sq.purge();
		return n;
	}

    /**
	 * Applies a supplemental hash function to a given hashCode, which defends
	 * against poor quality hash functions. This is critical because HashMap
	 * uses power-of-two length hash tables, that otherwise encounter collisions
	 * for hashCodes that do not differ in lower bits. Note: Null keys always
	 * map to hash 0, thus index 0.
	 * @see java.util.HashMap
	 */
	static int hash(int h) {
		// This function ensures that hashCodes that differ only by
		// constant multiples at each bit position have a bounded
		// number of collisions (approximately 8 at default load factor).
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}

	/**
	 * Returns index for hash code h.
	 */
	static int indexFor(int h, int length) {
		return h & (length - 1);
	}

	/**
	 * for debug or manage.
	 * <b>not well defined!</b>
	 * @deprecated public for debug only
	 */
	public Serial serialFor(K key) {
		return serials[getIndexForKey(key)];
	}

	
	/** add by cjc 20110906
	 *  Serial Segment ID. for debug and statitis
	 */
	public int getIndexForKey(K key)
	{
		return indexFor(hash(key.hashCode()), serials.length);
	}
	
	
	/**
	 * for debug or manage.
	 */
//	public Serial[] serials() {
//		return serials;
//	}

	/**
	 * ȡ������ɾ������
	 * @param future ����������ͨ�� submit �ύ����ʱ���صĽ����
	 */
    @SuppressWarnings("unchecked")
	public boolean remove(Future<?> future) {
		if (future instanceof SerialKeyExecutor<?>.Serial.Task<?>)
			return ((Serial.Task<?>)future).remove();

		throw new RuntimeException("SerialKeyExecutor.remove: future is not a serial task.");
	}

	private static Callable<Object> dummyHeader = new Callable<Object>() {
		public Object call() {
			throw new IllegalStateException("SerialKeyExecutor.header");
		}
	};

	public final class Serial {
		private final Task<?> header;
		private int size = 0;
		private final Lock lock = new ReentrantLock();

		Serial() {
			header = new Serial.Task<Object>(null, dummyHeader);
			reset();
		}

		// concurrent: user submit task.
		<T> Future<T> submit(K key, Callable<T> callable) {
			lock.lock();
			try {
				Serial.Task<T> newTask = addLast(new Serial.Task<T>(key, callable));
				if (size == 1) {
					try {
						SerialKeyExecutor.this.execute(newTask);
					} catch (RejectedExecutionException e) {
						remove(newTask); // roll back
						throw e;
					} catch (Throwable e) { // ȷ����ȷ�ԣ�Ӧ���ᷢ����
						remove(newTask); // roll back. 
						throw new RejectedExecutionException(e);
					}
				}
				return newTask;
			} finally {
				lock.unlock();
			}
		}

		// concurrent: serial task done. only One-Thread. lock in SerialTask.
		void scheduleNext(Serial.Task<?> done) {
			assert( done == getFirst() );
			this.remove(done);

			/**
			 * ɾ���Ѿ�ȡ��������
			 * �������ȡ���Ƚ�Ƶ�������Լ��ٲ���Ҫ���߳��л������Ч�ʡ�
			 * �����Ż���ȥ����һ������Ȼ����ȷ�ġ�
			 */
			while (this.size > 0 && getFirst().isCancelled()) removeFirst();
			// ������������ʱ�䴰�ڱ� cancel���൱��û����������ɾ�������������⡣

			/**
			 * ����ִ����һ������
			 */
			while (this.size > 0) {
				final Serial.Task<?> first = getFirst();
				try {
					SerialKeyExecutor.this.execute(first);
					break; // execute done
				} catch (Throwable e) {
					// һ������ִ�д������Ͻ����������񡣱������ʱ��ʹ��ͬһ���쳣���ã�Ӧ��û����ɡ�
					while (this.size > 0) removeFirst().setException(e);
					// ����Ĵ���ʽ���죬���� Task û�����������ڴ�����
					/*
					for (Task<?> task = header.next; task != header; task = task.next)
						task.setException(e); // �ظ�ʹ��ͬһ���쳣���ã�Ӧ��û����ɡ�
					reset();
					// */
				}
			}
		}

		// concurrent: user cancel task by key
		int cancel(final K key) {
			lock.lock();
			try {
				if (this.size == 0)
					return 0;

				int canceled = 0;
				// cancel scheduled-task but don't remove.
				if (header.next.cancel(false))
					++ canceled;
				// try cancel and remove others.
				Serial.Task<?> task = header.next.next; // skip over scheduled-task.
				while (task != header) {
					final Serial.Task<?> next = task.next;
					if (task.key.equals(key) && task.cancel(false)) {
						this.remove(task);
						++ canceled;
					}
					task = next;
				}
				return canceled;
			} finally {
				lock.unlock();
			}
		}

		// for debug.
		int purge() {
			lock.lock();
			try {
				int canceled = 0;
				for (Serial.Task<?> task = header.next; task != header; task = task.next) {
					if (task.cancel(false))
						++ canceled;
				}
				reset();
				return canceled;
			} finally {
				lock.unlock();
			}
		}

		public int size() {
			lock.lock();
			try {
				return size;
			} finally {
				lock.unlock();
			}
		}

		final class Task<T> extends FutureTask<T> {
			private Task<?> next = null;
			private Task<?> previous = null;
			private final K key;

			Task(K key, Callable<T> callable) {
				super(callable);
				this.key = key;
			}
	
			// concurrent: user cancel and remove the task.
			public boolean remove() {
				if (super.cancel(false)) {
					// future.cancel ��ֻ֤�ܽ�������һ�Ρ�
					lock.lock();
					try {
						/**
						 * ����ɾ���Ѿ����ɷ���ȥ���������������жϵĺ��壬���£�
						 * a) &&֮ǰ������ɷ�����û��ִ���������
						 * b) &&֮���Ѿ��ɷ���ȥ�����п�������������֮ǰ��ִ����ϲ��Ƴ���
						 */
						if (this != Serial.this.header.next && null != this.next)
							Serial.this.remove(this);
					} finally {
						lock.unlock();
					}
					return true;
				}
				return false;
			}

			// ���ﲻ������ done() ��ʵ�� scheduleNext ���߼���
			// ��Ϊ������ܻᱻ cancel����ʱ done() Ҳ�ᱻ���á�

			@Override
			public void run() {
				try {
					super.run();
				} finally {
					lock.lock();
					try {
						Serial.this.scheduleNext(this);
					} finally {
						lock.unlock();
					}
				}
			}

			@Override
			protected void setException(Throwable t) {
				// override to access protected.
				super.setException(t);
			}
		}

		////////////////////////////////////////////////////////////////////
		// queue ��ط���. ע�⣬unsafe! ����ʵ�ֲ���ȫ�����������ڲ�ʹ�ã����������ء�
		Serial.Task<?> getFirst() {
			if (size == 0)
			    throw new NoSuchElementException();
			return header.next;
		}

		Serial.Task<?> removeFirst() {
			return remove(getFirst());
		}

		private Task<?> remove(Task<?> task) {
			task.previous.next = task.next;
			task.next.previous = task.previous;
			task.next = task.previous = null; // ���ɾ�����Ľڵ㣬��ֹ������չ���ӿ������ռ�.
			size--;
			return task;
		}

		private <T> Serial.Task<T> addLast(Serial.Task<T> task) {
			task.next = header;
			task.previous = header.previous;
			header.previous.next = task;
			header.previous = task;
			++ size;
			return task;
		}

		/**
		 * �����������û������м�ӵ㡣
		 */
		void reset() {
			header.next = header.previous = header;
			size = 0;
		}
	}
}
