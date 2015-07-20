package com.mpos.lottery.te.gameimpl.union;

import com.mpos.lottery.te.gameimpl.union.game.UnionGameInstance;
import com.mpos.lottery.te.gameimpl.union.sale.UnionEntry;
import com.mpos.lottery.te.gameimpl.union.sale.UnionTicket;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class UnionDomainMocker {

    public static UnionTicket mockTicket() {
        UnionTicket ticket = new UnionTicket();
        ticket.setPIN("123456");
        ticket.setTotalAmount(new BigDecimal("800"));
        ticket.setMultipleDraws(1);
        ticket.setWinning(false);
        // set game instance
        UnionGameInstance gameDraw = new UnionGameInstance();
        gameDraw.setNumber("20090408");
        gameDraw.setGameId("GAME-UNION-111");
        ticket.setGameInstance(gameDraw);

        // set lotto entries
        List<BaseEntry> entries = new ArrayList<BaseEntry>(0);
        UnionEntry entry1 = new UnionEntry();
        entry1.setBetOption(BaseEntry.BETOPTION_SINGLE);
        entry1.setInputChannel(1);
        entry1.setSelectNumber("1,2,3,6,7,13-2,5");
        entries.add(entry1);
        UnionEntry entry2 = new UnionEntry();
        entry2.setBetOption(BaseEntry.BETOPTION_MULTIPLE);
        entry2.setInputChannel(1);
        entry2.setSelectNumber("3,11,14,16,22,25,36-2,4");
        entries.add(entry2);
        ticket.setEntries(entries);

        return ticket;
    }

    public static UnionGameInstance mockGameDraw() {
        // set game instance
        UnionGameInstance gameDraw = new UnionGameInstance();
        gameDraw.setNumber("20090408");
        gameDraw.setGameId("GAME-UNION-111");
        return gameDraw;
    }

}
