package xio.rpc;

import com.goldhuman.Common.Marshal.Marshal;

/**
 * �洢���̺�Rpc��ʱ��Ҫʵ������ӿ�
 *
 * @param <A>
 * @param <R>
 */
public interface Procedure<A extends Marshal, R extends Marshal> {
	public void setArgument(A a);
	public void setResult(R r);
	public void setConnection(xio.Xio from);

	public A getArgument();
	public R getResult();
	public xio.Xio getConnection();
}
