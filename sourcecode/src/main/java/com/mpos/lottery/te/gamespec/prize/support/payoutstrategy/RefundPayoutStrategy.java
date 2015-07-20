package com.mpos.lottery.te.gamespec.prize.support.payoutstrategy;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RefundPayoutStrategy extends AbstractPayoutStrategy {
    private BaseTicketDao baseTicketDao;

    @Override
    public void doPayout(Context<?> respCtx, GameType supportedGameType, PrizeDto prize,
            List<? extends BaseTicket> hostTickets) throws ApplicationException {
        // update tickets
        for (BaseTicket ticket : prize.getPaidTickets()) {
            /**
             * If internal payout, only when all game instances are payout-started, this operation will be allowed.
             */
            if (respCtx.isInternalCall()) {
                // won't update the status and updatetime of ticket, otherwise
                // it may affect activity report.
                ticket.setAbsorptionPaid(true);
            } else {
                ticket.setStatus(BaseTicket.STATUS_PAID);
                ticket.setUpdateTime(respCtx.getTransaction().getCreateTime());
            }
        }
        this.getBaseTicketDao().update(prize.getPaidTickets());

        List<Payout> returnPayouts = new LinkedList<Payout>();
        for (BaseTicket ticket : prize.getFutureTickets()) {
            ticket.setStatus(BaseTicket.STATUS_RETURNED);
            ticket.setCountInPool(false);
            returnPayouts.add(this.customizeReturnedPayout(respCtx, prize, ticket));
            ticket.setUpdateTime(respCtx.getTransaction().getCreateTime());
        }
        this.getBaseTicketDao().update(prize.getFutureTickets());
        this.getPayoutDao().insert(returnPayouts);
    }

    /**
     * TEmplate method for subclass to override the process of marking 'returned' ticket.
     */
    protected Payout customizeReturnedPayout(Context<?> respCtx, PrizeDto prize, BaseTicket ticket)
            throws ApplicationException {
        Payout payout = new Payout();
        payout.setGameInstanceId(ticket.getGameInstance().getId());
        payout.setGameId(ticket.getGameInstance().getGame().getId());
        payout.setId(this.getUuidService().getGeneralID());
        payout.setCreateTime(new Date());
        payout.setUpdateTime(payout.getCreateTime());
        payout.setTransaction(respCtx.getTransaction());
        payout.setTicketSerialNo(ticket.getSerialNo());
        payout.setValid(true);
        payout.setStatus(Payout.STATUS_PAID);
        payout.setTotalAmount(ticket.getTotalAmount());
        payout.setBeforeTaxTotalAmount(ticket.getTotalAmount());
        payout.setType(Payout.TYPE_RETURN);
        payout.setInputChannel(prize.getWinningTicket().getPayoutInputChannel());

        payout.setOperatorId(respCtx.getTransaction().getOperatorId());
        payout.setDevId((int) respCtx.getTransaction().getDeviceId());
        payout.setMerchantId((int) respCtx.getTransaction().getMerchantId());

        return payout;
    }

    @Override
    public void confirm(Context respCtx, GameType supportedGameType, List<? extends BaseTicket> hostTickets)
            throws ApplicationException {
        // do nothing
    }

    @Override
    protected void doReversal(Context respCtx, GameType supportedGameType, List<? extends BaseTicket> hostTickets)
            throws ApplicationException {
        for (BaseTicket ticket : hostTickets) {
            ticket.setStatus(BaseTicket.STATUS_ACCEPTED);
            ticket.setCountInPool(true);
            this.customizeReversePayoutTicket(respCtx, ticket);
            ticket.setUpdateTime(new Date());
        }
        this.getBaseTicketDao().update(hostTickets);
    }

    protected void customizeReversePayoutTicket(Context<?> respCtx, BaseTicket ticket) {
        // template method
    }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

}
