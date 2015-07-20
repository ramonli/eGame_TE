package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.merchant.domain.CreditTransferLog;

public interface CreditTransferLogDao extends DAO {

    CreditTransferLog findByTransactionId(String transactionId);
}
