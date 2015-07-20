package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatSaleTransactionDao;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("vatSaleTransactionDao")
public class JpaVatSaleTransactionDao extends BaseJpaDao implements VatSaleTransactionDao {

    @Override
    public VatSaleTransaction findByTransaction(String teTransId) throws DataAccessException {
        String sql = "from VatSaleTransaction d where d.transactionId=:transactionId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transactionId", teTransId);
        List result = this.findByNamedParams(sql, params);
        return (VatSaleTransaction) this.single(result, true);
    }

    @Override
    public VatSaleTransaction findBySerialnoAndOperatorid(String serialno, String operatorid)
            throws DataAccessException {
        String sql = "from VatSaleTransaction d where d.ticketSerialNo=:ticketSerialNo and d.operatorId=:operatorId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ticketSerialNo", serialno);
        params.put("operatorId", operatorid);
        List result = this.findByNamedParams(sql, params);
        return (VatSaleTransaction) this.single(result, true);
    }

    @Override
    public VatSaleTransaction findByRefNo(String refNo) throws DataAccessException {
        String sql = "from VatSaleTransaction d where d.vatRefNo=:refNo";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refNo", refNo);
        List result = this.findByNamedParams(sql, params);
        return (VatSaleTransaction) this.single(result, false);
    }

}
