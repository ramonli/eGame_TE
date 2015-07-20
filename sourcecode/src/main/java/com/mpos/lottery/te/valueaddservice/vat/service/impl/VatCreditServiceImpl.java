package com.mpos.lottery.te.valueaddservice.vat.service.impl;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.web.VatTransferDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;
import com.mpos.lottery.te.valueaddservice.vat.VatOperatorBalance;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatOperatorBalanceDao;
import com.mpos.lottery.te.valueaddservice.vat.service.VatCreditService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

public class VatCreditServiceImpl extends AbstractReversalOrCancelStrategy implements VatCreditService {
    private Log logger = LogFactory.getLog(VatCreditServiceImpl.class);

    private OperatorDao operatorDao;
    private VatOperatorBalanceDao vatOperatorBalanceDao;
    @PersistenceContext(unitName = "lottery_te")
    private EntityManager entityManager;
    private MerchantService merchantService;
    private UUIDService uuidManager;

    @Override
    public VatTransferDto vatTransferCredit(Context respCtx, VatTransferDto dto) throws ApplicationException {
        // lookup from operator
        Operator fromOperator = this.getOperatorDao().findByLoginNameForUpdate(dto.getFromOperatorLoginName());
        if (fromOperator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "No operator found by login name("
                    + dto.getFromOperatorLoginName() + ").");
        }
        // Merchant fromMerchant = this.getMerchantService().getMerchantByOperator(fromOperator.getId(),
        // true);
        VatOperatorBalance fromVatOperatorBalance = this.getVatOperatorBalanceDao().findByOperatorIdForUpdate(
                fromOperator.getId());
        if (fromVatOperatorBalance == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR,
                    "No vat_operator_balance found by login name(" + dto.getFromOperatorLoginName() + ").");
        }

        // lookup to operator
        Operator toOperator = this.getOperatorDao().findByLoginNameForUpdate(dto.getToOperatorLoginName());
        if (toOperator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "No operator found by login name("
                    + dto.getToOperatorLoginName() + ").");
        }
        // Merchant toMerchant = this.getMerchantService().getMerchantByOperator(toOperator.getId(),
        // true);
        VatOperatorBalance toVatOperatorBalance = this.getVatOperatorBalanceDao().findByOperatorIdForUpdate(
                toOperator.getId());
        if (toVatOperatorBalance == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR,
                    "No vat_operator_balance found by login name(" + dto.getToOperatorLoginName() + ").");
        }

        Object fromTarget = null;
        Object toTarget = null;
        if (VatTransferDto.CREDITTYPE_SALE == dto.getCreditType()) {
            // deduct from from-operator
            fromTarget = this.doCredit(fromVatOperatorBalance, dto.getAmount(), null, false, true);
            // topup
            toTarget = this.doCredit(toVatOperatorBalance, dto.getAmount(), null, true, true);
        } else if (VatTransferDto.CREDITTYPE_PAYOUT == dto.getCreditType()) {
            // deduct from from-operator
            fromTarget = this.doCredit(fromVatOperatorBalance, dto.getAmount(), null, false, false);
            // topup
            toTarget = this.doCredit(toVatOperatorBalance, dto.getAmount(), null, true, false);
        } else {
            throw new SystemException("Unsupported credit type of [" + VatTransferDto.class + "]:"
                    + dto.getCreditType());
        }

        // initialize a transfer log

        // assemble response DTO
        this.assembleCreditBalance(dto, fromTarget, toTarget);

        return dto;
    }

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        return false;
    }

    // ----------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------

    protected Object doCredit(VatOperatorBalance vatOperatorBalance, BigDecimal amount, String gameId,
            boolean isRestore, boolean isSaleStyle) throws ApplicationException {
        // if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == operator.getCreditType()) {
        /**
         * If try to refresh a entity before flush state changes into underlying database, the changes of entity will be
         * lost.
         */
        this.getEntityManager().flush();
        /**
         * Refresh entity to latest state of underlying database and lock it.
         */
        this.getEntityManager().refresh(vatOperatorBalance, LockModeType.PESSIMISTIC_READ);
        if (logger.isDebugEnabled()) {
            logger.debug("The current vatOperatorBalance credit level(before transaction) of " + vatOperatorBalance
                    + " is " + "saleCreditLevel:" + vatOperatorBalance.getSaleBalance() + ",payoutCreditLevel:"
                    + vatOperatorBalance.getPayoutBalance());
        }
        if (isSaleStyle) {
            if (!isRestore) { // sale
                BigDecimal tmpCreditLevel = vatOperatorBalance.getSaleBalance().subtract(amount);
                if (tmpCreditLevel.compareTo(new BigDecimal("0")) < 0) {
                    throw new ApplicationException(SystemException.CODE_EXCEED_CREDITLIMIT,
                            "The balance of sale credit level(" + vatOperatorBalance.getSaleBalance() + ") of "
                                    + vatOperatorBalance + " isn't enough for sale(amount=" + amount + ").");
                }
                vatOperatorBalance.setSaleBalance(tmpCreditLevel);
            } else { // sale cancellation
                vatOperatorBalance.setSaleBalance(vatOperatorBalance.getSaleBalance().add(amount));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("The new sale credit level of " + vatOperatorBalance + " is "
                        + vatOperatorBalance.getSaleBalance());
            }
        } else {
            if (isRestore) { // payout
                vatOperatorBalance.setPayoutBalance(vatOperatorBalance.getPayoutBalance().add(amount));
            } else { // payout cancellation.
                BigDecimal tmpCreditLevel = vatOperatorBalance.getPayoutBalance().subtract(amount);
                if (tmpCreditLevel.compareTo(new BigDecimal("0")) < 0) {
                    throw new ApplicationException(SystemException.CODE_EXCEED_CREDITLIMIT,
                            "The balance of payout credit level(" + vatOperatorBalance.getPayoutBalance() + ") of "
                                    + vatOperatorBalance + " isn't enough for payout(amount=" + amount + ").");
                }
                vatOperatorBalance.setPayoutBalance(tmpCreditLevel);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("The new payout credit level of " + vatOperatorBalance + " is "
                        + vatOperatorBalance.getPayoutBalance());
            }
        }
        this.getVatOperatorBalanceDao().update(vatOperatorBalance);
        return vatOperatorBalance;
        // else if (Merchant.CREDIT_TYPE_USE_PARENT == operator.getCreditType()) {
        // if (logger.isDebugEnabled())
        // logger.debug("Ignore credit level calculation, as the credit type of " + operator
        // + " is USE PARENT... check its parent merchant");
        // return this.creditMerchant(merchant, amount, gameId, isRestore, isSaleStyle,
        // isSoldByCrecitCard);
        // }
        // else {
        // logger.info("Ignore calculation of credit level, as the credit type of " + operator + " is "
        // + operator.getCreditType());
        // return null;
        // }
    }

    protected void assembleCreditBalance(VatTransferDto dto, Object fromTarget, Object toTarget) {
        if (VatTransferDto.CREDITTYPE_SALE == dto.getCreditType()) {
            if (fromTarget instanceof VatOperatorBalance) {
                dto.setCreditBalanceOfFromOperator(((VatOperatorBalance) fromTarget).getSaleBalance());
            }

            if (toTarget instanceof VatOperatorBalance) {
                dto.setCreditBalanceOfToOperator(((VatOperatorBalance) toTarget).getSaleBalance());
            }
        } else if (VatTransferDto.CREDITTYPE_PAYOUT == dto.getCreditType()) {
            if (fromTarget instanceof VatOperatorBalance) {
                dto.setCreditBalanceOfFromOperator(((VatOperatorBalance) fromTarget).getPayoutBalance());
            }

            if (toTarget instanceof VatOperatorBalance) {
                dto.setCreditBalanceOfToOperator(((VatOperatorBalance) toTarget).getPayoutBalance());
            }
        }
    }

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

    public VatOperatorBalanceDao getVatOperatorBalanceDao() {
        return vatOperatorBalanceDao;
    }

    public void setVatOperatorBalanceDao(VatOperatorBalanceDao vatOperatorBalanceDao) {
        this.vatOperatorBalanceDao = vatOperatorBalanceDao;
    }

}
