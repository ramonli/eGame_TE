package com.mpos.lottery.te.valueaddservice.vat.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.TicketEnquiryService;
import com.mpos.lottery.te.gamespec.sale.web.QPEnquiryDto;
import com.mpos.lottery.te.port.Context;

import org.springframework.stereotype.Service;

@Service
public class VatTicketEnquiryService implements TicketEnquiryService {

    @Override
    public GameType supportedGameType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseTicket enquiry(Context<?> respCtx, BaseTicket clientTicket, boolean fetchEntries)
            throws ApplicationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QPEnquiryDto enquiryQP(Context<?> respCtx, QPEnquiryDto dto) throws ApplicationException {
        // non-supported
        return null;
    }

}
