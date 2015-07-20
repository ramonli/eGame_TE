package com.mpos.lottery.te.gamespec.prize.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.prize.Payout;

import org.springframework.dao.DataAccessException;

import java.util.List;

public interface PayoutDao extends DAO {

    List<Payout> getByTicketSerialNo(String ticketSerialNo) throws DataAccessException;

    List<Payout> getByTicketSerialNoAndStatus(String ticketSerialNo, int status) throws DataAccessException;

    /**
     * Fetch payouts by transaction-id and status. Due to multiple-draw ticket, one transaction associate with multiple
     * payout records.
     */
    List<Payout> getByTransactionAndStatus(String transactionId, int status) throws DataAccessException;

    /**
     * Due to table 'payout' doesn't aware of game, all payouts, including lotto, ig, horse racing, etc will be stored
     * in this table, the ticket seiral number maybe multipled(different game). To batch validation, one transaction
     * will associate with many payout records, so we have to use transaction and ticket to locate a payout record.
     */
    List<Payout> getByTransactionAndTicketAndStatus(String transactionId, String ticketSerialNo, int status)
            throws DataAccessException;

}
