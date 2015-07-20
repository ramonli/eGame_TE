package com.mpos.lottery.te.trans.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.trans.domain.TransactionRetryLog;

public interface TransactionRetryLogDao extends DAO {

    TransactionRetryLog getByTicketAndTransTypeAndDevice(String ticketSerialNO, int transType, long deviceId);

}
