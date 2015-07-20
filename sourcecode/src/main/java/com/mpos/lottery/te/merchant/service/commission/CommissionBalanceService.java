package com.mpos.lottery.te.merchant.service.commission;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

public interface CommissionBalanceService {

    /**
     * Calculate the commission and generate balance logs for a successful transaction. This service will only calculate
     * the commission of operator. If the credit type of operator is 'use parent', a balance log will be generated for
     * the parent merchant whose credit type is 'definite value' as well.
     * 
     * @param respCtx
     *            The transaction context. The following components must be set:
     *            respCtx.transaction.operatorId,respCtx.transaction.merchantId,respCtx.transaction.gameId
     * @param operatorOrMerchant
     *            THe operator or merchant(use parent) on which the balance is done.
     * @throws ApplicationException
     *             If encounter any business exception.
     */
    void calCommission(Context<?> respCtx, Object operatorOrMerchant) throws ApplicationException;

    /**
     * Reversal operation of {@link #calCommission(Context, Object)}. It will simply lookup all balance logs of original
     * transaction, and restore the commission balance of operator/merchant, and mark all balances logs as cancelled.
     * 
     * @param respCtx
     *            The transaction context.
     * @param targetTrans
     *            The transaction which is cancelled.
     * @param operatorOrMerchant
     *            THe operator or merchant(use parent) on which the balance is done.
     * @throws ApplicationException
     *             If encounter any business exception.
     */
    void cancelCommission(Context<?> respCtx, Transaction targetTrans, Object operatorOrMerchant)
            throws ApplicationException;

}
