
package xbean;

public interface ActivationCodeSet extends xdb.Bean {
	public ActivationCodeSet copy(); // deep clone
	public ActivationCodeSet toData(); // a Data instance
	public ActivationCodeSet toBean(); // a Bean instance
	public ActivationCodeSet toDataIf(); // a Data instance If need. else return this
	public ActivationCodeSet toBeanIf(); // a Bean instance If need. else return this

	public int getType(); // 
	public java.util.Set<Long> getValues(); // 
	public java.util.Set<Long> getValuesAsData(); // 
	public long getCreatetime(); // 
	public long getOpentime(); // 
	public long getExpiratetime(); // 
	public java.util.Set<Integer> getPlatformset(); // ���Լ����ƽ̨���ձ�ʾ��ƽ̨����
	public java.util.Set<Integer> getPlatformsetAsData(); // ���Լ����ƽ̨���ձ�ʾ��ƽ̨����
	public int getIsshared(); // �Ƿ��ǹ���������
	public boolean getIslogin(); // �Ƿ����ڼ������½

	public void setType(int _v_); // 
	public void setCreatetime(long _v_); // 
	public void setOpentime(long _v_); // 
	public void setExpiratetime(long _v_); // 
	public void setIsshared(int _v_); // �Ƿ��ǹ���������
	public void setIslogin(boolean _v_); // �Ƿ����ڼ������½
}
