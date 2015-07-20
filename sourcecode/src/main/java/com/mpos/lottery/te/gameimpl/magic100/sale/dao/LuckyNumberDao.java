package com.mpos.lottery.te.gameimpl.magic100.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumber;

import java.util.List;

public interface LuckyNumberDao extends DAO {

    /**
     * Lookup a set of sequential numbers. If lucky number reach the end of a cycle, it should restart from beginning
     * then.
     * 
     * @param sequenceNum
     *            The sequence No. of begin lucky number.
     * @param gameInstanceId
     *            The game instance.
     * @param countOfNumbers
     *            How many numbers will be sold?
     * @return a set of sequential lucky numbers.
     */
    List<LuckyNumber> findBySeuqnce(long sequenceNum, String gameInstanceId, int countOfNumbers);

    List<LuckyNumber> findBySeuqnces(String gameInstanceId, Long... sequenceNum);

    List<LuckyNumber> findByGameInstanceId(String gameInstanceId);

    int getMaxNumberSeq(String gameInstanceId);
}
