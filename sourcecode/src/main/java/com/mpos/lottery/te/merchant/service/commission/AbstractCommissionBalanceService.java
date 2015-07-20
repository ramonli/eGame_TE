package com.mpos.lottery.te.merchant.service.commission;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

public abstract class AbstractCommissionBalanceService implements CommissionBalanceService {
    private Log logger = LogFactory.getLog(AbstractCommissionBalanceService.class);
    // spring dependencies
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionDao;

    /**
     * Calculate the commission of transaction. Below will give a sample to explain the logic, and before that we assume
     * there is a 4-level operator/merchant hierarchy.
     * <p/>
     * <table border="1">
     * <tr>
     * <td>Level</td>
     * <td>Name</td>
     * <td>CreditType</td>
     * </tr>
     * <tr>
     * <td>#1</td>
     * <td>Distributor#1</td>
     * <td>Definite Value</td>
     * </tr>
     * <tr>
     * <td>#2</td>
     * <td>Merchant#2</td>
     * <td>Definite Value</td>
     * </tr>
     * <tr>
     * <td>#3</td>
     * <td>Merchant#2A</td>
     * <td>Definite Value</td>
     * </tr>
     * <tr>
     * <td>#4</td>
     * <td>Operator#2A1</td>
     * <td>Definite Value</td>
     * </tr>
     * </table>
     * <p/>
     * Now if operator#2A1 sold a ticket with amount $100 and the commission rate is 1%,
     * <h1>Definite Operator</h1>
     * If the credit type of operator#2A1 is 'definite value', TE will generate only a single balance log as below,
     * <table border="1">
     * <tr>
     * <td>TE_Trans_ID</td>
     * <td>Owner_ID</td>
     * <td>Trans_Amount</td>
     * <td>Commission_Amount</td>
     * </tr>
     * <tr>
     * <td>Sale_Trans_ID</td>
     * <td>Operator#2A1</td>
     * <td>100</td>
     * <td>1</td>
     * </tr>
     * </table>
     * <b>Be noted that the Owner_ID is the node who get the commission.</b>
     * <p/>
     * Besides TE will deduct the sale balance of 'operator#2A1' by $100 as well and topup commission balance of
     * 'operator#2A1' by $1. DataConsumer will care the commission of all parent nodes of 'operator#2A1'.
     * 
     * <h1>Operator Use Parent</h1>
     * If the credit type of both 'operator#2A1' and 'merchant#2A' is 'use parent', that says the sale will only affect
     * the sale balance of 'merchant#2'. In this case TE will generate 2 balance logs for 'operator#2A1' and
     * 'merchant#2', and it will only calculate commission for 'operator#2A1', DataConsumer will care the commission of
     * all parent nodes of 'operator#2A1'.
     * <table border="1">
     * <tr>
     * <td>TE_Trans_ID</td>
     * <td>Owner_ID</td>
     * <td>Trans_Amount</td>
     * <td>Commission_Amount</td>
     * </tr>
     * <tr>
     * <td>Sale_Trans_ID</td>
     * <td>Operator#2A1</td>
     * <td>100</td>
     * <td>1</td>
     * </tr>
     * <tr>
     * <td>Sale_Trans_ID</td>
     * <td>Merchant#2</td>
     * <td>100</td>
     * <td>null(dataConsumer will calculate it)</td>
     * </tr>
     * </table>
     * <p/>
     * TE will deduct the sale balance of 'merchant#2' by $100 as well, and doesn't need to maintain the commission
     * balance of 'operator#2A1', as it is 'use parent'.
     */
    @Override
    public void calCommission(Context<?> respCtx, Object operatorOrMerchant) throws ApplicationException {
        if (!MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            if (logger.isDebugEnabled()) {
                logger.debug("System has disable the commission supports, no need to calculate commission");
                return;
            }
        }
        if (operatorOrMerchant == null) {
            return;
        }
        List<CommissionUnit> commissionUnits = this.determineCommUnits(respCtx.getTransaction());
        BigDecimal totalComm = new BigDecimal("0");
        for (CommissionUnit commUnit : commissionUnits) {
            BigDecimal totalAmount = commUnit.getTransAmount();
            BigDecimal commission = SimpleToolkit.mathMultiple(commUnit.getTransAmount(), commUnit.getCommissonRate(),
                    MLotteryContext.getInstance().getInt(MLotteryContext.COMMISSION_BALANCE_PRECISION));
            totalComm = totalComm.add(commission);

            BalanceTransactions operatorLog = this.generateOperatorBalanceLog(respCtx.getTransaction(), respCtx
                    .getTransaction().getType(), totalAmount, commUnit.getCommissonRate(), commission, commUnit
                    .getGameId(), commUnit.getPaymentType(), false);
            this.getBalanceTransactionDao().insert(operatorLog);
            if (operatorOrMerchant instanceof Merchant) {
                // the credit type of operator is 'use parent', TE has to
                // generate a balance log of target merchant, however no need to
                // calculate commission of merchant.
                BalanceTransactions merchantLog = this.generateMerchantBalanceLog(respCtx.getTransaction(), respCtx
                        .getTransaction().getType(), ((Merchant) operatorOrMerchant).getId(), totalAmount, commUnit
                        .getGameId(), commUnit.getPaymentType(), false);
                this.getBalanceTransactionDao().insert(merchantLog);
            }
        }
        // that says the credit type of operator is 'definite value'.
        if (operatorOrMerchant instanceof Operator) {
            // update operator's commission balance.. TE only need to care
            // commission of operator, DataConsumer will be responsible of
            // commission of parent merchants.
            Operator operator = this.getBalanceTransactionDao().findById(Operator.class,
                    respCtx.getTransaction().getOperatorId());
            operator.setCommisionBalance(operator.getCommisionBalance().add(totalComm));
            this.getBalanceTransactionDao().update(operator);
        }
    }

    /**
     * Reverse the commission calculation. This service will
     * <ol>
     * <li>Mark all balance logs of original transaction(such as sale, payout etc) as 'invalid'.</li>
     * <li>Restore the commission balance of operator if its credit type is 'definite value'.</li>
     * <li>Generate cancellation balance log for operator and parent if operator's credit-type is 'use parent'(follow
     * the same rule as {@link #calCommission(Context, Object)}.</li>
     * </ol>
     */
    @Override
    public void cancelCommission(Context<?> respCtx, Transaction targetTrans, Object operatorOrMerchant)
            throws ApplicationException {
        if (!MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            if (logger.isDebugEnabled()) {
                logger.debug("System has disable the commission supports, no need to calculate commission");
            }
            return;
        }
        if (operatorOrMerchant == null) {
            return;
        }
        // mark all balance logs of target transaction as invalid
        this.getBalanceTransactionDao().updateBalanceTransactionsStatusByteTransactionId(targetTrans.getId());

        // lookup operator balance log of target transactions
        BigDecimal totalComm = new BigDecimal("0");
        List<BalanceTransactions> targetOperatorLogs = this.getBalanceTransactionDao().findByOwnerAndTransaction(
                targetTrans.getId(), targetTrans.getOperatorId());
        for (BalanceTransactions targetOperatorLog : targetOperatorLogs) {
            int paymentType = BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY;
            if (targetOperatorLog.getPaymentType() == BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY) {
                paymentType = BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY;
            }
            BalanceTransactions operatorCancelLog = this.generateOperatorBalanceLog(respCtx.getTransaction(),
                    targetOperatorLog.getTransactionType(), targetOperatorLog.getTransactionAmount(),
                    targetOperatorLog.getCommissionRate(), targetOperatorLog.getCommissionAmount(),
                    targetOperatorLog.getGameId(), paymentType, true);
            this.getBalanceTransactionDao().insert(operatorCancelLog);

            totalComm = totalComm.add(targetOperatorLog.getCommissionAmount());
        }

        if (operatorOrMerchant instanceof Merchant) {
            // the credit type of operator is 'use parent'
            List<BalanceTransactions> targetMerchantLogs = this.getBalanceTransactionDao().findByOwnerAndTransaction(
                    targetTrans.getId(), ((Merchant) operatorOrMerchant).getId() + "");
            for (BalanceTransactions targetMerchantLog : targetMerchantLogs) {
                int paymentType = BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY;
                if (targetMerchantLog.getPaymentType() == BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY) {
                    paymentType = BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY;
                }
                BalanceTransactions merchantCancelLog = this.generateMerchantBalanceLog(respCtx.getTransaction(),
                        targetMerchantLog.getTransactionType(), ((Merchant) operatorOrMerchant).getId(),
                        targetMerchantLog.getTransactionAmount(), targetMerchantLog.getGameId(), paymentType, true);
                this.getBalanceTransactionDao().insert(merchantCancelLog);
            }
        } else {
            // restore the commission balance of operator
            // update operator's commission balance
            Operator operator = this.getBalanceTransactionDao().findById(Operator.class,
                    respCtx.getTransaction().getOperatorId());
            operator.setCommisionBalance(operator.getCommisionBalance().subtract(totalComm));
            this.getBalanceTransactionDao().update(operator);
        }
    }

    // ----------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------

    /**
     * Sub-implementation class should implement this method to determine the <code>CommissionUnit</code>s. For some
     * transaction, such as 'IG batch validation', multiple <code>CommissionUnit</code> should be returned, as it may
     * involve multiple games, and each game will be assigned different commission rate.
     */
    protected abstract List<CommissionUnit> determineCommUnits(Transaction transaction) throws ApplicationException;

    private final BalanceTransactions generateOperatorBalanceLog(Transaction trans, int origTransType,
            BigDecimal transAmount, BigDecimal commissionRate, BigDecimal commission, String gameId, int paymentType,
            boolean isCancel) {
        return this.generateBalanceLog(trans, origTransType, trans.getOperatorId(),
                BalanceTransactions.OWNER_TYPE_OPERATOR, transAmount, commissionRate, commission, gameId, paymentType,
                isCancel);
    }

    private final BalanceTransactions generateMerchantBalanceLog(Transaction trans, int origTransType, long merchantId,
            BigDecimal transAmount, String gameId, int paymentType, boolean isCancel) {
        return this.generateBalanceLog(trans, origTransType, merchantId + "", BalanceTransactions.OWNER_TYPE_MERCHANT,
                transAmount, new BigDecimal("0"), new BigDecimal("0"), gameId, paymentType, isCancel);
    }

    private final BalanceTransactions generateBalanceLog(Transaction trans, int origTransType, String ownerId,
            int ownerType, BigDecimal transAmount, BigDecimal commissionRate, BigDecimal commission, String gameId,
            int paymentType, boolean isCancel) {
        // id of CommissonLog will be generated by underlying database
        // automatically.
        BalanceTransactions log = new BalanceTransactions();
        log.setTeTransactionId(trans.getId());
        log.setMerchantId(trans.getMerchantId());
        log.setOperatorId(trans.getOperatorId());
        log.setDeviceId(trans.getDeviceId());
        log.setOwnerId(ownerId);
        log.setOwnerType(ownerType);
        log.setPaymentType(paymentType);
        log.setTransactionType(trans.getType());
        log.setOriginalTransType(origTransType);
        // if (paymentType == BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY
        // && transAmount.compareTo(new BigDecimal("0")) > 0) {
        // transAmount = SimpleToolkit.mathMultiple(transAmount, new BigDecimal("-1"));
        // }
        // if (paymentType == BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY
        // && transAmount.compareTo(new BigDecimal("0")) < 0) {
        // transAmount = SimpleToolkit.mathMultiple(transAmount, new BigDecimal("-1"));
        // }
        log.setTransactionAmount(transAmount);
        log.setCommissionAmount(isCancel ? SimpleToolkit.mathMultiple(commission, new BigDecimal("-1"), MLotteryContext
                .getInstance().getInt(MLotteryContext.COMMISSION_BALANCE_PRECISION)) : commission);
        log.setCommissionRate(commissionRate);
        log.setStatus(BalanceTransactions.STATUS_VALID);
        log.setCreateTime(new Timestamp(new Date().getTime()));
        log.setUpdateTime(log.getCreateTime());
        log.setGameId(gameId);
        return log;
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCIES
    // ----------------------------------------------------------

    public BalanceTransactionsDao getBalanceTransactionDao() {
        return balanceTransactionDao;
    }

    public void setBalanceTransactionDao(BalanceTransactionsDao balanceTransactionDao) {
        this.balanceTransactionDao = balanceTransactionDao;
    }

}
