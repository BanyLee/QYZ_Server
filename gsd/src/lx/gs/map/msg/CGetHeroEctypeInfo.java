
package lx.gs.map.msg;

import com.goldhuman.Common.Marshal.MarshalException;
import com.goldhuman.Common.Marshal.OctetsStream;
import gs.AProcedure;
import lx.gs.map.FMap;
import xbean.HeroesGroupInfo;

import java.util.Map;

// {{{ RPCGEN_IMPORT_BEGIN
// {{{ DO NOT EDIT THIS

abstract class __CGetHeroEctypeInfo__ extends xio.Protocol { }

// DO NOT EDIT THIS }}}
// RPCGEN_IMPORT_END }}}

public class CGetHeroEctypeInfo extends __CGetHeroEctypeInfo__ {
	@Override
	protected void process() {
		new AProcedure<CGetHeroEctypeInfo>(this) {
			@Override
			protected boolean doProcess() throws Exception {
				final Map<Integer, HeroesGroupInfo> info = FMap.getEctype(roleid).getHerogroups();
				final SGetHeroEctypeInfo resp = new SGetHeroEctypeInfo();
				info.forEach((k, v) -> {
					final lx.gs.map.msg.HeroesGroupInfo groupInfo = new lx.gs.map.msg.HeroesGroupInfo(v.getRefreshtime(), v.getEctypeid());
					resp.herogroups.put(k, groupInfo);
				});
				response(resp);
				return true;
			}
		}.validateRoleidAndExecute();
	}

	// {{{ RPCGEN_DEFINE_BEGIN
	// {{{ DO NOT EDIT THIS
	public static final int PROTOCOL_TYPE = 6583067;

	public int getType() {
		return 6583067;
	}


	public CGetHeroEctypeInfo() {
	}

	public final boolean _validator_() {
		return true;
	}

	public OctetsStream marshal(OctetsStream _os_) {
		return _os_;
	}

	public OctetsStream unmarshal(OctetsStream _os_) throws MarshalException {
		return _os_;
	}

	public boolean equals(Object _o1_) {
		if (_o1_ == this) return true;
		if (_o1_ instanceof CGetHeroEctypeInfo) {
			return true;
		}
		return false;
	}

	public int hashCode() {
		int _h_ = 0;
		return _h_;
	}

	public String toString() {
		StringBuilder _sb_ = new StringBuilder();
		_sb_.append("(");
		_sb_.append(")");
		return _sb_.toString();
	}

	public int compareTo(CGetHeroEctypeInfo _o_) {
		if (_o_ == this) return 0;
		int _c_ = 0;
		return _c_;
	}

	// DO NOT EDIT THIS }}}
	// RPCGEN_DEFINE_END }}}

}

