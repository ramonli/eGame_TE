package com.mpos.lottery.te.valueaddservice.vat.web;

import com.mpos.lottery.te.gamespec.sale.DummyTicket;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;

/**
 * For generate representation of client, mainly XML mapping.
 * 
 * @author Ramon
 * 
 */
public class VatSaleTransactionDto extends VatSaleTransaction {
    private static final long serialVersionUID = 4119136103587938724L;
    private DummyTicket ticketDto;

    public DummyTicket getTicketDto() {
        return ticketDto;
    }

    public void setTicketDto(DummyTicket ticketDto) {
        this.ticketDto = ticketDto;
    }

}
