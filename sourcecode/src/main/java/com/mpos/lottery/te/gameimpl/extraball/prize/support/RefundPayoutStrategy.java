package com.mpos.lottery.te.gameimpl.extraball.prize.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.util.Date;

public class RefundPayoutStrategy extends AbstractPayoutStrategy {

    @Override
    public String key() {
        return Game.TYPE_UNDEF + "-" + BaseOperationParameter.PAYOUTMODE_REFUND;
    }

    public void doUpdateTicket(Prize prize) {
        // when confirm payout, the status of future tickets will be marked as
        // returned.

        // for (BaseTicket ticket : prize.getFutureTickets()) {
        // ticket.setStatus(BaseTicket.STATUS_RETURNED);
        // ticket.setUpdateTime(new Date());
        // this.getBaseTicketDao().update(ticket);
        // }
    }

    /**
     * Handle the future tickets
     */
    @Override
    protected void handleFutureTickets(Context<?> respCtx, Prize prize) throws ApplicationException {
        for (BaseTicket ticket : prize.getFutureTickets()) {
            // generate returned payout
            Payout payout = new Payout();
            payout.setGameInstanceId(ticket.getGameInstance().getId());
            payout.setId(this.getUuidService().getGeneralID());
            payout.setCreateTime(new Date());
            payout.setUpdateTime(payout.getCreateTime());
            payout.setTransaction(respCtx.getTransaction());
            payout.setTicketSerialNo(prize.getWinningTicket().getSerialNo());
            payout.setTotalAmount(ticket.getTotalAmount());
            payout.setBeforeTaxTotalAmount(ticket.getTotalAmount());
            payout.setType(Payout.TYPE_RETURN);
            payout.setValid(true);
            payout.setStatus(Payout.STATUS_PAID);
            payout.setOperatorId(respCtx.getTransaction().getOperatorId());
            payout.setDevId((int) respCtx.getTransaction().getDeviceId());
            payout.setMerchantId((int) respCtx.getTransaction().getMerchantId());
            payout.setGameId(ticket.getGameInstance().getGame().getId());

            this.getPayoutDao().insert(payout);
        }
    }
}
