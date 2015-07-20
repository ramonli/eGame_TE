package com.mpos.lottery.te.gameimpl.lotto.prize.dao;

import com.mpos.lottery.te.gameimpl.lotto.prize.domain.PrizeObject;

import java.sql.SQLException;

public interface PrizeObjectDao {

    /**
     * Find prize object. Be note that this method is for the second chance prize of LOTTO.
     * 
     * @param prizeLogicId
     *            The prize logic associated with given game instance.
     * @param prizeLevel
     *            The prize level of a lucky winning item.
     * @param winningType
     *            The winning type, refer to <code>LuckyWinningItem.winningType</code>
     * @param version
     *            The version which will be used when redo occured.
     * @return a prize object.
     * @throws SQLException
     *             when encounter a database related exception.
     */
    PrizeObject findByPrizeLogicAndLevel(String prizeLogicId, int prizeLevel, int winningType, int version)
            throws SQLException;

}
