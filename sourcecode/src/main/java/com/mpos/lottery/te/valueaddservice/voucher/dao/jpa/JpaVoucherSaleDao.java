package com.mpos.lottery.te.valueaddservice.voucher.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherSale;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherSaleDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository("jpaVoucherSaleDao")
public class JpaVoucherSaleDao extends BaseJpaDao implements VoucherSaleDao {

    @Override
    public VoucherSale findByTransaction(String transactionId) {
        String jpql = "from VoucherSale s where s.transaction.id=:transId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transId", transactionId);
        return (VoucherSale) this.findSingleByNamedParams(jpql, params);
    }

}
