package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameTypeAware;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.web.QPEnquiryDto;
import com.mpos.lottery.te.port.Context;

public interface TicketEnquiryService extends GameTypeAware {

    /**
     * Enquiry a ticket with a specified ticketSerialNo. The implementation must check if the ticket is multiplDraws, if
     * so, implementation should assemble one LottoTicket instance from multiple LottoTicket records.
     * <ul>
     * <li>The gameDraw of returned ticket must be the oldest draw in all tickets with same serialNo</li>
     * <li>The value of multipleDraw should be the size of ticket list with same serialNo</li>
     * </ul>
     * 
     * @param respCtx
     *            Context of transaction.
     * @param clientTicket
     *            LottoTicket data transfer object, the following components of this arguments must not be null:
     *            serialNo
     * @param fetchEntries
     *            Whethere needs to fetch all associated entries?
     * @return a ticket of type(com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket) with given serialNo.
     */
    BaseTicket enquiry(Context<?> respCtx, BaseTicket clientTicket, boolean fetchEntries) throws ApplicationException;

    /**
     * Enquiry quick pick numbers.
     */
    QPEnquiryDto enquiryQP(Context<?> respCtx, QPEnquiryDto dto) throws ApplicationException;
}
