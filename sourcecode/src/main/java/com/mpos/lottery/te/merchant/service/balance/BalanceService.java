package com.mpos.lottery.te.merchant.service.balance;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;

/**
 * This service will define those interfaces which be responsible of handling the balance maintenance, no commission
 * will be calculated here, Refer to {@link com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService}
 * for commission maintenance.
 * 
 * @author Ramon
 */
public interface BalanceService {
    public static final int BALANCE_TYPE_SALE = 1;
    public static final int BALANCE_TYPE_PAYOUT = 2;
    public static final int BALANCE_TYPE_COMMISSION = 3;
    public static final int BALANCE_TYPE_CASHOUT = 4;

    /**
     * Maintain the balance of all supported balance types. At present there are following kinds of balance: sale,
     * payout, cashout and commission.
     * <p/>
     * All balance will be maintained in the same style, will explain it by sale balance.
     * <p/>
     * There are several transactions may affect sale balance, for example sale, credit transfer and the corresponding
     * cancellations. However there is a special case for sale transaction, if player pay by credit card, that says
     * operator won't get the cash in pocket, money will be transferred from player's credit card account into lottery
     * operator company's account directly, in this case no sale balance will be affected.
     * <p/>
     * Below illustrates the work flow of how sale(pay by cash) affects the sale balance.
     * <h1>Sale Balance</h1>
     * <ol>
     * <li>Check the credit type of operator.
     * <dl>
     * <dt>- credit type is DEFINITIVE VALUE</dt>
     * <dd>1. Simple deduct the operator's sale balance and return</dd>
     * <dt>- credit type is USE PARENT</dt>
     * <dd>1. Go to check the credit type of parent merchent.
     * </dl>
     * </li>
     * <li>Check the credit type of parent merchant if needed.
     * <dl>
     * <dt>- credit type is DEFINITIVE VALUE</dt>
     * <dd>1. Simple deduct the merchant's sale balance and return</dd>
     * <dt>- credit type is USE PARENT</dt>
     * <dd>1. Go to check the credit type of parent merchant until found a DEFINITIVE merchant.
     * </dl>
     * </li>
     * </ol>
     * 
     * @param respCtx
     *            The current transaction context.
     * @param targetTrans
     *            The target transaction. For a cancellation transaction, it should be the transaction that has been
     *            cancelled, however for normal transaction, it is itself.The following components must be set:
     *            totalAmont(it will be used to calculate balance and commission if needed)
     * @param balanceType
     *            Which balance will be operated? refer to BALANCE_TYPE.XXX.
     * @param operatorId
     *            Whose balance will be operated? Think about the 'credit transfer' transaction.
     * @param isTopup
     *            Topup or deduct the balance.
     * @return Whose balance has been updated? either operator or merchant(if credit type of operator is use-parent).
     *         null will be returned if no balance updated.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    Object balance(Context<?> respCtx, Transaction targetTrans, int balanceType, String operatorId, boolean isTopup)
            throws ApplicationException;

    /**
     * Maintain the balance of all supported balance types. This service will simply call
     * {@link #balance(Context, Transaction, int, String, boolean)} with <code>targetTrans</code> as
     * <code>respCtx.getTransaction()</code>.
     * 
     * @param respCtx
     *            The current transaction context.The following components must be set: transaction.totalAmont(it will
     *            be used to calculate commission if needed)
     * @param balanceType
     *            Which balance will be operated? refer to BALANCE_TYPE.XXX.
     * @param operatorId
     *            Whose balance will be operated? Think about the 'credit transfer' transaction.
     * @param isTopup
     *            Topup or deduct the balance.
     * @return Whose balance has been updated? either operator or merchant(if credit type of operator is use-parent).
     *         null will be returned if no balance updated.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    Object balance(Context<?> respCtx, int balanceType, String operatorId, boolean isTopup) throws ApplicationException;

    /**
     * Lookup the operator/merchant node which will be responsible of balance maintenance. In generate it will be the
     * operator, however if operator's credit type is 'use parent', this service will start to lookup its parent
     * merchants(from leaf merchant till distributor), and return the first merchant whose credit type is 'definite
     * value'.
     * <p/>
     * This interface will lock the operator/merchant node and compare the <code>transAmount</code> with current sale
     * balance of operator/merchant node, if <code>transAmount</code> is less than current sale balance, exception
     * <code>SystemException.CODE_EXCEED_CREDITLIMIT</code> will be thrown out.
     * <p/>
     * In some cases, null will be returned:
     * <ul>
     * <li>Configure the operator to ignore credit.</li>
     * <li>No 'definite value' merchant found, however from business point of view, this case should never happen.</li>
     * </ul>
     * 
     * @param operatorId
     *            THe id of operator.
     * @param leafMerchantId
     *            The id of leaf merchant of operator.
     * @param transAmount
     *            The amount of transaction.
     * @return a operator or merchant object. null will be returned if no need to care the balance.
     * @throws ApplicationException
     *             if encounter any business exception.
     */
    Object lockAndVerifySaleBalance(String operatorId, long leafMerchantId, BigDecimal transAmount)
            throws ApplicationException;

    // /**
    // * Maintain the balance of given operator/merchant note. This interface should corporate with {@Link
    // * #lookupBalanceNode(String)}.
    // *
    // * @param balanceNode
    // * The operator/merchant whose balance should be managed.
    // * @throws ApplicationException
    // * if encounter any business exception.
    // */
    // void calBalance(Object balanceNode, boolean isTopup) throws ApplicationException;
}
