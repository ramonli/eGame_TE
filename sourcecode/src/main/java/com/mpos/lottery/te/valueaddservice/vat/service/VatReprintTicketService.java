package com.mpos.lottery.te.valueaddservice.vat.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.web.VatReprintTicketReqDto;

public interface VatReprintTicketService {
    /**
     * reprint the raffle ticket.
     */
    VatSaleTransaction raffleReprintTicket(Context reqCtx, Context respCtx, VatReprintTicketReqDto dto)
            throws ApplicationException;

    VatSaleTransaction MagicReprintTicket(Context reqCtx, Context respCtx, VatReprintTicketReqDto dto)
            throws ApplicationException;
}
