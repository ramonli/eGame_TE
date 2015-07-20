package com.mpos.lottery.te.trans.dao;

import com.mpos.lottery.te.trans.domain.PendingTransaction;

import java.util.List;

public interface PendingTransactionDao {

    void insert(Object entity);

    List<PendingTransaction> getByTicketSerialNo(String serialNo);

    List<PendingTransaction> getByDeviceAndTraceMsgId(long deviceId, String traceMsgId);

}
