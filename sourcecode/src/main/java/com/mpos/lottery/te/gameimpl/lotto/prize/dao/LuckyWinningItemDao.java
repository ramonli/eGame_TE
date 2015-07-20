package com.mpos.lottery.te.gameimpl.lotto.prize.dao;

import com.mpos.lottery.te.gameimpl.lotto.prize.domain.LuckyWinningItem;

import java.sql.SQLException;
import java.util.List;

public interface LuckyWinningItemDao {

    List<LuckyWinningItem> findByTicketAndGameDraw(String serialNo, String gameDrawId, long lastSuccessfulVersion)
            throws SQLException;

    public List<LuckyWinningItem> findByTicketSerial(String serialNo) throws SQLException;
}
