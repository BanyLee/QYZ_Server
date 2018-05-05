package xdb.util;

import java.util.LinkedList;
import xdb.Procedure;

/**
 * ������˳�򣬰���ִ�д洢���̡��κ�ʱ�����ֻ��һ���洢��������ִ�С�
 * 
 * �ڲ����첽�����£�Ϊ����ȷ����ĳЩ���⣬��Ҫ��Ƹ��ӵ�Э�顣
 * OneByOne ���Ⲣ����˳���Ժ����Э�������ס�
 * 
 * ���ܣ�
 *   1 �������ơ��ﵽ�������add �����׳� RuntimeException��
 *   2 �������ƣ�(δʵ��)������ʱ����ƴ洢����������
 *   3 ͳ�ƣ�(δʵ��)��Ū������ͳ�ƣ�
 *   4 shutdown��(δʵ��)��a) �ȴ����еĶ�ִ�����˲ŷ��� b)�ж�����ִ�еģ����ػ�û��ִ�еĴ洢���̡�
 *   
 * @author lichenghua
 *
 */
public class ProcedureOneByOne {
	private int maxsize;
	private LinkedList<xdb.Procedure> onebyone = new LinkedList<xdb.Procedure>();
	private xdb.Procedure.Done<xdb.Procedure> done = new xdb.Procedure.Done<xdb.Procedure>() {
		@Override
		public void doDone(Procedure p) {
			synchronized (ProcedureOneByOne.this) {
				Procedure f = onebyone.removeFirst();
				assert(f == p); // ����OneByOne���⡣
				if (onebyone.size() > 0)
					xdb.Procedure.execute(onebyone.peekFirst(), done);
			}
		}
	};

	public ProcedureOneByOne() {
		this.maxsize = 0; // unlimited
	}

	public ProcedureOneByOne(int maxsize) {
		this.maxsize = maxsize;
	}

	public void add(xdb.Procedure p) {
		synchronized (this) {
			if (maxsize > 0 && onebyone.size() > maxsize)
				throw new RuntimeException("out of capacity! maxsize=" + maxsize);
			onebyone.addLast(p);
			if (onebyone.size() == 1)
				xdb.Procedure.execute(onebyone.peekFirst(), done);
		}
	}

	//////////////////////////////////////////////////////////////
	// ��ʱ��Ҫ��������ķ�������Щ���ܻ�ûȷ����
	public xdb.Procedure peekDebugOnly() {
		synchronized (this) {
			return onebyone.peekFirst();
		}
	}

	public int sizeDebugOnly() {
		synchronized (this) {
			return onebyone.size();
		}
	}
}
