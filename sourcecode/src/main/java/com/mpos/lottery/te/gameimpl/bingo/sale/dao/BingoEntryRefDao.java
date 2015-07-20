package com.mpos.lottery.te.gameimpl.bingo.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoEntryRef;

import java.util.List;

public interface BingoEntryRefDao extends DAO {

    List<BingoEntryRef> findByGameInstanceAndState(String gameInstanceId, int status);

    List<BingoEntryRef> findBySelectedNumber(String selectedNumber);
}
