
package xbean;

public interface UserInfo extends xdb.Bean {
	public UserInfo copy(); // deep clone
	public UserInfo toData(); // a Data instance
	public UserInfo toBean(); // a Bean instance
	public UserInfo toDataIf(); // a Data instance If need. else return this
	public UserInfo toBeanIf(); // a Bean instance If need. else return this

	public int getPlattype(); // ƽ̨id
	public String getUseridentity(); // ƽ̨�û�id
	public com.goldhuman.Common.Octets getUseridentityOctets(); // ƽ̨�û�id

	public void setPlattype(int _v_); // ƽ̨id
	public void setUseridentity(String _v_); // ƽ̨�û�id
	public void setUseridentityOctets(com.goldhuman.Common.Octets _v_); // ƽ̨�û�id
}
