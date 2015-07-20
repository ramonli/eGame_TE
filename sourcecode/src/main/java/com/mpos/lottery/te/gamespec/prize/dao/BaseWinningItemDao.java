package com.mpos.lottery.te.gamespec.prize.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.bingo.prize.support.second.BingoLuckyPrizeResult;
import com.mpos.lottery.te.gameimpl.bingo.prize.support.second.BingoWinningLuckyItem;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import java.util.List;

public interface BaseWinningItemDao extends DAO {

    <T extends BaseWinningItem> List<T> findByGameInstanceAndSerialNoAndVersion(Class<T> clazz, String gameInstanceId,
            String ticketSerialNo, long version);

    /**
     * Find all winning records of a given ticket
     */
    <T extends BaseWinningItem> List<T> findBySerialNo(Class<T> clazz, String ticketSerialNo);

    List<BingoWinningLuckyItem> findSecondPrizeBySerialNo(String ticketSerialNo);

    /**
     * Query second prize infomation ,currently only supports the bingo game
     */
    List<BingoLuckyPrizeResult> findLuckyPrizeResultByLuckyNoAndSerialNoAndVersion(String gameInstanceId,
            String luckyNo, int version);

    /**
     * Find all winning records of a given ticket
     */

    public <T extends BaseWinningItem> List<T> findAllBySerialNo(Class<T> clazz, String ticketSerialNo);
}
