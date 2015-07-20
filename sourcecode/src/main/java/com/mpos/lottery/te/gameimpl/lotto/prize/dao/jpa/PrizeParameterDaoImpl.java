package com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.PrizeParameterDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.PrizeParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrizeParameterDaoImpl extends BaseJpaDao implements PrizeParameterDao {

    @Override
    public PrizeParameter findByPrizeLogicAndLevel(String prizeLogicId, String prizeLevel) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("prizeLogicId", prizeLogicId);
        params.put("prizeLevel", prizeLevel);
        List result = this.findByNamedParams("from PrizeParameter p where p.prizeLogicId=:prizeLogicId "
                + "and p.prizeLevel=:prizeLevel", params);
        if (result.size() == 0) {
            return null;
        }
        return (PrizeParameter) result.get(0);
    }

}
