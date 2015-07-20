package com.mpos.lottery.te.valueaddservice.vat.web;

import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.valueaddservice.vat.VAT;

public class VatTicket extends BaseTicket {
    private static final long serialVersionUID = 4925122672961442792L;

    public VatTicket() {
    }

    public VatTicket(RaffleTicket ticket) {

    }

    public VatTicket(Magic100Ticket ticket) {

    }

    /**
     * Convert a <code>VatTicket</code> instance into <code>RaffleTicket</code>. Be remind that the instance of
     * <code>VatTicket</code> is simply a request DTO.
     */
    public RaffleTicket toRaffleTicket() {
        return null;
    }

    /**
     * Convert a <code>VatTicket</code> instance into <code>Magic100Ticket</code>. Be remind that the instance of
     * <code>VatTicket</code> is simply a request DTO.
     */
    public Magic100Ticket toMagic100Ticket() {
        return null;
    }

    /**
     * A DTO of game instance, will be converted into other specific game instance, such as Raffle game instance.
     */
    private BaseGameInstance gameInstaneDto;
    private VAT vat;

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstaneDto;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstaneDto = gameInstance;
    }

    public VAT getVat() {
        return vat;
    }

    public void setVat(VAT vat) {
        this.vat = vat;
    }

}
