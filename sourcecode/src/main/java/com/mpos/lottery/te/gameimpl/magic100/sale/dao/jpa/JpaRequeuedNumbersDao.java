package com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.RequeuedNumbers;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.RequeuedNumbersDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaRequeuedNumbersDao extends BaseJpaDao implements RequeuedNumbersDao {

    @Override
    public List<RequeuedNumbers> findByGameInstanceAndCountOfValidNumber(String gameInstanceId, int countOfValidNumber) {
        String sql = "from RequeuedNumbers r where r.gameInstanceId=:gameInstanceId and r.countOfValidNumbers>=:countOfValidNumbers";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        params.put("countOfValidNumbers", countOfValidNumber);
        return this.findByNamedParams(sql, params);
    }

    @Override
    public RequeuedNumbers findByTransaction(String cancelTransactionId) {
        String sql = "from RequeuedNumbers r where r.transactionId=:transactionId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transactionId", cancelTransactionId);
        List result = this.findByNamedParams(sql, params);
        if (result.size() > 0) {
            return (RequeuedNumbers) result.get(0);
        }
        return null;
    }

}
