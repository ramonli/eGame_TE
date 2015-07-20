package com.mpos.lottery.te.merchant.support;

import com.google.gson.Gson;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperatorTopupReversalStrategy extends AbstractReversalOrCancelStrategy {
    private Log logger = LogFactory.getLog(OperatorTopupReversalStrategy.class);
    private CreditService creditService;
    private BalanceTransactionsDao balanceTransactionsDao;

    @Override
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(TransactionType.OPERATOR_TOPUP_VOUCHER.getRequestType());
    }

    /**
     * Cancel the transaction of 'operator topup'.
     */
    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        this.getCreditService().credit(targetTrans.getOperatorId(), targetTrans.getMerchantId(),
                targetTrans.getTotalAmount(), false, true);
        BalanceTransactions transMessageOjbect = new Gson().fromJson(targetTrans.getTransMessage().getRequestMsg(),
                BalanceTransactions.class);
        balanceTransactionsDao.updateBalanceTransactionsStatusByteTransactionId(targetTrans.getTransMessage()
                .getTransactionId());
        BalanceTransactions balanceTransactions = balanceTransactionsDao.assembleBalanceTransactions(respCtx,
                targetTrans.getTotalAmount());
        balanceTransactions.setOriginalTransType(targetTrans.getType());
        balanceTransactions.setCommissionAmount(BalanceTransactions.ZERO.subtract(transMessageOjbect
                .getCommissionAmount()));
        balanceTransactions.setTransactionAmount(BalanceTransactions.ZERO.subtract(targetTrans.getTotalAmount()));
        balanceTransactions.setCommissionRate(balanceTransactions.getCommissionRate());
        balanceTransactionsDao.insert(balanceTransactions);
        return false;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
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

}
