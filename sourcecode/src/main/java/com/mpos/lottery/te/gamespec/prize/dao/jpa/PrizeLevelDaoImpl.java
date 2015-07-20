package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.prize.PrizeLevel;
import com.mpos.lottery.te.gamespec.prize.dao.PrizeLevelDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrizeLevelDaoImpl extends BaseJpaDao implements PrizeLevelDao {

    @Override
    public PrizeLevel findByPrizeLogicAndLevel(String prizeLogicId, int level) {
        String sql = "from PrizeLevel p where p.prizeLogicId=:prizeLogicId and p.prizeLevel=:prizeLevel";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("prizeLogicId", prizeLogicId);
        params.put("prizeLevel", level);
        return (PrizeLevel) this.findSingleByNamedParams(sql, params);
    }

    @Override
    public List<PrizeLevel> findByPrizeLogic(String prizeLogicId) {
        String sql = "from PrizeLevel p where p.prizeLogicId=:prizeLogicId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("prizeLogicId", prizeLogicId);
        return this.findByNamedParams(sql, params);
    }

}
