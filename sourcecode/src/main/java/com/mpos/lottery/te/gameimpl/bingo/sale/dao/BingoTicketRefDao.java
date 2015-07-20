package com.mpos.lottery.te.gameimpl.bingo.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicketRef;

import java.util.List;

public interface BingoTicketRefDao extends DAO {

    List<BingoTicketRef> findByGameInstanceAndState(String gameInstanceId, int status);

    BingoTicketRef findByGameInstanceAndSequence(String gameInstanceId, long sequence);

    BingoTicketRef findBySerialNo(String serialNo);
}
