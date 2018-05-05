
package xbean;

public interface TestOrderInfo extends xdb.Bean {
	public TestOrderInfo copy(); // deep clone
	public TestOrderInfo toData(); // a Data instance
	public TestOrderInfo toBean(); // a Bean instance
	public TestOrderInfo toDataIf(); // a Data instance If need. else return this
	public TestOrderInfo toBeanIf(); // a Bean instance If need. else return this

	public long getCreatetime(); // 
	public String getGsorderid(); // ��Ϸ����������id
	public com.goldhuman.Common.Octets getGsorderidOctets(); // ��Ϸ����������id
	public String getUseridentity(); // ƽ̨�û�id
	public com.goldhuman.Common.Octets getUseridentityOctets(); // ƽ̨�û�id
	public <T extends com.goldhuman.Common.Marshal.Marshal> T getVars(T _v_); // 
	public boolean isVarsEmpty(); // 
	public byte[] getVarsCopy(); // 

	public void setCreatetime(long _v_); // 
	public void setGsorderid(String _v_); // ��Ϸ����������id
	public void setGsorderidOctets(com.goldhuman.Common.Octets _v_); // ��Ϸ����������id
	public void setUseridentity(String _v_); // ƽ̨�û�id
	public void setUseridentityOctets(com.goldhuman.Common.Octets _v_); // ƽ̨�û�id
	public void setVars(com.goldhuman.Common.Marshal.Marshal _v_); // 
	public void setVarsCopy(byte[] _v_); // 
}
