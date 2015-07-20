package com.mpos.lottery.te.gameimpl.raffle;

import com.mpos.lottery.te.gameimpl.raffle.game.RaffleGameInstance;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.User;

import java.math.BigDecimal;

public class RaffleDomainMocker {

    public static RaffleGameInstance gameInstance() {
        RaffleGameInstance gameInstance = new RaffleGameInstance();
        gameInstance.setGameId("RA-1");
        gameInstance.setNumber("11002");
        return gameInstance;
    }

    public static RaffleTicket ticket() {
        RaffleTicket ticket = new RaffleTicket();
        ticket.setTotalAmount(new BigDecimal("300.0"));
        ticket.setMultipleDraws(1);
        // ticket.setGameInstance(gameInstance());
        ticket.setPIN("!!!!");

        ticket.setGameInstance(gameInstance());

        User user = new User();
        user.setMobile("13413148084");
        ticket.setUser(user);

        // assemble entry
        BaseEntry entry = new BaseEntry();
        entry.setBetOption(1);
        entry.setSelectNumber("PLAY");
        entry.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        entry.setTotalBets(3);

        ticket.getEntries().add(entry);
        return ticket;
    }
}
