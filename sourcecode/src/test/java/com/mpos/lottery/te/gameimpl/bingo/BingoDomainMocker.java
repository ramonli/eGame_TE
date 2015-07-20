package com.mpos.lottery.te.gameimpl.bingo;

import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoEntry;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.math.BigDecimal;

public class BingoDomainMocker {

    public static BingoTicket mockTicket() {
        BingoTicket ticket = new BingoTicket();
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("300.0"));

        // assemble entries
        BingoEntry entry1 = new BingoEntry();
        entry1.setSelectNumber("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");
        entry1.setBetOption(BingoEntry.BETOPTION_SINGLE);
        entry1.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        ticket.getEntries().add(entry1);
        BingoEntry entry2 = new BingoEntry();
        entry2.setSelectNumber("21,22,23,4,5,6,7,8,9,10,11,12,13,14,15");
        entry2.setBetOption(BingoEntry.BETOPTION_SINGLE);
        entry2.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        ticket.getEntries().add(entry2);
        BingoEntry entry3 = new BingoEntry();
        entry3.setSelectNumber("31,32,33,34,5,6,7,8,9,10,11,12,13,14,15");
        entry3.setBetOption(BingoEntry.BETOPTION_SINGLE);
        entry3.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        ticket.getEntries().add(entry3);

        ticket.setGameInstance(mockGameInstance());
        return ticket;
    }

    public static BingoGameInstance mockGameInstance() {
        BingoGameInstance gameInstance = new BingoGameInstance();
        gameInstance.setGameId("BINGO-1");
        gameInstance.setNumber("11002");
        return gameInstance;
    }
}
