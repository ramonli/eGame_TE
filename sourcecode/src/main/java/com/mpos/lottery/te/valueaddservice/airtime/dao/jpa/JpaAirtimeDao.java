package com.mpos.lottery.te.valueaddservice.airtime.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;
import com.mpos.lottery.te.valueaddservice.airtime.dao.AirtimeDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository("jpaAirtimeDao")
public class JpaAirtimeDao extends BaseJpaDao implements AirtimeDao {

    @Override
    public AirtimeTopup getAirtimeTopupByTeTransactionId(String teTransactionId) {
        String sql = "from AirtimeTopup as at where at.transaction.id=:transId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transId", teTransactionId);
        return (AirtimeTopup) this.findSingleByNamedParams(sql, params);
    }

}
