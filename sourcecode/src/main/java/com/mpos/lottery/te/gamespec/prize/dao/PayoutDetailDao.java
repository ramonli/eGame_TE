package com.mpos.lottery.te.gamespec.prize.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;

import java.util.List;

/**
 * payout detail dao interface
 * 
 * @author Administrator
 * 
 */
public interface PayoutDetailDao extends DAO {

    List<PayoutDetail> findByTransactions(List<String> transactionIds);

    // /**
    // * Find all object-typed payout details in a batch. It includes all kinds
    // of
    // * papyout, such as lotto payout, validation and batch validation.
    // *
    // * @param operatorId The id of operator who performs settlement.
    // * @param merchantId The id of merchant which manages the operator.
    // * @param batchNo The batch which will be settled.
    // * @return All object-typed payout details in a batch.
    // */
    // List<PayoutDetail> findBySettledPayout(String operatorId, long
    // merchantId, String batchNo);

    List<PayoutDetail> findByPayout(String payoutId);
}
