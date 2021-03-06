
package map.msg;

import com.goldhuman.Common.Marshal.OctetsStream;
import com.goldhuman.Common.Marshal.MarshalException;

// {{{ RPCGEN_IMPORT_BEGIN
// {{{ DO NOT EDIT THIS

abstract class __MapContexProtocol__ extends xio.Protocol { }

// DO NOT EDIT THIS }}}
// RPCGEN_IMPORT_END }}}

public class MapContexProtocol extends __MapContexProtocol__ {
	@Override
	protected void process() {
		// protocol handle
	}

	// {{{ RPCGEN_DEFINE_BEGIN
	// {{{ DO NOT EDIT THIS
	public static final int PROTOCOL_TYPE = 6707916;

	public int getType() {
		return 6707916;
	}

	public int index; // 从0开始
	public com.goldhuman.Common.Octets data;

	public MapContexProtocol() {
		data = new com.goldhuman.Common.Octets();
	}

	public MapContexProtocol(int _index_, com.goldhuman.Common.Octets _data_) {
		this.index = _index_;
		this.data = _data_;
	}

	public final boolean _validator_() {
		return true;
	}

	public OctetsStream marshal(OctetsStream _os_) {
		_os_.marshal(index);
		_os_.marshal(data);
		return _os_;
	}

	public OctetsStream unmarshal(OctetsStream _os_) throws MarshalException {
		index = _os_.unmarshal_int();
		data = _os_.unmarshal_Octets();
		return _os_;
	}

	public boolean equals(Object _o1_) {
		if (_o1_ == this) return true;
		if (_o1_ instanceof MapContexProtocol) {
			MapContexProtocol _o_ = (MapContexProtocol)_o1_;
			if (index != _o_.index) return false;
			if (!data.equals(_o_.data)) return false;
			return true;
		}
		return false;
	}

	public int hashCode() {
		int _h_ = 0;
		_h_ += index;
		_h_ += data.hashCode();
		return _h_;
	}

	public String toString() {
		StringBuilder _sb_ = new StringBuilder();
		_sb_.append("(");
		_sb_.append(index).append(",");
		_sb_.append("B").append(data.size()).append(",");
		_sb_.append(")");
		return _sb_.toString();
	}

	// DO NOT EDIT THIS }}}
	// RPCGEN_DEFINE_END }}}

}

