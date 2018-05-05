
package gnet;

import com.goldhuman.Common.Marshal.Marshal;
import com.goldhuman.Common.Marshal.OctetsStream;
import com.goldhuman.Common.Marshal.MarshalException;

public class ActivationCodeErr implements Marshal , Comparable<ActivationCodeErr>{
	public final static int ERR_SUCCESS = 0; // �ɹ�
	public final static int ERR_FORMATE_INVALID = 1; // �������ʽ����
	public final static int ERR_INVALID = 2; // ��������Ч
	public final static int ERR_TYPE_NOT_MATCH = 3; // ���������Ͳ�ƥ��
	public final static int ERR_CODE_IS_USED = 4; // ��������ʹ��
	public final static int ERR_CODE_IS_EXPIRATED = 5; // �������ѹ���
	public final static int ERR_CODE_IS_NOT_OPEN = 6; // ������δ��ʹ��ʱ��
	public final static int ERR_FUNCTION_IS_CLOSED = 7; // �����빦���ѹر�
	public final static int ERR_PLATFORM_NOT_MATCH = 8; // ������ƽ̨��ƥ��
	public final static int ERR_HAS_ALEADY_ACTIVATED = 9; // �Ѿ�ʹ�ù�ͬһ���͵ļ�����
	public final static int ERR_NETWORK = 10; // deliver��auͨѶ�쳣
	public final static int ERR_EXCEED_DAY_USENUM = 11; // ����ÿ��ʹ�ô���
	public final static int ERR_EXCEED_ALL_USENUM = 12; // �����ۼ�ʹ�ô���
	public final static int ERR_INTERNAL = 13; // �������ڲ�����
	public final static int ERR_LEVEL_TOO_LOWE = 15; // �ȼ�̫��,�޷�ʹ��
	public final static int ERR_LEVEL_TOO_HIGH = 16; // �ȼ�̫��,�޷�ʹ��

	public int code;

	public ActivationCodeErr() {
	}

	public ActivationCodeErr(int _code_) {
		this.code = _code_;
	}

	public final boolean _validator_() {
		return true;
	}

	public OctetsStream marshal(OctetsStream _os_) {
		_os_.marshal(code);
		return _os_;
	}

	public OctetsStream unmarshal(OctetsStream _os_) throws MarshalException {
		code = _os_.unmarshal_int();
		return _os_;
	}

	public boolean equals(Object _o1_) {
		if (_o1_ == this) return true;
		if (_o1_ instanceof ActivationCodeErr) {
			ActivationCodeErr _o_ = (ActivationCodeErr)_o1_;
			if (code != _o_.code) return false;
			return true;
		}
		return false;
	}

	public int hashCode() {
		int _h_ = 0;
		_h_ += code;
		return _h_;
	}

	public String toString() {
		StringBuilder _sb_ = new StringBuilder();
		_sb_.append("(");
		_sb_.append(code).append(",");
		_sb_.append(")");
		return _sb_.toString();
	}

	public int compareTo(ActivationCodeErr _o_) {
		if (_o_ == this) return 0;
		int _c_ = 0;
		_c_ = code - _o_.code;
		if (0 != _c_) return _c_;
		return _c_;
	}

}

