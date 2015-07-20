package com.mpos.lottery.te.gameimpl.magic100.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.magic100.sale.OfflineCancellation;

import java.util.List;

public interface OfflinecancellationDao extends DAO {
    List<OfflineCancellation> findByGameId(String gameId);

    List<OfflineCancellation> findByTransactionId(String teTransactionId);
}
