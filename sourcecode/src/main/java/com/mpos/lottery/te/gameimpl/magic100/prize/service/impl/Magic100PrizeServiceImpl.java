package com.mpos.lottery.te.gameimpl.magic100.prize.service.impl;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.WinningStatistics;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.service.LuckyNumberService;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;
import com.mpos.lottery.te.gamespec.prize.service.impl.AbstractPrizeService;
import com.mpos.lottery.te.gamespec.prize.web.PrizeAmount;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.web.PayoutLevelAllowRequest;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Magic100PrizeServiceImpl extends AbstractPrizeService {
    private LuckyNumberService luckyNumberService;
    private Log logger = LogFactory.getLog(Magic100PrizeServiceImpl.class);

    @Override
    public GameType supportedGameType() {
        return GameType.LUCKYNUMBER;
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

    /**
     * Calculate prize information of a given ticket.
     */
    @Override
    protected PrizeDto assemblePrize(Context<?> respCtx, BaseTicket clientTicket,
            List<? extends BaseTicket> hostTickets, boolean isPrizeEnquiry) throws ApplicationException {
        BaseTicket soldTicket = hostTickets.get(0);
        // initial PrizeDto
        PrizeDto prize = this.newPrize(clientTicket, hostTickets);

        // ** Handle normal draw
        if (clientTicket.isWinning()) {
            StopWatch sw = new Log4JStopWatch();
            // handle ticket one by one, that says one game instance by one game
            // instance
            for (BaseTicket t : hostTickets) {
                prize.getPaidTickets().add(t);
                // assemble normal prize information
                List<? extends BaseWinningItem> winningItems = new ArrayList();

                PrizeItemDto prizeItem = new PrizeItemDto();
                prizeItem.setGameInstance(t.getGameInstance());
                prizeItem.getGameInstance().setGameId(t.getGameInstance().getGame().getId());
                prizeItem.setType(PrizeGroupItem.GROUP_TYPE_NORMAL_DRAW);
                this.assembleNormalPrizeItem(prize, prizeItem, t, winningItems);

                // if calculate tax per ticket, do it here
                this.calculateTaxBasedOnPerTicket(prize, t, prizeItem);
            }
            sw.stop("Assemble_Normal_Prize", "Assemble normal draw prize of ticket(" + clientTicket.getSerialNo() + ")");
        }
        prize.setPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        // prize.setPayoutMode(this.lookupOperationParameter(soldTicket.getGameInstance().getGame()).getPayoutMode());
        if (BaseOperationParameter.PAYOUTMODE_REFUND == prize.getPayoutMode()) {
            prize.setActualAmount(prize.getActualAmount().add(prize.getReturnAmount()));
        }

        return prize;
    }

    @Override
    protected void assembleNormalPrizeItem(PrizeDto prize, PrizeItemDto prizeItem, BaseTicket ticket,
            List<? extends BaseWinningItem> winningItems) throws ApplicationException {
        List<Magic100Entry> entrys = (List<Magic100Entry>) this.lookupTicketEntriess(ticket.getSerialNo());
        // generate prize level items
        for (Magic100Entry magic100Entry : entrys) {
            // check if the winning item is valid
            if (!magic100Entry.isWinning()) {
                continue;
            }
            BigDecimal totalTaxAmountOfLevel = this.getTaxService().tax(magic100Entry.getPrizeAmount(),
                    ticket.getGameInstance().getGame().getId());
            prize.setPrizeAmount(prize.getPrizeAmount().add(magic100Entry.getPrizeAmount()));
            //
            if (Game.TAXMETHOD_PAYOUT == ticket.getGameInstance().getGame().getTaxMethod()) {
                prize.setTaxAmount(prize.getTaxAmount().add(totalTaxAmountOfLevel));
            }
            prize.setActualAmount(prize.getActualAmount().add(
                    magic100Entry.getPrizeAmount().subtract(totalTaxAmountOfLevel)));

            prizeItem.setActualAmount(prize.getActualAmount());
            prizeItem.setPrizeAmount(prize.getPrizeAmount());
            prizeItem.setTaxAmount(prize.getTaxAmount());
            prizeItem.setGameInstance(ticket.getGameInstance());

            // Provide a default value, call public interface insert data support
            PrizeLevelItemDto prizeLevelItem = new PrizeLevelItemDto();
            prizeItem.getPrizeLevelItems().add(prizeLevelItem);
            prize.getPrizeItems().add(prizeItem);

        }
    }

    @Override
    protected PayoutLevelAllowRequest[] assemblePayoutLevelAllowRequests(Context<?> respCtx, PrizeDto prize,
            BaseTicket ticket) {
        return null;
    }

    /**
     * @return luckyNumberService
     */
    public LuckyNumberService getLuckyNumberService() {
        return luckyNumberService;
    }

    /**
     * @param luckyNumberService
     */
    public void setLuckyNumberService(LuckyNumberService luckyNumberService) {
        this.luckyNumberService = luckyNumberService;
    }

}
