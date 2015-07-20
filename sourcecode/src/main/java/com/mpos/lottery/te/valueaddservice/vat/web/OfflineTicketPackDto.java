package com.mpos.lottery.te.valueaddservice.vat.web;

import com.mpos.lottery.te.valueaddservice.vat.VAT;

/**
 * 
 * @author terry
 */
public class OfflineTicketPackDto {
    private VAT vat;

    private VatRefNoPackDto vatRefNoPackDto;

    private TicketPackDto ticketPackDto;

    private SelectedNumberPackDto selectedNumberPackDto;

    public VAT getVat() {
        return vat;
    }

    public void setVat(VAT vat) {
        this.vat = vat;
    }

    public VatRefNoPackDto getVatRefNoPackDto() {
        return vatRefNoPackDto;
    }

    public void setVatRefNoPackDto(VatRefNoPackDto vatRefNoPackDto) {
        this.vatRefNoPackDto = vatRefNoPackDto;
    }

    public TicketPackDto getTicketPackDto() {
        return ticketPackDto;
    }

    public void setTicketPackDto(TicketPackDto ticketPackDto) {
        this.ticketPackDto = ticketPackDto;
    }

    public SelectedNumberPackDto getSelectedNumberPackDto() {
        return selectedNumberPackDto;
    }

    public void setSelectedNumberPackDto(SelectedNumberPackDto selectedNumberPackDto) {
        this.selectedNumberPackDto = selectedNumberPackDto;
    }

}
