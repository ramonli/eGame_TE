package com.mpos.lottery.te.gameimpl.digital;

import com.mpos.lottery.te.gameimpl.digital.game.DigitalGameInstance;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalEntry;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalTicket;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.User;

import java.math.BigDecimal;

public class DigitalDomainMocker {

    public static DigitalTicket mockTicket() {
        DigitalTicket ticket = new DigitalTicket();
        ticket.setMultipleDraws(2);
        ticket.setPIN("!!!");
        ticket.setTotalAmount(new BigDecimal("400.0"));

        // assemble entries
        DigitalEntry entry1 = new DigitalEntry();
        entry1.setSelectNumber("0,3,2,8");
        entry1.setBetOption(DigitalEntry.DIGITAL_BETOPTION_4D);
        entry1.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry1.setEntryAmount(new BigDecimal("60.0"));
        ticket.getEntries().add(entry1);

        DigitalEntry entry2 = new DigitalEntry();
        entry2.setSelectNumber("25");
        entry2.setBetOption(DigitalEntry.DIGITAL_BETOPTION_SUM);
        entry2.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry2.setEntryAmount(new BigDecimal("140.0"));
        ticket.getEntries().add(entry2);

        ticket.setGameInstance(mockGameInstance());

        ticket.setUser(new User("lai", "13800138000", "09812091283120123"));

        return ticket;
    }

    public static DigitalGameInstance mockGameInstance() {
        DigitalGameInstance gameInstance = new DigitalGameInstance();
        gameInstance.setGameId("FD-1");
        gameInstance.setNumber("11002");
        return gameInstance;
    }

}
