package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.prize.TaxThreshold;
import com.mpos.lottery.te.gamespec.prize.dao.TaxThresholdDao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxThresholdDaoImpl extends BaseJpaDao implements TaxThresholdDao {

    public TaxThreshold getByPolicyAndAmountAndDateRange(String taxPolicyId, BigDecimal amount, Date taxDate) {
        Map params = new HashMap();
        params.put("now", taxDate);
        params.put("amount", amount);
        params.put("taxPolicyId", taxPolicyId);
        String query = "select t from TaxThreshold t "
                + "where t.taxPolicyId=:taxPolicyId and (:now between t.taxDateRange.beginDate "
                + "and t.taxDateRange.endDate) and t.minAmount<=:amount and t.maxAmount>=:amount";
        List<TaxThreshold> list = this.findByNamedParams(query, params);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }
}
