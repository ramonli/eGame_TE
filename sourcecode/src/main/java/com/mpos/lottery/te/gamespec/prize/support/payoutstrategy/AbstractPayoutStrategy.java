package com.mpos.lottery.te.gamespec.prize.support.payoutstrategy;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;
import com.mpos.lottery.te.gamespec.prize.PrizeLevel;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelObjectItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractPayoutStrategy implements PayoutStrategy {
    // SPRING DEPENDENCIES INJECTION
    private UUIDService uuidService;
    private PayoutDao payoutDao;
    private PayoutDetailDao payoutDetailDao;

    @Override
    public final void reverse(Context respCtx, GameType supportedGameType, List<? extends BaseTicket> hostTickets,
            Transaction targetTrans) throws ApplicationException {
        List<Payout> payouts = this.getPayoutDao().getByTransactionAndStatus(targetTrans.getId(), Payout.STATUS_PAID);
        // update payouts and calculate credit amount
        for (Payout payout : payouts) {
            payout.setStatus(Payout.STATUS_REVERSED);
        }
        this.getPayoutDao().update(payouts);

        this.doReversal(respCtx, supportedGameType, hostTickets);
    }

    protected void doReversal(Context respCtx, GameType supportedGameType, List<? extends BaseTicket> hostTickets)
            throws ApplicationException {
        // template method
    }

    @Override
    public final void payout(Context<?> respCtx, GameType supportedGameType, PrizeDto prize,
            List<? extends BaseTicket> hostTickets) throws ApplicationException {
        this.doPayout(respCtx, supportedGameType, prize, hostTickets);

        // generate payout records
        List<Payout> payouts = new LinkedList<Payout>();
        // handle prize payout
        for (PrizeItemDto prizeItem : prize.getPrizeItems()) {
            /*
             * If a ticket entry(multiple ticket entries for a multi-draw ticket) win nothing(neither cash prize nor
             * object prize), don't write payout record
             */
            if (prizeItem.getPrizeLevelItems().size() == 0) {
                continue;
            }

            Payout payout = this.assemblePayout(respCtx, prizeItem, prize);
            payouts.add(payout);
        }
        this.getPayoutDao().insert(payouts);
        for (Payout payout : payouts) {
            if (payout.getPayoutDetails() != null && payout.getPayoutDetails().size() > 0) {
                this.getPayoutDetailDao().insert(payout.getPayoutDetails());
            }
        }

    }

    /**
     * A subclass should override this method to implement required logic of a specific payout mode.
     */
    protected void doPayout(Context<?> respCtx, GameType supportedGameType, PrizeDto prize,
            List<? extends BaseTicket> hostTickets) throws ApplicationException {
        // template method
    }

    /**
     * Generate payout record based on a given game instance.
     */
    protected Payout assemblePayout(Context respCtx, PrizeItemDto prizeItem, PrizeDto prize)
            throws ApplicationException {
        Payout payout = new Payout();
        payout.setGameInstanceId(prizeItem.getGameInstance().getId());
        payout.setGameId(prizeItem.getGameInstance().getGame().getId());
        payout.setId(this.getUuidService().getGeneralID());
        payout.setCreateTime(new Date());
        payout.setUpdateTime(payout.getCreateTime());
        payout.setTransaction(respCtx.getTransaction());
        payout.setTicketSerialNo(prize.getWinningTicket().getSerialNo());
        payout.setType(Payout.TYPE_WINNING);
        payout.setValid(true);
        payout.setStatus(Payout.STATUS_PAID);
        payout.setTotalAmount(prizeItem.getActualAmount());
        payout.setBeforeTaxTotalAmount(prizeItem.getPrizeAmount());
        payout.setType(Payout.TYPE_WINNING);
        payout.setInputChannel(prize.getWinningTicket().getPayoutInputChannel());

        payout.setOperatorId(respCtx.getTransaction().getOperatorId());
        payout.setDevId((int) respCtx.getTransaction().getDeviceId());
        payout.setMerchantId((int) respCtx.getTransaction().getMerchantId());

        this.customizeAssemblePayout(respCtx, prizeItem, prize, payout);
        this.assemblePayoutDetail(respCtx, payout, prizeItem, prize);

        return payout;
    }

    /**
     * Generate payout details. Based on a single 'prizeItem', single cash payout detail record(sum the cash of all
     * prizeLevelItems) and multiple object payout detail records(if exists) will be generated.
     */
    protected void assemblePayoutDetail(Context<?> respCtx, Payout payout, PrizeItemDto prizeItem, PrizeDto prize)
            throws ApplicationException {
        // generate cash payout detail first
        PayoutDetail cashPayoutDetail = new PayoutDetail();
        cashPayoutDetail.setId(this.getUuidService().getGeneralID());
        cashPayoutDetail.setPayoutId(payout.getId());
        cashPayoutDetail.setPayoutType(PrizeLevel.PRIZE_TYPE_CASH);

        // generate object payout detail then
        for (PrizeLevelItemDto prizeLevelItem : prizeItem.getPrizeLevelItems()) {
            for (PrizeLevelObjectItemDto objectPrizeLevelItem : prizeLevelItem.getPrizeLevelObjectItems()) {
                PayoutDetail objectPayoutDetail = new PayoutDetail();
                objectPayoutDetail.setId(this.getUuidService().getGeneralID());
                objectPayoutDetail.setPayoutId(payout.getId());
                objectPayoutDetail.setPayoutType(PrizeLevel.PRIZE_TYPE_OBJECT);
                objectPayoutDetail.setObjectId(objectPrizeLevelItem.getObjectId());
                objectPayoutDetail.setObjectName(objectPrizeLevelItem.getObjectName());
                // set prize_amount to total_amount
                objectPayoutDetail.setPrizeAmount(objectPrizeLevelItem.getPrice());
                // set actual_amount to cash_amount
                objectPayoutDetail.setActualAmount(objectPrizeLevelItem.getPrice().subtract(
                        objectPrizeLevelItem.getTaxAmount()));
                objectPayoutDetail.setNumberOfObject(objectPrizeLevelItem.getNumberOfObject()
                        * prizeLevelItem.getNumberOfPrizeLevel());
                objectPayoutDetail.setUpdateTime(respCtx.getTransaction().getCreateTime());
                objectPayoutDetail.setCreateTime(respCtx.getTransaction().getCreateTime());
                objectPayoutDetail.setCreateBy(payout.getOperatorId());
                objectPayoutDetail.setUpdateBy(payout.getOperatorId());
                payout.getPayoutDetails().add(objectPayoutDetail);
            }
        }

        // set prize_amount to total_amount
        cashPayoutDetail.setPrizeAmount(prizeItem.getPrizeAmount());
        // set actual_amount to cash_amount
        cashPayoutDetail.setActualAmount(prizeItem.getActualAmount());
        // for cash prize, this value will always be 1.
        cashPayoutDetail.setNumberOfObject(1);
        cashPayoutDetail.setUpdateTime(respCtx.getTransaction().getCreateTime());
        cashPayoutDetail.setCreateTime(respCtx.getTransaction().getCreateTime());
        cashPayoutDetail.setCreateBy(payout.getOperatorId());
        cashPayoutDetail.setUpdateBy(payout.getOperatorId());
        payout.getPayoutDetails().add(cashPayoutDetail);
    }

    /**
     * A template method for a gametype specific implementation to customize the process of assembling a
     * <code>Payout</code> instance.
     */
    protected void customizeAssemblePayout(Context respCtx, PrizeItemDto prizeItem, PrizeDto prize, Payout payout) {

    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public PayoutDetailDao getPayoutDetailDao() {
        return payoutDetailDao;
    }

    public void setPayoutDetailDao(PayoutDetailDao payoutDetailDao) {
        this.payoutDetailDao = payoutDetailDao;
    }

}
