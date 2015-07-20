package com.mpos.lottery.te.trans.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionRetryLog;

/**
 * The 'XXXServiceAsyn' means this service will require a new transaction. Please refer to 'transaction definition' part
 * of spring-service.xml for more information.
 */
public interface TransactionRetryLogServiceAsyn {

    TransactionRetryLog add(String ticketSerialNo, int transType, long deviceId) throws ApplicationException;

    void update(TransactionRetryLog retryLog) throws ApplicationException;

    TransactionRetryLog getBySerialNoAndTransTypeAndDevice(String ticketSerialNo, int transType, long deviceId)
            throws ApplicationException;

    /**
     * check if client has issued same transaction too many times.
     * 
     * @param trans
     *            The transaction
     * @param ticketSerialNo
     *            The serial number of ticket(lotto, ig, toto, etc)
     * @param transType
     *            The transaction type.
     * @param game
     *            The game associates with given ticket.
     * @return true if the total times has exceeded max allowed times, or false.
     */
    TransactionRetryLog checkMaxValidationTimes(Transaction trans, String ticketSerialNo, int transType, Game game)
            throws ApplicationException;
}
