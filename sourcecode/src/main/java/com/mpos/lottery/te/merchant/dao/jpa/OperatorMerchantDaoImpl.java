package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.dao.OperatorMerchantDao;
import com.mpos.lottery.te.merchant.domain.OperatorMerchant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperatorMerchantDaoImpl extends BaseJpaDao implements OperatorMerchantDao {

    @Override
    public OperatorMerchant findByOperatorAndMerchant(String operatorID, long merchantID) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("operatorID", operatorID);
        params.put("merchantID", merchantID);
        params.put("state", OperatorMerchant.STATE_VALID);
        List result = this.findByNamedParams("from OperatorMerchant o where o.operatorID=:operatorID and "
                + "o.merchantID=:merchantID and o.state=:state", params);
        return (OperatorMerchant) this.single(result, true);
    }

    @Override
    public OperatorMerchant findByOperator(String operatorId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("operatorID", operatorId);
        params.put("state", OperatorMerchant.STATE_VALID);
        List result = this.findByNamedParams(
                "from OperatorMerchant o where o.operatorID=:operatorID and o.state=:state", params);
        return (OperatorMerchant) this.single(result, true);
    }

}
