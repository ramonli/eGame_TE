package com.mpos.lottery.te.merchant.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.web.CreditTransferDto;
import com.mpos.lottery.te.merchant.web.OperatorTopupDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;

/**
 * Interfaces for managing sale and payout balance.
 * 
 * @author Ramon
 * 
 */
public interface CreditService {
    /**
     * There are 2 kind of credit level: sale credit level and payout credit level.
     * <p>
     * sale and sale cancellation will affect sale credit level, however there is a special case, if player pay by
     * credit card and the amount will be collected by merchant's account, no sale credit level will be affected. Below
     * illustrates the work flow of how sale(pay by cash) affects the sale credit level.
     * <h1>Sale Credit Level</h1>
     * <ol>
     * <li>Check the credit type of operator.
     * <dl>
     * <dt>- credit type is DEFINITIVE VALUE</dt>
     * <dd>1. Simple deduct the operator's sale credit level and return</dd>
     * <dt>- credit type is USE PARENT</dt>
     * <dd>1. Go to check the credit type of parent merchent.
     * </dl>
     * </li>
     * <li>Check the credit type of parent merchant if needed.
     * <dl>
     * <dt>- credit type is DEFINITIVE VALUE</dt>
     * <dd>1. Simple deduct the merchant's sale credit level and return</dd>
     * <dt>- credit type is USE PARENT</dt>
     * <dd>1. Go to check the credit type of parent merchant until found a DEFINITIVE merchant.
     * </dl>
     * </li>
     * </ol>
     * <h1>Payout Credit Level</h1>
     * <ol>
     * <li>Check the credit type of operator.
     * <dl>
     * <dt>- credit type is DEFINITIVE VALUE</dt>
     * <dd>1. Simple topup the operator's payout credit level and return</dd>
     * <dt>- credit type is USE PARENT</dt>
     * <dd>1. Go to check the credit type of parent merchent.
     * </dl>
     * </li>
     * <li>Check the credit type of parent merchant if needed.
     * <dl>
     * <dt>- credit type is DEFINITIVE VALUE</dt>
     * <dd>1. Simple topup the merchant's payout credit level and return</dd>
     * <dt>- credit type is USE PARENT</dt>
     * <dd>1. Go to check the credit type of parent merchant until found a DEFINITIVE merchant.
     * </dl>
     * </li>
     * </ol>
     * 
     * @param operatorId
     *            The identifier of operator.
     * @param merchantId
     *            The identifier of leaf merchant.
     * @param amount
     *            The sale amount.
     * @param gameId
     *            calculate commission based on given game. If null, no need to calculate commission.
     * @param isRestore
     *            Does restore the credit level?
     * @param isSaleStyle
     *            Is the transaction which cause re-calculate of credit level is sale style, or payout style??
     *            sale/cancel is sale style, payout/reversal(or its corresponding cancel by transaction) is payment
     *            style.
     * @param isSoldByCreditCard
     *            whether the sale is paid by credit card. only function for sale.
     * @return whose balance has been updated? either operator or merchant(if credit type of operator is use-parent).
     *         null will be returned if no balance updated.
     * @throws ApplicationException
     *             when encounter any business exception.
     * @deprecated Will be removed soon, replaced by {@link com.mpos.lottery.te.merchant.service.balance.BalanceService}
     *             and {@link com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService}
     */
    Object credit(String operatorId, long merchantId, BigDecimal amount, String gameId, boolean isRestore,
            boolean isSaleStyle, boolean isSoldByCreditCard) throws ApplicationException;

    /**
     * A alternate method for credit update. If the transaction which will affect credit level has no relationship with
     * game, such as cashout, call this method then.
     * 
     * @param operatorId
     *            Update which operator's balance.
     * @param merchantId
     *            The merchant who contains the given operator.
     * @param amount
     *            The amount which will be applied to current sale or payout balance.
     * @param isRestore
     *            topup the sale/payout balance if true, deduct if false.
     * @param isSaleStyle
     *            operator on sale balance if true, otherwise on payout balance.
     * @return whose balance has been updated? either operator or merchant(if credit type of operator is
     *         use-parent).null will be returned if no balance updated.
     * @deprecated Will be removed soon, replaced by {@link com.mpos.lottery.te.merchant.service.balance.BalanceService}
     *             and {@link com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService} *
     */
    Object credit(String operatorId, long merchantId, BigDecimal amount, boolean isRestore, boolean isSaleStyle)
            throws ApplicationException;

    /**
     * A alternate method for credit update. If the transaction which will affect credit level has no relationship with
     * game, such as cashout, call this method then.
     * 
     * @param operatorId
     *            Update which operator's balance.
     * @param merchantId
     *            The merchant who contains the given operator.
     * @param amount
     *            The amount which will be applied to current sale or payout balance.
     * @param isRestore
     *            topup the sale/payout balance if true, deduct if false.
     * @param isSaleStyle
     *            operator on sale balance if true, otherwise on payout balance.
     * @param transaction
     *            te transaction
     * @return whose balance has been updated? either operator or merchant(if credit type of operator is
     *         use-parent).null will be returned if no balance updated.
     */
    Object credit(String operatorId, long merchantId, BigDecimal amount, String gameId, boolean isRestore,
            boolean isSaleStyle, boolean isSoldByCreditCard, Transaction transaction) throws ApplicationException;

    /**
     * There are 2 kinds of credit: sale and payout. Sale and its corresponding cancellation will affect sale credit,
     * payout/validation and its corresponding cancellation will affect payout credit.
     * <p>
     * Refer to {@link CreditService#credit(String, long, BigDecimal, String, boolean, boolean, boolean)} for more
     * information.
     */
    CreditTransferDto transferCredit(Context reqCtx, Context respCtx, CreditTransferDto dto)
            throws ApplicationException;

    /**
     * Topup the sale balance of given operator. If the credit type of operator is 'use-parent', system will lookup its
     * parent till find a merchant whose credit type is definitive value.
     * 
     * @param respCtx
     *            The context of current topup transaction.
     * @param topupDto
     *            The topup request, the field 'amount' must be provided. If no field 'operatorId' provided, system will
     *            retrieve operator from 'X-Operator-Id'.
     * @return The balance of the operator or merchant whose sale balance has been topuped.
     * @throws ApplicationException
     *             when encounter biz exception.
     */
    OperatorTopupDto topupOperator(Context respCtx, OperatorTopupDto topupDto) throws ApplicationException;

}
