package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.util.List;

public interface TicketService {
    /**
     * Save a ticket. If the ticket is multlpleDraw, the multiple records will be saved. Before save a ticket, below
     * condition will be checked:
     * <ul>
     * <li>The game draw is open for sell ticket(game draw isn't inactive, and current time is between startSellingTime
     * and stopSellingTime).</li>
     * <li>The format of selectedNumber is legal.</li>
     * <li>Ignored: The totalAmount is right</li>
     * <li>Check credit level</li>
     * </ul>
     * 
     * @param respCtx
     *            Context of transaction.
     * @param clientTicket
     *            LottoTicket data transfer object, the following components of this arguments must not be null:
     *            gameInstance.gameId, gameIntance.drawNo,totalAmount, entries.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    List<? extends BaseTicket> sell(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException;

    /**
     * Cancel a ticket with specified serialNo. After cancellation, the status ticket should be 'canceled'. If ticket is
     * multipleDraw, all tickets with this serialNo will all be set to 'canceled'.
     * <p>
     * However <code>Controller</code> shouldn't call this service directly, it should call
     * {@link com.mpos.lottery.te.trans.service.TransactionService#reverseOrCancel(Context, BaseTicket)} instead.
     * 
     * @param respCtx
     *            Context of transaction.
     * @param clientTicket
     *            LottoTicket data transfer object, the following components of this arguments must not be null:
     *            serialNo
     * @return true if the cancel is declined, otherwise false will be returned.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    boolean cancelByTicket(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException;
}
