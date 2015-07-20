package com.mpos.lottery.te.gameimpl.lfn.prize.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.lfn.prize.LfnWinningItem;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.service.impl.AbstractPrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeAmount;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

public class LfnPrizeService extends AbstractPrizeService {

    @Override
    public GameType supportedGameType() {
        return GameType.LFN;
    }

    @Override
    protected PrizeAmount lookupAnnouncedPrizeAmount(BaseWinningItem winningItem, BaseTicket ticket)
            throws ApplicationException {
        LfnWinningItem lfnWinningItem = (LfnWinningItem) winningItem;
        return new PrizeAmount(lfnWinningItem.getPrizeAmount(), lfnWinningItem.getTaxAmount(),
                lfnWinningItem.getActualAmount());
    }
}
