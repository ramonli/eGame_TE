package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import com.google.gson.Gson;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantVIRNPrizeDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantVIRNPrize;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class ValidateReversalStrategy extends AbstractReversalOrCancelStrategy {
    private InstantTicketDao instantTicketDao;
    private InstantVIRNPrizeDao instantVIRNPrizeDao;
    private PayoutDao payoutDao;
    private MerchantService merchantService;
    private CreditService creditService;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    @Override
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType());
    }

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        String ticketSerialNo = targetTrans.getTicketSerialNo();
        InstantTicket ticket = this.getInstantTicketDao().getBySerialNo(ticketSerialNo);
        if (ticket == null) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET,
                    "can NOT find instant ticket with serialNo='" + ticketSerialNo + "'.");
        }
        // reverse ticket.status = active
        this.reverseTicket(ticket);
        // reverse payout record
        this.reversePayout(targetTrans);
        // check if need to reverse VIRN
        this.reverseVIRN(ticket);

        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            // cancelled balance transaction
            BalanceTransactions tempBalanceTransactions = new Gson().fromJson(targetTrans.getTransMessage()
                    .getRequestMsg(), BalanceTransactions.class);
            BalanceTransactions operatorBalanceTransactions = balanceTransactionsDao.assembleBalanceTransactions(
                    respCtx, tempBalanceTransactions.getTransactionAmount());
            operatorBalanceTransactions.setCommissionAmount(BalanceTransactions.ZERO.subtract(tempBalanceTransactions
                    .getCommissionAmount()));
            operatorBalanceTransactions.setTransactionAmount(BalanceTransactions.ZERO.subtract(tempBalanceTransactions
                    .getTransactionAmount()));
            operatorBalanceTransactions.setCommissionRate(tempBalanceTransactions.getCommissionRate());
            balanceTransactionsDao.updateBalanceTransactionsStatusByteTransactionId(targetTrans.getTransMessage()
                    .getTransactionId());
            balanceTransactionsDao.insert(operatorBalanceTransactions);
        }
        return false;
    }

    // ----------------------------------------------------
    // PROTECTED METHODS
    // ----------------------------------------------------

    protected void reverseTicket(InstantTicket hostTicket) {
        hostTicket.setStatus(InstantTicket.STATUS_ACTIVE);
        this.getInstantTicketDao().update(hostTicket);
    }

    protected void reversePayout(Transaction trans) throws ApplicationException {
        List<Payout> payouts = this.getPayoutDao().getByTransactionAndStatus(trans.getId(), Payout.STATUS_PAID);
        BigDecimal credit = new BigDecimal("0");
        for (Payout payout : payouts) {
            credit = credit.add(payout.getTotalAmount());
            payout.setStatus(Payout.STATUS_REVERSED);
            this.getPayoutDao().update(payout);
        }
        // if
        // (MLotteryContext.getInstance().getSysConfiguration().isRestoreCreditLevelWhenPayout())
        // {
        // restore credit level
        this.getCreditService().credit(trans.getOperatorId(), trans.getMerchantId(), credit, trans.getGameId(), false,
                false, false, trans);
    }

    protected void reverseVIRN(InstantTicket hostTicket) {
        int validationType = hostTicket.getGameDraw().getValidationType();
        if (validationType == InstantGameDraw.VALIDATION_TYPE_VIRN) {
            InstantVIRNPrize virnPrize = this.getInstantVIRNPrizeDao().getByGameDrawAndVIRN(
                    hostTicket.getGameDraw().getId(), hostTicket.getTicketXOR3());
            if (virnPrize != null) {
                virnPrize.setValidated(false);
                this.getInstantVIRNPrizeDao().update(virnPrize);
            }
        }
    }

    // ----------------------------------------------------
    // SPRING INDENPENDICES INJECTION
    // ----------------------------------------------------

    public InstantTicketDao getInstantTicketDao() {
        return instantTicketDao;
    }

    public void setInstantTicketDao(InstantTicketDao instantTicketDao) {
        this.instantTicketDao = instantTicketDao;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public InstantVIRNPrizeDao getInstantVIRNPrizeDao() {
        return instantVIRNPrizeDao;
    }

    public void setInstantVIRNPrizeDao(InstantVIRNPrizeDao instantVIRNPrizeDao) {
        this.instantVIRNPrizeDao = instantVIRNPrizeDao;
    }

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

}
