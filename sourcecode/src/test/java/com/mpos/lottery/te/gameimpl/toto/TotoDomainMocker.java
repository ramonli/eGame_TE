package com.mpos.lottery.te.gameimpl.toto;

import com.mpos.lottery.te.gameimpl.toto.domain.ToToEntry;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToGameInstance;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class TotoDomainMocker {

    public static ToToTicket mockTicket() {
        ToToTicket ticket = new ToToTicket();
        ticket.setPIN("123456");
        ticket.setTotalAmount(new BigDecimal("600.0"));
        ticket.setMultipleDraws(1);
        // set game instance
        ticket.setGameInstance(mockGameDraw());

        // set toto entries
        List<BaseEntry> entries = new LinkedList<BaseEntry>();
        ToToEntry entry2 = new ToToEntry();
        entry2.setEntryNo("2");
        entry2.setInputChannel(1);
        entry2.setSelectNumber("0|1|3,1,3|0");
        entries.add(entry2);
        ticket.setEntries(entries);

        return ticket;
    }

    public static ToToGameInstance mockGameDraw() {
        // set game instance
        ToToGameInstance gameDraw = new ToToGameInstance();
        gameDraw.setNumber("20090402");
        gameDraw.setGameId("GAME-TOTO-1");
        gameDraw.setOmrGameSet("G2");
        return gameDraw;
    }
}
