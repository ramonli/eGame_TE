package com.mpos.lottery.te.trans.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.trans.dao.PendingTransactionDao;
import com.mpos.lottery.te.trans.domain.PendingTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingTransactionDaoImpl extends BaseJpaDao implements PendingTransactionDao {

    @SuppressWarnings("unchecked")
    @Override
    public List<PendingTransaction> getByTicketSerialNo(String serialNo) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("serialNo", serialNo);
        return (List<PendingTransaction>) this.findByNamedParams(
                "from PendingTransaction t where t.ticketSerialNo=:serialNo", param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PendingTransaction> getByDeviceAndTraceMsgId(long deviceId, String traceMsgId) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("deviceId", deviceId);
        param.put("traceMsgId", traceMsgId);
        return (List<PendingTransaction>) this.findByNamedParams(
                "from PendingTransaction t where t.deviceId=:deviceId and t.traceMsgId=:traceMsgId", param);
    }

}
