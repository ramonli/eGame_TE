package com.mpos.lottery.te.trans.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.trans.dao.TransactionRetryLogDao;
import com.mpos.lottery.te.trans.domain.TransactionRetryLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionRetryLogDaoImpl extends BaseJpaDao implements TransactionRetryLogDao {

    public TransactionRetryLog getByTicketAndTransTypeAndDevice(String ticketSerialNo, int transType, long deviceId) {
        String sql = "from TransactionRetryLog t where t.ticketSerialNo=:ticketSerialNo and "
                + "t.transType=:transType and t.deviceId=:deviceId and t.version=:version";
        Map params = new HashMap(2);
        params.put("ticketSerialNo", ticketSerialNo);
        params.put("transType", transType);
        params.put("deviceId", deviceId);
        params.put("version", (long) TransactionRetryLog.VERSION_VALID);
        List<TransactionRetryLog> logs = this.findByNamedParams(sql, params);
        if (logs.size() > 1) {
            throw new SystemException("Find " + logs.size() + " TransactionRetryLog(ticketSerialNo=" + ticketSerialNo
                    + ",transType=" + transType + ") records, it should be only 1 record.");
        }
        return logs.size() == 0 ? null : logs.get(0);
    }

}
