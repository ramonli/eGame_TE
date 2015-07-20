package com.mpos.lottery.te.gameimpl.lotto.prize.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.PrizeParameter;

public interface PrizeParameterDao extends DAO {

    PrizeParameter findByPrizeLogicAndLevel(String prizeLogicId, String prizeLevel);
}
