package com.mpos.lottery.te.gameimpl.magic100;

import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.User;

import java.math.BigDecimal;

public class Magic100DomainMocker {

    public static Magic100Ticket mockTicket() {
        Magic100Ticket ticket = new Magic100Ticket();
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("300.0"));
        ticket.setPIN("!!!!");
        ticket.setGameInstance(mockGameInstance());

        Magic100Entry entry = new Magic100Entry();
        entry.setSelectNumber("PLAY");
        entry.setBetOption(BaseEntry.BETOPTION_SINGLE);
        entry.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        entry.setTotalBets(3);
        ticket.getEntries().add(entry);

        User user = new User();
        user.setMobile("138000138000");
        ticket.setUser(user);

        return ticket;
    }

    public static Magic100GameInstance mockGameInstance() {
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        gameInstance.setGameId("LK-1");
        gameInstance.setNumber("001");
        return gameInstance;
    }
}
