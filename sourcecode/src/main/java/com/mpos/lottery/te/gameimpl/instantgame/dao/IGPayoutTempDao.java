package com.mpos.lottery.te.gameimpl.instantgame.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGPayoutTemp;
import com.mpos.lottery.te.gamespec.game.Game;

import java.math.BigDecimal;
import java.util.List;

public interface IGPayoutTempDao extends DAO {

    /**
     * Get temp Payout record from db according to batchNumber,operatorId,serialNumber.
     * */
    IGPayoutTemp getPayoutTempByCondition(long batchNumber, String operatorId, String serialNumber);

    /**
     * Need to check whether current IT ticket is handling by another operatorId.
     * */
    boolean isUsedByAnotherOperatorId(String operatorId, String serialNumber);

    /**
     * Get total before tax amount of all IG tickets of a operator validated in a batch.
     * */
    BigDecimal getTotoalAmountBeforeTax(long batchNumber, String operatorId);

    /**
     * Get actual amount of all IG tickets of a operator validated in a batch.
     * */
    BigDecimal getActualAmount(long batchNumber, String operatorId);

    /**
     * move temporary payout data to payout table.
     * */
    void movePayoutData(long batchNumber, String operatorId);

    /**
     * set status of all IG tickets to valid.
     * */
    void validateAllTicket(long batchNumber, String operatorId);

    /**
     * get all games of this batch.
     * */
    List<Game> getAllGamesOfThisBatch(long batchNumber, String operatorId);

    /**
     * Get actual amount of all IG tickets of a operator validated in a batch.
     * */
    BigDecimal getActualAmountByGame(Game game, long batchNumber, String operatorId);

    /**
     * Get numbers of all IG tickets of a operator validated in a batch.
     * */
    Long getSucceededTicketsCount(long batchNumber, String operatorId);

    /**
     * set status of all IG tickets to what you want.
     * */
    void changeStatusOfAllIGTickets(int status, long batchNumber, String operatorId);

    /**
     * delete data by operatorid.
     */
    void deleteDataByOperatorId(long batchNumber, String operatorId);

}
