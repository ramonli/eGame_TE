package com.mpos.lottery.te.gameimpl.raffle.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.AbstractTicketService;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * For Raffle game, player doesn't need to pick numbers, however s/he can request to buy multiple bets which will give
 * him more winning opportunity.
 */
public class RaffleTicketService extends AbstractTicketService {
    private Log logger = LogFactory.getLog(RaffleTicketService.class);

    @Override
    public GameType supportedGameType() {
        return GameType.RAFFLE;
    }

    @Override
    protected List<? extends BaseEntry> lookupEntries(Context respCtx, BaseTicket clientTicket)
            throws ApplicationException {
        return null;
    }

    @Override
    protected void customizeAssembleTicket(BaseTicket generatedTicket, BaseTicket clientTicket) {
        // no need to persist Entries
        generatedTicket.getEntries().clear();
    }

}
