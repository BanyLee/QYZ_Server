
package lx.gs.map.msg;

import cfg.CfgMgr;
import cfg.item.EItemBindType;
import com.goldhuman.Common.Marshal.MarshalException;
import com.goldhuman.Common.Marshal.OctetsStream;
import common.Bonus;
import common.ErrorCode;
import gs.AProcedure;
import lx.gs.bonus.FBonus;
import lx.gs.logger.By;
import lx.gs.map.FMap;

import java.util.List;

// {{{ RPCGEN_IMPORT_BEGIN
// {{{ DO NOT EDIT THIS

abstract class __CObtainChapterReward__ extends xio.Protocol { }

// DO NOT EDIT THIS }}}
// RPCGEN_IMPORT_END }}}

public class CObtainChapterReward extends __CObtainChapterReward__ {
	@Override
	protected void process() {
		new AProcedure<CObtainChapterReward>(this) {

			@Override
			protected boolean doProcess() throws Exception {
				final xbean.RoleEctype info = FMap.getEctype(roleid);
				final xbean.ChapterInfo chapterInfo = info.getChapters().get(chapterid);
				final List<Integer> obtainRewardIndexs = chapterInfo.getObtainrewardindexs();
				if(obtainRewardIndexs.contains(rewardindex)) {
					return error(ErrorCode.HAS_OBTAIN_CHAPTER_REWARD);
				}
				final int totalStar = chapterInfo.getSectionstars().stream().mapToInt(x -> x).sum();
				final cfg.ectype.ChapterAward scfg = CfgMgr.chapter.get(chapterid).bonus_awardid.get(rewardindex);
				if(scfg.requirestar > totalStar) {
					return error(ErrorCode.CHAPTER_STAR_NOT_ENOUGH);
				}
				obtainRewardIndexs.add(rewardindex);
				boolean result = FBonus.addBonus(roleid, scfg.award, Bonus.BindType.BIND, By.Chapter_Reward);
				if(result) response(new SObtainChapterReward(chapterid, rewardindex));
				return result;
			}
		}.validateRoleidAndExecute();
	}

	// {{{ RPCGEN_DEFINE_BEGIN
	// {{{ DO NOT EDIT THIS
	public static final int PROTOCOL_TYPE = 6571545;

	public int getType() {
		return 6571545;
	}

	public int chapterid;
	public int rewardindex;

	public CObtainChapterReward() {
	}

	public CObtainChapterReward(int _chapterid_, int _rewardindex_) {
		this.chapterid = _chapterid_;
		this.rewardindex = _rewardindex_;
	}

	public final boolean _validator_() {
		return true;
	}

	public OctetsStream marshal(OctetsStream _os_) {
		_os_.marshal(chapterid);
		_os_.marshal(rewardindex);
		return _os_;
	}

	public OctetsStream unmarshal(OctetsStream _os_) throws MarshalException {
		chapterid = _os_.unmarshal_int();
		rewardindex = _os_.unmarshal_int();
		return _os_;
	}

	public boolean equals(Object _o1_) {
		if (_o1_ == this) return true;
		if (_o1_ instanceof CObtainChapterReward) {
			CObtainChapterReward _o_ = (CObtainChapterReward)_o1_;
			if (chapterid != _o_.chapterid) return false;
			if (rewardindex != _o_.rewardindex) return false;
			return true;
		}
		return false;
	}

	public int hashCode() {
		int _h_ = 0;
		_h_ += chapterid;
		_h_ += rewardindex;
		return _h_;
	}

	public String toString() {
		StringBuilder _sb_ = new StringBuilder();
		_sb_.append("(");
		_sb_.append(chapterid).append(",");
		_sb_.append(rewardindex).append(",");
		_sb_.append(")");
		return _sb_.toString();
	}

	public int compareTo(CObtainChapterReward _o_) {
		if (_o_ == this) return 0;
		int _c_ = 0;
		_c_ = chapterid - _o_.chapterid;
		if (0 != _c_) return _c_;
		_c_ = rewardindex - _o_.rewardindex;
		if (0 != _c_) return _c_;
		return _c_;
	}

	// DO NOT EDIT THIS }}}
	// RPCGEN_DEFINE_END }}}

}

