package com.mpos.lottery.te.gameimpl.lotto.prize.dao;

import com.mpos.lottery.te.gameimpl.lotto.prize.domain.DailyCashWinningItem;

import java.sql.SQLException;
import java.util.List;

public interface DailyCashWinningItemDao {

    List<DailyCashWinningItem> findByTicketAndGameDraw(String serialNo, String gameDrawId, long lastSuccessfulVersion)
            throws SQLException;
}
