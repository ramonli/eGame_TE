package com.mpos.lottery.te.gameimpl.raffle.prize;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.support.payoutstrategy.NewPrintPayoutStrategy;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.util.ArrayList;
import java.util.List;

public class RaffleNewPrintPayoutStrategy extends NewPrintPayoutStrategy {

    @Override
    protected List<BaseEntry> assembleEntries(GameType supportedGameType, BaseTicket oldHostTicket, String newSerialNo)
            throws ApplicationException {
        // no entries for raffle game
        return new ArrayList<BaseEntry>();
    }

}
