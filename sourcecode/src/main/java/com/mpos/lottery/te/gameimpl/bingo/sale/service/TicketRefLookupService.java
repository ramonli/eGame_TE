package com.mpos.lottery.te.gameimpl.bingo.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicketRef;
import com.mpos.lottery.te.port.Context;

/**
 * If player doesn't pick any BINGO numbers, system will be responsible of generating numbers for player.
 * 
 * @author Ramon
 */
public interface TicketRefLookupService {

    /**
     * Look up bingo selected-numbers for client.
     * 
     * @param respCtx
     *            The context of current bingo sale transaction.
     * @param clientTicket
     *            The client sale request.
     * @param pickEntry
     *            Whether pick entries as well?
     * @return A in-advance generated ticket of 'new' status.
     * @throws ApplicationException
     *             if any business exception encountered.
     */
    BingoTicketRef lookupTicket(Context respCtx, BingoTicket clientTicket, boolean pickEntry)
            throws ApplicationException;
}
