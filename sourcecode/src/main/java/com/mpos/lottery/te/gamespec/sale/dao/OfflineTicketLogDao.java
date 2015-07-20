package com.mpos.lottery.te.gamespec.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.sale.OfflineTicketLog;

import java.util.List;

public interface OfflineTicketLogDao extends DAO {

    List<OfflineTicketLog> findByTransaction(String transactionId);
}
