package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.dao.CreditTransferLogDao;
import com.mpos.lottery.te.merchant.domain.CreditTransferLog;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreditTransferLogDaoImpl extends BaseJpaDao implements CreditTransferLogDao {

    @Override
    public CreditTransferLog findByTransactionId(final String transactionId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transId", transactionId);
        List<CreditTransferLog> result = this.findByNamedParams(
                "from CreditTransferLog s where s.transactionId=:transId", params);
        CreditTransferLog log = (CreditTransferLog) this.single(result, false);
        if (log == null) {
            throw new DataIntegrityViolationException("No " + CreditTransferLog.class + " found by transactionId="
                    + transactionId);
        }
        return log;
    }

}
