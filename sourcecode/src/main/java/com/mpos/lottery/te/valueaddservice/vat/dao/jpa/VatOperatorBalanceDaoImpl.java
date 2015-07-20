package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.valueaddservice.vat.VatOperatorBalance;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatOperatorBalanceDao;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

@Repository("vatOperatorBalanceDao")
public class VatOperatorBalanceDaoImpl extends BaseJpaDao implements VatOperatorBalanceDao {

    @Override
    public VatOperatorBalance findByOperatorIdForUpdate(String operatorid) throws DataAccessException {
        long waitLockTime = MLotteryContext.getInstance().getWaitLockTime();

        String sql = "select * from VAT_OPERATOR_BALANCE where OPERATOR_ID=? for update";
        if (waitLockTime > 0) {
            sql += " wait " + waitLockTime;
        }
        Query query = this.getEntityManager().createNativeQuery(sql, VatOperatorBalance.class);
        query.setParameter(1, operatorid);
        List<Object> listVatOperatorBalance = query.getResultList();
        if (listVatOperatorBalance == null || listVatOperatorBalance.size() <= 0) {
            return null;
        }
        return (VatOperatorBalance) listVatOperatorBalance.get(0);
    }

    @Override
    public VatOperatorBalance findByOperator(String operatorid) throws DataAccessException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("operatorId", operatorid);
        List<VatOperatorBalance> result = this.findByNamedParams(
                "from VatOperatorBalance b where b.operatorId=:operatorId", params);
        return this.single(result, false);
    }
}
