package com.mpos.lottery.te.gameimpl.lfn;

import com.mpos.lottery.te.gameimpl.lfn.game.LfnGameInstance;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnEntry;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnTicket;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.math.BigDecimal;

public class LfnDomainMocker {

    public static LfnTicket mockTicket() {
        LfnTicket ticket = new LfnTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("2600.0"));

        // assemble entries
        LfnEntry entry1 = new LfnEntry();
        entry1.setSelectNumber("5");
        entry1.setBetOption(LfnEntry.BETOPTION_N1);
        entry1.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry1.setEntryAmount(new BigDecimal("100.0"));
        ticket.getEntries().add(entry1);
        LfnEntry entry2 = new LfnEntry();
        entry2.setSelectNumber("2,7,13,24,25,36");
        entry2.setBetOption(LfnEntry.BETOPTION_P3);
        entry2.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry2.setEntryAmount(new BigDecimal("1200.0"));
        ticket.getEntries().add(entry2);

        ticket.setGameInstance(mockGameInstance());
        return ticket;
    }

    public static LfnGameInstance mockGameInstance() {
        LfnGameInstance gameInstance = new LfnGameInstance();
        gameInstance.setGameId("LFN-1");
        gameInstance.setNumber("11002");
        return gameInstance;
    }
}
