package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

public interface OfflineTicketService {

    /**
     * Sync a offline ticket. If the ticket is multlpleDraw, the multiple records will be saved. As system can't control
     * when the client will upload offline tickets, there is risk that ticket is sold at DRAW#1, however it is uploaded
     * at DRAE#2.
     * <p>
     * In this case, system will record a special turnover to the game instance which is active at the time point of
     * uploading.
     * <p>
     * <b>Pre-Condition</b>
     * <ul>
     * <li>None of any associated game instances is 'in progress of winner analysis'. if we do uploading during winner
     * analysis, it will break the process of winner analysis.</li>
     * </ul>
     * <b>Post-Condition</b>
     * <ul>
     * <li>Generate {@link com.mpos.lottery.te.gamespec.sale.OfflineTicketLog}</li>
     * <li>Generate concrete entity {@link com.mpos.lottery.te.gamespec.sale.BaseTicket}</li>
     * </ul>
     * <p>
     * <b>Usage Restriction</b>
     * <ul>
     * <li>Authorization - Can only be called by signed in user.</li>
     * <li>Concurrent Access - To a single device, this interface must be called sequentially. To multiple devices,
     * there are no limitation on the number of concurrent accesses.</li>
     * <li>Message Redelivery - Client can't simply redeliver the same message content(with different trace message ID)
     * multiple times, the backend will regard it as a new ticket.</li>
     * </ul>
     * 
     * @param respCtx
     *            Context of transaction.
     * @param clientTicket
     *            The offline ticket sold by client.
     * @return The offline ticket which has been assembled by the backend.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    BaseTicket sync(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException;

}
