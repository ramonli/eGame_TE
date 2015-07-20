package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.dao.OperatorCommissionDao;
import com.mpos.lottery.te.merchant.domain.OperatorCommission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperatorCommissionDaoImpl extends BaseJpaDao implements OperatorCommissionDao {

    public OperatorCommission getByOperatorAndMerchantAndGame(String operatorId, long merchantId, String gameId) {
        Map params = new HashMap();
        params.put("operatorId", operatorId);
        params.put("gameId", gameId);
        params.put("merchantId", merchantId);
        String sql = "from OperatorCommission o where o.operatorId=:operatorId and o.game.id=:gameId and o.merchantId=:merchantId";
        List<OperatorCommission> result = this.findByNamedParams(sql, params);
        return this.single(result, true);
    }

}
