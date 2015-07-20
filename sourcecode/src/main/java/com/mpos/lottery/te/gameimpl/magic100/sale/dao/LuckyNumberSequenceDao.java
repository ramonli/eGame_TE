package com.mpos.lottery.te.gameimpl.magic100.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumberSequence;

public interface LuckyNumberSequenceDao extends DAO {

    /**
     * There should be only one entity of type of <code>LuckyNumberSeqLog</code> for a game.
     * 
     * @param gameId
     *            In which game the lucky numbers will be sold?
     */
    LuckyNumberSequence lookup(String gameId);
}
