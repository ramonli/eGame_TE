package com.mpos.lottery.te.sequence.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.sequence.domain.Sequence;
import com.mpos.lottery.te.sequence.domain.TicketSerialSpec;

import java.math.BigInteger;

/**
 * The SequenceService must be run in a independent transaction. I SequenceService join the transaction of
 * TicketService, let's image below condition: 1) SequenceService need to fetch new sequence and do update. 2)
 * TicketService throw exception result in the rollback of transaction. 3) then SequenceService got the next sequence,
 * but the database doesn't update the sequence. NOTE: due SequenceService will run in a transaction, here we won't
 * define UUIDManager in a transaction.
 */
public interface UUIDService {

    /**
     * The convenient method of {@link #getTicketSerialNo(int, int)}, the sale mode will be set to online.
     */
    String getTicketSerialNo(int gameType) throws ApplicationException;

    /**
     * Retrieve a formatted ticket serial number. Refer to {@link TicketSerialSpec} for the format definition.
     * 
     * @param saleMode
     *            Online sale or offline sale? Refer to {@link TicketSerialSpec#OFFLINE_MODE} and
     *            {@link TicketSerialSpec#ONLINE_MODE}
     * @param gameType
     *            THe game type of ticket.
     * @return a formatted ticket serial No.
     */
    String getTicketSerialNo(int saleMode, int gameType) throws ApplicationException;

    /**
     * Retrieve a formatted reference No, for example VAT reference No. Refer to {@link TicketSerialSpec} for the format
     * definition.
     * 
     * @param saleMode
     *            Online or offline?
     * @return a formatter reference No.
     */
    String getReferenceNo(int saleMode) throws ApplicationException;

    /**
     * Get a identifier for a transaction.
     */
    String getGeneralID() throws ApplicationException;

    /**
     * Get the current value of sequence with given name.
     * 
     * @param name
     *            The name of sequence. Refer to {@link Sequence#NAME_TICKETSERIALNO}, {@link Sequence#NAME_GENERAL}
     */
    BigInteger retrieveCurrentSeq(String name) throws ApplicationException;

    /**
     * Clear the cached sequence. After reset(), both requests to {@link #getTicketSerialNo(int)} ,
     * {@link #getGeneralID()} and {@link #getReferenceNo(int)} will query new sequence range from underlying database.
     * 
     * @param sequenceName
     *            Reset the sequence with given name( {@link Sequence#NAME_TICKETSERIALNO},
     *            {@link Sequence#NAME_GENERAL}). If sequenceName is null, then this operation will reset all sequences.
     */
    void reset(String sequenceName) throws ApplicationException;
}
