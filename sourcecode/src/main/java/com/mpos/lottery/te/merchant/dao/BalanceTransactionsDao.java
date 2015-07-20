package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;
import java.util.List;

public interface BalanceTransactionsDao extends DAO {

    BalanceTransactions findByTransactionAndOwnerAndGameAndOrigTransType(String transactionId, String ownerId,
            String gameId, int origTransType);

    /**
     * Query activity report of special game type.
     */
    List<BalanceTransactions> findBalanceTransactions(String teTransactionId);

    List<BalanceTransactions> findByOwnerAndTransaction(String transactionId, String ownerId);

    void updateBalanceTransactionsStatusByteTransactionId(String teTransactionId);

    BalanceTransactions assembleBalanceTransactions(Context respCtx, BigDecimal amount) throws ApplicationException;

    void addBalanceTransactionRecord(Context respCtx, BigDecimal cashoutAmount, int transType,
            String operatorMerchantid, int ownerType, int paymentType, BigDecimal commissionAmount,
            BigDecimal commissionRate) throws ApplicationException;

    /**
     * add balance transaction for transfer.
     */
    void addBalanceTransferBalanceTransactionRecord(Context reqCtx, BigDecimal balanceAmount, int transType,
            int orgTransType, Long fromParentMerchantid, Long toParentMerchantid, String operatorid, String ownerid,
            int ownerType, int paymentType) throws ApplicationException;

}
