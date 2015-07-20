package com.mpos.lottery.te.gameimpl.extraball;

import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallEntry;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallGameInstance;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.math.BigDecimal;

public class ExtraBallDomainMocker {

    public static ExtraBallTicket mockTicket() {
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setMultipleDraws(3);
        ticket.setTotalAmount(new BigDecimal("4800.0"));
        ticket.setGameInstance(mockGameInstance());

        // add entries
        ExtraBallEntry entry1 = new ExtraBallEntry();
        entry1.setSelectNumber("12");
        entry1.setBetOption(ExtraBallEntry.BET_OPTION_NUMBER);
        entry1.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        entry1.setEntryAmount(new BigDecimal("1000.0"));
        ticket.getEntries().add(entry1);

        ExtraBallEntry entry2 = new ExtraBallEntry();
        entry2.setSelectNumber("RED");
        entry2.setBetOption(ExtraBallEntry.BET_OPTION_COLOR);
        entry2.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        entry2.setEntryAmount(new BigDecimal("300.0"));
        ticket.getEntries().add(entry2);

        ExtraBallEntry entry3 = new ExtraBallEntry();
        entry3.setSelectNumber("MEDIUM");
        entry3.setBetOption(ExtraBallEntry.BET_OPTION_RANGE);
        entry3.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        entry3.setEntryAmount(new BigDecimal("300.0"));
        ticket.getEntries().add(entry3);

        return ticket;
    }

    public static ExtraBallGameInstance mockGameInstance() {
        ExtraBallGameInstance gameInstance = new ExtraBallGameInstance();
        Game game = new Game();
        game.setId("GAME-EB");
        gameInstance.setGame(game);
        gameInstance.setNumber("20120709");
        return gameInstance;
    }

}
