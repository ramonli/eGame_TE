package com.mpos.lottery.te.gameimpl.raffle.prize.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.raffle.game.RaffleGameInstance;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.service.impl.AbstractPrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeAmount;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.util.LinkedList;
import java.util.List;

public class RafflePrizeService extends AbstractPrizeService {

    @Override
    public GameType supportedGameType() {
        return GameType.RAFFLE;
    }

    /**
     * For RAFFLE game, the prize information must be retrieve from bd_prize_level
     */
    @Override
    protected void assembleNormalPrizeItem(PrizeDto prize, PrizeItemDto prizeItem, BaseTicket ticket,
            List<? extends BaseWinningItem> winningItems) throws ApplicationException {
        // generate prize level items
        for (BaseWinningItem winningItem : winningItems) {
            // check if the winning item is valid
            if (!winningItem.isValid()) {
                throw new ApplicationException(SystemException.CODE_CANCELED_WINNING_TICKET,
                        "can not payout a invalid winning item(id=" + winningItem.getId() + ").");
            }

            // lookup the prize level definition
            PrizeLevelItemDto winningDef = this.assemblePrizeItemFromBasePrizeLeveDef(
                    ((RaffleGameInstance) prizeItem.getGameInstance()).getPrizeLogicId(), winningItem, prizeItem);
            this.updatePrizeStatPerDraw(prize, prizeItem, winningDef);
        }
    }

    @Override
    protected PrizeAmount lookupAnnouncedPrizeAmount(BaseWinningItem winningItem, BaseTicket ticket)
            throws ApplicationException {
        // no need this method any more
        return null;
    }

    @Override
    protected List<? extends BaseEntry> lookupTicketEntriess(String serialNo) throws ApplicationException {
        // for raffle game, entries don't matter.
        return new LinkedList<BaseEntry>();
    }

}
