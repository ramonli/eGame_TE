package com.mpos.lottery.te.valueaddservice.vat.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.service.CompositeTicketService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;
import com.mpos.lottery.te.valueaddservice.vat.VatOperatorBalance;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatOperatorBalanceDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatSaleTransactionDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VatReversalOrCancelStrategy extends AbstractReversalOrCancelStrategy {
    private static Log logger = LogFactory.getLog(VatReversalOrCancelStrategy.class);
    private VatOperatorBalanceDao vatOperatorBalanceDao;
    private VatSaleTransactionDao vatSaleTransactionDao;
    private CompositeTicketService raffleTicketService;
    private CompositeTicketService magic100TicketService;

    /**
     * Cancel a VAT sale. In general it will cancel all operations made by sale, it includes:
     * <ul>
     * <li>Mark VAT sale transaction as invalid.</li>
     * <li>Restore the VAT balance of operator.</li>
     * <li>Cancel the ticket sale if needed.</li>
     * </ul>
     */
    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        // invalid vat sale transaction
        VatSaleTransaction vatSaleTrans = this.getVatSaleTransactionDao().findByTransaction(targetTrans.getId());
        vatSaleTrans.setStatus(VatSaleTransaction.STATUS_INVALID);

        // restore operator's sale balance
        VatOperatorBalance operatorBalance = this.getVatOperatorBalanceDao().findByOperatorIdForUpdate(
                targetTrans.getOperatorId());
        operatorBalance.setSaleBalance(operatorBalance.getSaleBalance().subtract(vatSaleTrans.getVatTotalAmount()));
        this.getVatOperatorBalanceDao().update(operatorBalance);

        boolean isCancelDecline = false;
        if (vatSaleTrans.getTicketSerialNo() != null) {
            // call ticket service respectively
            if (GameType.RAFFLE.getType() == vatSaleTrans.getGameType()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The business type of trans(id=" + targetTrans.getId()
                            + ") is B2B, go to cancel raffle sale.");
                }
                isCancelDecline = this.getRaffleTicketService().cancelOrReverse(respCtx, targetTrans);
            } else if (GameType.LUCKYNUMBER.getType() == vatSaleTrans.getGameType()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The business type of trans(id=" + targetTrans.getId()
                            + ") is B2C, go to cancel magic100 sale.");
                }
                isCancelDecline = this.getMagic100TicketService().cancelOrReverse(respCtx, targetTrans);
            } else {
                throw new IllegalStateException("Unsupported business type:" + vatSaleTrans.getBusinessType());
            }
        }

        return isCancelDecline;
    }

    // ---------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ---------------------------------------------------------------------

    public VatOperatorBalanceDao getVatOperatorBalanceDao() {
        return vatOperatorBalanceDao;
    }

    public CompositeTicketService getRaffleTicketService() {
        return raffleTicketService;
    }

    public void setRaffleTicketService(CompositeTicketService raffleTicketService) {
        this.raffleTicketService = raffleTicketService;
    }

    public CompositeTicketService getMagic100TicketService() {
        return magic100TicketService;
    }

    public void setMagic100TicketService(CompositeTicketService magic100TicketService) {
        this.magic100TicketService = magic100TicketService;
    }

    public void setVatOperatorBalanceDao(VatOperatorBalanceDao vatOperatorBalanceDao) {
        this.vatOperatorBalanceDao = vatOperatorBalanceDao;
    }

    public VatSaleTransactionDao getVatSaleTransactionDao() {
        return vatSaleTransactionDao;
    }

    public void setVatSaleTransactionDao(VatSaleTransactionDao vatSaleTransactionDao) {
        this.vatSaleTransactionDao = vatSaleTransactionDao;
    }

}
