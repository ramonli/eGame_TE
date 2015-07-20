package com.mpos.lottery.te.merchant.service.impl;

import com.google.gson.Gson;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.CreditTransferLogDao;
import com.mpos.lottery.te.merchant.dao.MerchantCommissionDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.dao.OperatorMerchantDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.service.IncomeBalanceService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.web.IncomeBalanceDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

public class IncomeBalanceServiceImpl extends AbstractReversalOrCancelStrategy implements IncomeBalanceService {
    private Log logger = LogFactory.getLog(IncomeBalanceServiceImpl.class);
    private MerchantDao merchantDao;
    private MerchantCommissionDao merchantCommissionDao;
    // private PrizeGroupItemDao prizeGroupItemDao;
    private UUIDService uuidManager;
    private OperatorDao operatorDao;
    private OperatorMerchantDao operatorMerchantDao;
    private CreditTransferLogDao creditTransferLogDao;
    @PersistenceContext(unitName = "lottery_te")
    private EntityManager entityManager;
    private MerchantService merchantService;
    private UUIDService uuidService;
    private BalanceTransactionsDao balanceTransactionsDao;

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        if (targetTrans.getTransMessage() == null || targetTrans.getTransMessage().getRequestMsg() == null) {
            logger.warn("NO associated transaction message found.");
            return false;
        }
        IncomeBalanceDto dto = new Gson().fromJson(targetTrans.getTransMessage().getRequestMsg(),
                IncomeBalanceDto.class);
        Operator operator = this.getOperatorDao().findById(Operator.class, respCtx.getOperatorId());
        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "can NOT find operator by id='"
                    + respCtx.getOperatorId() + "'.");
        }

        Merchant merchant = this.getMerchantDao().findById(Merchant.class, respCtx.getMerchant().getId());
        if (merchant == null) {
            throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "can NOT find merchant by id='"
                    + respCtx.getMerchant().getId() + "'.");
        }
        BalanceTransactions balanceTransactions = balanceTransactionsDao.assembleBalanceTransactions(respCtx,
                dto.getAmount());
        balanceTransactions.setPaymentType(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY);
        balanceTransactions.setOriginalTransType(targetTrans.getType());
        operator.setUpdateTime(balanceTransactions.getUpdateTime());
        merchant.setUpdateTime(balanceTransactions.getUpdateTime());
        balanceTransactions.setTransactionAmount(dto.getAmount());
        balanceTransactions.setCommissionAmount(BalanceTransactions.ZERO.subtract(dto.getCommissionBalance()));
        Object updatedOperator = this.doBalance(dto, operator, merchant, false, true);
        if (updatedOperator == null) {
            throw new SystemException(SystemException.CODE_OPERATOR_TOPUP_IGNORED, "THe topup to operator(id="
                    + respCtx.getOperatorId() + " will be ignored.");
        }
        if (updatedOperator instanceof Operator) {
            operator = (Operator) updatedOperator;
            balanceTransactions.setOwnerId(operator.getId());
            balanceTransactions.setOwnerType(BalanceTransactions.OWNER_TYPE_OPERATOR);
        } else if (updatedOperator instanceof Merchant) {
            merchant = (Merchant) updatedOperator;
            balanceTransactions.setOwnerId(String.valueOf(merchant.getId()));
            balanceTransactions.setOwnerType(BalanceTransactions.OWNER_TYPE_MERCHANT);

        } else {
            throw new IllegalStateException("unsupported topup target type: " + updatedOperator);
        }
        balanceTransactionsDao.updateBalanceTransactionsStatusByteTransactionId(targetTrans.getTransMessage()
                .getTransactionId());
        balanceTransactionsDao.insert(balanceTransactions);
        return false;
    }

    @Override
    public IncomeBalanceDto incomeBalanceTransfer(Context respCtx, IncomeBalanceDto incomeBalanceDto)
            throws ApplicationException {
        if (logger.isDebugEnabled()) {
            logger.debug(" Income Balance (" + incomeBalanceDto + ") to operator " + respCtx.getOperatorId());
        }
        Operator operator = this.getOperatorDao().findById(Operator.class, respCtx.getOperatorId());
        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "can NOT find operator by id='"
                    + respCtx.getOperatorId() + "'.");
        }

        Merchant merchant = this.getMerchantDao().findById(Merchant.class, respCtx.getMerchant().getId());
        if (merchant == null) {
            throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "can NOT find merchant by id='"
                    + respCtx.getMerchant().getId() + "'.");
        }
        BalanceTransactions balanceTransactions = balanceTransactionsDao.assembleBalanceTransactions(respCtx,
                incomeBalanceDto.getAmount());
        balanceTransactions.setPaymentType(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY);
        operator.setUpdateTime(balanceTransactions.getUpdateTime());
        merchant.setUpdateTime(balanceTransactions.getUpdateTime());
        Object updatedOperator = this.doBalance(incomeBalanceDto, operator, merchant, true, false);

        if (updatedOperator == null) {
            throw new SystemException(SystemException.CODE_OPERATOR_TOPUP_IGNORED,
                    "The Transfer balance to operator(id=" + respCtx.getOperatorId() + " will be ignored.");
        }
        if (updatedOperator instanceof Operator) {
            operator = (Operator) updatedOperator;
            balanceTransactions.setOwnerId(operator.getId());
            balanceTransactions.setOwnerType(BalanceTransactions.OWNER_TYPE_OPERATOR);
        } else if (updatedOperator instanceof Merchant) {
            merchant = (Merchant) updatedOperator;
            balanceTransactions.setOwnerId(String.valueOf(merchant.getId()));
            balanceTransactions.setOwnerType(BalanceTransactions.OWNER_TYPE_MERCHANT);

        } else {
            throw new IllegalStateException("unsupported topup target type: " + updatedOperator);
        }
        balanceTransactionsDao.insert(balanceTransactions);

        return incomeBalanceDto;
    }

    private Object doBalance(IncomeBalanceDto incomeBalanceDto, Operator operator, Merchant merchant,
            boolean isBalanceTransfer, boolean isReverse) throws ApplicationException {
        if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == operator.getCreditType()) {
            /**
             * If try to refresh a entity before flush state changes into underlying database, the changes of entity
             * will be lost.
             */
            this.getEntityManager().flush();
            /**
             * Refresh entity to latest state of underlying database and lock it.
             */
            this.getEntityManager().refresh(operator, LockModeType.PESSIMISTIC_READ);
            if (logger.isDebugEnabled()) {
                logger.debug("The balance transfer(before transaction) of " + operator + " is " + "saleBalace:"
                        + operator.getSaleCreditLevel() + ",payoutBalace:" + operator.getPayoutCreditLevel()
                        + ",cashoutBalace:" + operator.getCashoutBalance() + ",commissionBalance:"
                        + operator.getCommisionBalance());
            }
            if (isBalanceTransfer) {
                this.calculationBalance(incomeBalanceDto, operator, merchant, BalanceTransactions.OWNER_TYPE_OPERATOR);
            }
            if (isReverse) {
                this.calculationCancelledBalance(incomeBalanceDto, operator, merchant,
                        BalanceTransactions.OWNER_TYPE_OPERATOR);
            }
            this.operatorDao.update(operator);
            return operator;

        } else if (Merchant.CREDIT_TYPE_USE_PARENT == operator.getCreditType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignore credit level calculation, as the credit type of " + operator
                        + " is USE PARENT... check its parent merchant");
            }
            return this.doBalanceMerchant(incomeBalanceDto, operator, merchant, isBalanceTransfer, isReverse);
        } else {
            logger.info("Ignore calculation of credit level, as the credit type of " + operator + " is "
                    + operator.getCreditType());
            return null;
        }
    }

    private Object doBalanceMerchant(IncomeBalanceDto incomeBalanceDto, Operator operator, Merchant merchant,
            boolean isBalanceTransfer, boolean isReverse) throws ApplicationException {
        if (Merchant.CREDIT_TYPE_DEFINITIVEVALUE == merchant.getCreditType()) {
            /**
             * If try to refresh a entity before flush state changes into underlying database, the changes of entity
             * will be lost.
             */
            this.getEntityManager().flush();
            /**
             * Refresh entity to latest state of underlying database and lock it.
             */
            this.getEntityManager().refresh(merchant, LockModeType.PESSIMISTIC_READ);
            if (logger.isDebugEnabled()) {
                logger.debug("The balance transfer(before transaction) of " + merchant + " is " + "saleBalace:"
                        + merchant.getSaleCreditLevel() + ",payoutBalace:" + merchant.getPayoutCreditLevel()
                        + ",cashoutBalace:" + merchant.getCashoutBalance() + ",commissionBalance:"
                        + merchant.getCommisionBalance());
            }
            if (isBalanceTransfer) {
                this.calculationBalance(incomeBalanceDto, operator, merchant, BalanceTransactions.OWNER_TYPE_MERCHANT);
            }
            if (isReverse) {
                this.calculationCancelledBalance(incomeBalanceDto, operator, merchant,
                        BalanceTransactions.OWNER_TYPE_MERCHANT);
            }
            this.getMerchantDao().update(merchant);
            return merchant;
        } else if (Merchant.CREDIT_TYPE_USE_PARENT == merchant.getCreditType()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignore balance calculation, as the credit type of" + merchant
                        + " is USE PARENT... check its parent merchant.");
            }
            // invoke method recursively
            if (merchant.getParentMerchant() == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "can NOT find parent of merchant(id='"
                        + merchant.getId() + "').");
            }
            Merchant parent = this.getMerchantDao().findByIdForUpdate(merchant.getParentMerchant().getId());
            return this.doBalanceMerchant(incomeBalanceDto, operator, parent, isBalanceTransfer, isReverse);
        } else {
            logger.info("Ignore calculation of balance, as the credit type of " + merchant + " is "
                    + merchant.getCreditType());
            return null;
        }
    }

    /**
     * calculation Balance balance debit order:1.commission balance->2.payout balance->3.cash out balance.
     * 
     * @param incomeBalanceDto
     * @param operator
     * @param merchant
     * @param type
     *            1:operator,2:merchnat
     * @throws ApplicationException
     */
    private void calculationBalance(IncomeBalanceDto incomeBalanceDto, Operator operator, Merchant merchant, int type)
            throws ApplicationException {
        if (type == BalanceTransactions.OWNER_TYPE_OPERATOR) {
            incomeBalanceDto.setSaleBalance(incomeBalanceDto.getAmount().add(operator.getSaleCreditLevel()));
            incomeBalanceDto.setPayoutBalance(operator.getPayoutCreditLevel());
            incomeBalanceDto.setCashoutBalance(operator.getCashoutBalance());
            incomeBalanceDto.setCommissionBalance(operator.getCommisionBalance());
        } else if (type == BalanceTransactions.OWNER_TYPE_MERCHANT) {
            incomeBalanceDto.setSaleBalance(incomeBalanceDto.getAmount().add(merchant.getSaleCreditLevel()));
            incomeBalanceDto.setPayoutBalance(merchant.getPayoutCreditLevel());
            incomeBalanceDto.setCashoutBalance(merchant.getCashoutBalance());
            incomeBalanceDto.setCommissionBalance(merchant.getCommisionBalance());
        }
        BigDecimal amount = incomeBalanceDto.getAmount();
        BigDecimal payoutBalance = incomeBalanceDto.getPayoutBalance();
        BigDecimal cashoutBalance = incomeBalanceDto.getCashoutBalance();
        BigDecimal commissionBalance = incomeBalanceDto.getCommissionBalance().compareTo(new BigDecimal("0")) < 0
                ? new BigDecimal("0")
                : incomeBalanceDto.getCommissionBalance();
        incomeBalanceDto.setAddSaleAmount(amount);
        if (commissionBalance.compareTo(amount) >= 0) {
            incomeBalanceDto.setDeductCommissionAmount(amount);
            if (commissionBalance.compareTo(new BigDecimal("0")) > 0) {
                incomeBalanceDto.setCommissionBalance(commissionBalance.subtract(amount));
            }

        } else if (commissionBalance.add(payoutBalance).compareTo(amount) >= 0) {
            incomeBalanceDto.setDeductPayoutAmount(amount.subtract(commissionBalance));
            incomeBalanceDto.setPayoutBalance(commissionBalance.add(payoutBalance).subtract(amount));
            if (commissionBalance.compareTo(new BigDecimal("0")) > 0) {
                incomeBalanceDto.setDeductCommissionAmount(commissionBalance);
                incomeBalanceDto.setCommissionBalance(new BigDecimal("0"));
            }
        } else if (commissionBalance.add(payoutBalance).add(cashoutBalance).compareTo(amount) >= 0) {
            incomeBalanceDto.setDeductPayoutAmount(payoutBalance);
            incomeBalanceDto.setDeductCashoutAmount(amount.subtract(commissionBalance).subtract(payoutBalance));

            incomeBalanceDto.setPayoutBalance(new BigDecimal("0"));
            incomeBalanceDto.setCashoutBalance(commissionBalance.add(payoutBalance).add(cashoutBalance)
                    .subtract(amount));
            if (commissionBalance.compareTo(new BigDecimal("0")) > 0) {
                incomeBalanceDto.setDeductCommissionAmount(commissionBalance);
                incomeBalanceDto.setCommissionBalance(new BigDecimal("0"));
            }
        } else {
            throw new ApplicationException(SystemException.CODE_INSUFFICIENT_BALANCE, "Transfer amount[" + amount
                    + "] insufficient . commissionBalance[" + incomeBalanceDto.getCommissionBalance().doubleValue()
                    + "],payoutBalance[" + payoutBalance.doubleValue() + "],cashoutBalance["
                    + cashoutBalance.doubleValue() + "]");
        }

        // set operator or mechant balance
        if (type == BalanceTransactions.OWNER_TYPE_OPERATOR) {
            operator.setSaleCreditLevel(incomeBalanceDto.getSaleBalance());
            operator.setPayoutCreditLevel(incomeBalanceDto.getPayoutBalance());
            operator.setCashoutBalance(incomeBalanceDto.getCashoutBalance());
            operator.setCommisionBalance(incomeBalanceDto.getCommissionBalance());
        } else if (type == BalanceTransactions.OWNER_TYPE_MERCHANT) {
            merchant.setSaleCreditLevel(incomeBalanceDto.getSaleBalance());
            merchant.setPayoutCreditLevel(incomeBalanceDto.getPayoutBalance());
            merchant.setCashoutBalance(incomeBalanceDto.getCashoutBalance());
            merchant.setCommisionBalance(incomeBalanceDto.getCommissionBalance());
        }

    }

    private void calculationCancelledBalance(IncomeBalanceDto incomeBalanceDto, Operator operator, Merchant merchant,
            int type) throws ApplicationException {
        // set operator or mechant balance
        if (type == BalanceTransactions.OWNER_TYPE_OPERATOR) {
            operator.setSaleCreditLevel(operator.getSaleCreditLevel().subtract(incomeBalanceDto.getAddSaleAmount()));
            operator.setPayoutCreditLevel(operator.getPayoutCreditLevel().add(incomeBalanceDto.getDeductPayoutAmount()));
            operator.setCashoutBalance(operator.getCashoutBalance().add(incomeBalanceDto.getDeductCashoutAmount()));
            if (incomeBalanceDto.getCommissionBalance().compareTo(new BigDecimal("0")) >= 0) {
                operator.setCommisionBalance(operator.getCommisionBalance().add(
                        incomeBalanceDto.getDeductCommissionAmount()));
            }
        } else if (type == BalanceTransactions.OWNER_TYPE_MERCHANT) {
            merchant.setSaleCreditLevel(merchant.getSaleCreditLevel().subtract(incomeBalanceDto.getAddSaleAmount()));
            merchant.setPayoutCreditLevel(merchant.getPayoutCreditLevel().add(incomeBalanceDto.getDeductPayoutAmount()));
            merchant.setCashoutBalance(merchant.getCashoutBalance().add(incomeBalanceDto.getDeductCashoutAmount()));
            if (incomeBalanceDto.getCommissionBalance().compareTo(new BigDecimal("0")) >= 0) {
                merchant.setCommisionBalance(merchant.getCommisionBalance().add(
                        incomeBalanceDto.getDeductCommissionAmount()));
            }
        }
    }

    // ----------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public MerchantCommissionDao getMerchantCommissionDao() {
        return merchantCommissionDao;
    }

    public void setMerchantCommissionDao(MerchantCommissionDao merchantCommissionDao) {
        this.merchantCommissionDao = merchantCommissionDao;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public OperatorMerchantDao getOperatorMerchantDao() {
        return operatorMerchantDao;
    }

    public void setOperatorMerchantDao(OperatorMerchantDao operatorMerchantDao) {
        this.operatorMerchantDao = operatorMerchantDao;
    }

    public CreditTransferLogDao getCreditTransferLogDao() {
        return creditTransferLogDao;
    }

    public void setCreditTransferLogDao(CreditTransferLogDao creditTransferLogDao) {
        this.creditTransferLogDao = creditTransferLogDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    /**
     * @return uuidService
     */
    public UUIDService getUuidService() {
        return uuidService;
    }

    /**
     * @param uuidService
     */
    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    /**
     * @return balanceTransactionsDao
     */
    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    /**
     * @param balanceTransactionsDao
     */
    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

    public static void main(String args[]) {
        System.out.println(new BigDecimal("200.21").subtract(new BigDecimal("0.2")));
    }
}
