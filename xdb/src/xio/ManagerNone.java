package xio;

/**
 * ���������ӵ�ʵ�֡�
 *
 * �������ӽ����κι����ʺ����ڴ������ķ�������
 */
public class ManagerNone extends Manager {
	@Override
	protected void addXio(Xio xio) {
	}

	@Override
	public Xio get() {
		return null;
	}

	@Override
	protected void removeXio(Xio xio, Throwable e) {
	}

	@Override
	public int size() {
		return 0;
	}
}
