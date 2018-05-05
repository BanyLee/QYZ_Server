package xio;

import java.util.HashSet;
import java.util.Set;

/**
 * �����е����ӷ���һ�������й���������Ϊһ������ʵ�ֵĻ������á��������¹��ܣ�
 * <ol>
 * <li>����ر�ʱ���ر��������ӡ�
 * <li>���Ӷ���Ļص� onXioRemoved onXioAdd ���ڼ̳С�
 * </ol>
 */
public class ManagerSimple extends Manager {
	private Set<Xio> xios = new HashSet<Xio>();
	private Object mutex = new Object();

	protected void onXioRemoved(Xio x, Throwable e) {
		xdb.Trace.warn("onXioRemoved=" + x, e);
	}

	protected void onXioAdded(Xio x) {
		if (xdb.Trace.isWarnEnabled())
			xdb.Trace.warn("onXioAdded=" + x);
	}

	@Override
	protected void removeXio(Xio xio, Throwable e) {
		boolean removed = false;
		synchronized (mutex) {
			removed = xios.remove(xio);
		}
		if (removed)
			try { onXioRemoved(xio, e); } catch (Throwable e2) { /* skip */ }
	}

	@Override
	protected void addXio(Xio xio) {
		boolean added = false;
		if (Engine.isOpen()) {
			synchronized (mutex) {
				added = (super.getMaxSize() <= 0 || xios.size() < super.getMaxSize()) && xios.add(xio);
			}
		}
		if (added)
			try { onXioAdded(xio); } catch (Throwable e) { /* skip */ }
		else {
			xdb.Trace.warn("Close in addXio " + xio + " size=" + size() + "/" + getMaxSize());
			xio.close();
		}
	}

	@Override
	protected void close() {
		super.close();

		// ���ڴ������ӵ�Ӧ�ã����ر����Ӹ��á�Ŀǰ�����ɾ����˳���
		Set<Xio> tmp;
		synchronized (mutex) {
			tmp = xios;
			xios = new HashSet<Xio>();
		}
		for (Xio io : tmp)
			io.close();
	}

	@Override
	public int size() {
		synchronized (mutex) {
			return xios.size();
		}
	}

	@Override
	public Xio get() {
		synchronized (mutex) {
			if (xios.isEmpty())
				return null;
			return xios.iterator().next();
		}
	}

//	// poll �ò������ӣ��͵ȴ�
//	public Xio take() {
//		synchronized (xios) {
//			while (xios.isEmpty()) {
//				try {
//					xios.wait();
//				} catch (InterruptedException e) {
//					throw new RuntimeException(e);
//				}
//			}
//			Xio x = xios.iterator().next();
//			xios.remove(x);
//			return x;
//		}
//	}
//
//	public void put(Xio xio) {
//		boolean added = false;
//		if (Engine.isOpen()) {
//			synchronized (xios) {
//				added = xios.add(xio);
//			}
//		}
//		if (!added)
//			xio.close();
//	}

}
