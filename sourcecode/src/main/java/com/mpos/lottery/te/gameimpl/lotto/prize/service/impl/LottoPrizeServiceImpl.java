package com.mpos.lottery.te.gameimpl.lotto.prize.service.impl;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.WinningStatistics;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.service.impl.AbstractPrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeAmount;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LottoPrizeServiceImpl extends AbstractPrizeService {
    private Log logger = LogFactory.getLog(LottoPrizeServiceImpl.class);

    @Override
    public GameType supportedGameType() {
        return GameType.LOTTO;
    }

    private WinningStatistics lookupWinningStatistics(BaseGameInstance gameInstance, BaseWinningItem winningItem)
            throws ApplicationException {
        // should cache winning statistics
        WinningStatistics stat = this.getBaseWinningStatisticsDao().getByGameDrawAndPrizeLevelAndVersion(
                WinningStatistics.class, gameInstance.getId(), winningItem.getPrizeLevel(), gameInstance.getVersion());
        if (stat == null) {
            throw new ApplicationException(SystemException.CODE_INTERNAL_SERVER_ERROR,
                    "can NOT LOTTO find winning statistics(gameDrawId=" + gameInstance.getId() + ",prizeLeve="
                            + winningItem.getPrizeLevel() + ",version=" + gameInstance.getVersion() + ").");
        }
        return stat;
    }

    @Override
    protected PrizeAmount lookupAnnouncedPrizeAmount(BaseWinningItem winningItem, BaseTicket ticket)
            throws ApplicationException {
        WinningStatistics stat = this.lookupWinningStatistics(ticket.getGameInstance(), winningItem);
        return new PrizeAmount(stat.getPrizeAmount(), stat.getTaxAmount(), stat.getActualAmount());
    }
}
