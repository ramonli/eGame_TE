package com.mpos.lottery.te.gameimpl.magic100.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumber;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.port.Context;

import java.util.List;

public interface LuckyNumberService {

    /**
     * Determine which lucky number should be sold.
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param clientTicket
     *            The client ticket request.
     * @return A list of lucky number which will be sold to current player.
     */
    List<LuckyNumber> determine(Context respCtx, Magic100Ticket clientTicket) throws ApplicationException;

    /**
     * Cancel the sale of a set of lucky numbers. Those cancelled numbers must be requeued and can be sold for next sale
     * request.
     */
    void cancel(Context respCtx, Magic100Ticket ticket) throws ApplicationException;
}
