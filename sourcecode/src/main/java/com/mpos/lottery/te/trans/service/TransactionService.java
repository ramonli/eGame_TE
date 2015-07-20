package com.mpos.lottery.te.trans.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.CompositeTicketService;
import com.mpos.lottery.te.gamespec.sale.service.TicketEnquiryService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

/**
 * The service definition of Transaction. Guarantee that save/update transaction is in individual transaction(database
 * transaction). In spring transaction definition, a interface method will required a transaction(if a transaction has
 * existed, join it. or, create a new transaction). Here, ServiceFacade will invoke TransactionService in individual
 * transaction.
 */
public interface TransactionService {

    /**
     * Register a game type specific ticket enquiry implementation. Refer to {#link
     * {@link CompositeTicketService#enquiry(Context, com.mpos.lottery.te.gamespec.sale.BaseTicket, boolean)}
     */
    void registerTicketEnquiry(GameType gameType, TicketEnquiryService ticketEquiryService);

    /**
     * Get transaction by terminal id and trace message id. It maps to 'Transaction Enquiry' transaction.
     * 
     * @param terminalId
     *            The identifier of terminal
     * @param traceMessageId
     *            The identifier of trace message.
     * @return a Transaction instance with specified criteria.
     */
    Transaction enquiry(Context respCtx, long terminalId, String traceMessageId) throws ApplicationException;

    /**
     * Save a new transaction.
     */
    void save(Transaction trans) throws ApplicationException;

    /**
     * update a exist transaction instance.
     */
    void update(Transaction trans) throws ApplicationException;

    /**
     * Handle incoming requests of reversal or cancel by transaction.
     * <p>
     * Reversal will reverse a 'payout' and 'validate instant ticket' transaction. When reverse different transaction,
     * corresponding strategy should be adopted. below condition must be checked:
     * <ol>
     * <li>Is the status of ticket(ticket record associate with bought game draw) 'paid'?</li>
     * </ol>
     * Reversal just set the status of 'payed' ticket to 'accepted', then set payout record to 'reversed'
     * 
     * @param dbTrans
     *            The transaction associating with 'Payout'.
     * @return true if cancel declined, or false
     * @throws ApplicationException
     *             when encounter any business logic exception.
     */
    boolean reverseOrCancel(Context<?> respCtx, Transaction dbTrans) throws ApplicationException;

    /**
     * Cancel a ticket, in general should be a sale transaction. This service will call
     * {@link #reverseOrCancel(Context, Transaction)}
     */
    boolean reverseOrCancel(Context<?> respCtx, BaseTicket ticket) throws ApplicationException;

    /**
     * When client switches between TE instances, cancel transactions must be treated carefully. Imagine that client
     * issues a sale request to the first TE instance, then a cancel request is issued to the sencond Te instance(maybe
     * the first TE instance is unavaliable).
     * <p>
     * This method will handle this case. If no corresponding sale transaction found, just record it, other process will
     * handle these pending cancel request later(maybe after the data synchronization between two Te instances). handle
     * <p>
     * PRE-CONDITION: The corresponding sale which client going to cancel doesn't exist.
     * 
     * @param cancelTransType
     *            All TE supported cancellation type, including reversal.
     * @param params
     *            If canceltransType is 'cancelBySerialNo', the params will be [String serialNo, long deviceId, String
     *            traceMsgId]. If cancel by transaction, the params will be [long deviceId, String traceMsgId].
     */
    void pendTransaction(int cancelTransType, Object... params) throws ApplicationException;
}
