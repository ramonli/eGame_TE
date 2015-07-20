package com.mpos.lottery.te.gameimpl.digital.prize.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.digital.prize.DigitalWinningItem;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.service.impl.AbstractPrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeAmount;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

public class DigitalPrizeService extends AbstractPrizeService {

    @Override
    public GameType supportedGameType() {
        return GameType.DIGITAL;
    }

    // private DigitalWinningStatistics lookupWinningStatistics(BaseGameInstance gameInstance,
    // BaseWinningItem winningItem) throws ApplicationException {
    // // should cache winning statistics
    // DigitalWinningStatistics stat = this.getBaseWinningStatisticsDao()
    // .getByGameDrawAndPrizeLevelAndVersion(DigitalWinningStatistics.class, gameInstance.getId(),
    // winningItem.getPrizeLevel(), gameInstance.getVersion());
    // if (stat == null) {
    // throw new ApplicationException(SystemException.CODE_INTERNAL_SERVER_ERROR,
    // "can NOT find DIGITAL winning statistics(gameDrawId=" + gameInstance.getId()
    // + ",prizeLeve=" + winningItem.getPrizeLevel() + ",version="
    // + gameInstance.getVersion() + ").");
    // }
    // return stat;
    // }

    @Override
    protected PrizeAmount lookupAnnouncedPrizeAmount(BaseWinningItem winningItem, BaseTicket ticket)
            throws ApplicationException {
        // DigitalWinningStatistics stat = this.lookupWinningStatistics(ticket.getGameInstance(),
        // winningItem);
        // return new PrizeAmount(stat.getPrizeAmount(), stat.getTaxAmount(), stat.getActualAmount());
        DigitalWinningItem lfnWinningItem = (DigitalWinningItem) winningItem;
        return new PrizeAmount(lfnWinningItem.getPrizeAmount(), lfnWinningItem.getTaxAmount(),
                lfnWinningItem.getActualAmount());
    }
}
