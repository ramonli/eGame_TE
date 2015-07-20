package com.mpos.lottery.te.gamespec.prize.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.prize.PrizeLevel;

import java.util.List;

public interface PrizeLevelDao extends DAO {

    PrizeLevel findByPrizeLogicAndLevel(String prizeLogicId, int level);

    List<PrizeLevel> findByPrizeLogic(String prizeLogicId);
}
