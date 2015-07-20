package com.mpos.lottery.te.gameimpl.toto.service.impl;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToEntry;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToWinningStatistics;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.service.impl.AbstractPrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeAmount;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.util.LinkedList;
import java.util.List;

public class ToToPayoutServiceImpl extends AbstractPrizeService {

    @Override
    public GameType supportedGameType() {
        return GameType.TOTO;
    }

    private ToToWinningStatistics lookupWinningStatistics(BaseGameInstance gameInstance, BaseWinningItem winningItem)
            throws ApplicationException {
        // should cache winning statistics
        ToToWinningStatistics stat = this.getBaseWinningStatisticsDao().getByGameDrawAndPrizeLevelAndVersion(
                ToToWinningStatistics.class, gameInstance.getId(), winningItem.getPrizeLevel(),
                gameInstance.getVersion());
        if (stat == null) {
            throw new ApplicationException(SystemException.CODE_INTERNAL_SERVER_ERROR,
                    "can NOT find TOTO winning statistics(gameDrawId=" + gameInstance.getId() + ",prizeLeve="
                            + winningItem.getPrizeLevel() + ",version=" + gameInstance.getVersion() + ").");
        }
        return stat;
    }

    @Override
    protected PrizeAmount lookupAnnouncedPrizeAmount(BaseWinningItem winningItem, BaseTicket ticket)
            throws ApplicationException {
        ToToWinningStatistics stat = this.lookupWinningStatistics(ticket.getGameInstance(), winningItem);
        return new PrizeAmount(stat.getPrizeAmount(), stat.getTaxAmount(), stat.getActualAmount());
    }

    /**
     * TOTO game doesn't care about payoutMOde
     */
    @Override
    protected BaseOperationParameter lookupOperationParameter(Game game) throws ApplicationException {
        BaseOperationParameter opParams = new BaseOperationParameter();
        // the value of payout mode doesn't matter.
        opParams.setPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        return opParams;
    }

    /**
     * NO entry for TOTO game.
     */
    protected List<? extends BaseEntry> lookupTicketEntriess(String serialNo) throws ApplicationException {
        return new LinkedList<ToToEntry>();
    }
}
