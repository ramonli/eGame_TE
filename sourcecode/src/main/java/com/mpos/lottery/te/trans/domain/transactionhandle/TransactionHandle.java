package com.mpos.lottery.te.trans.domain.transactionhandle;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

public interface TransactionHandle {

    /**
     * Check whether this handle supports given game type id. For each service handle there should be a dedicated
     * implementation.
     * 
     * @return the game type id of supported airtime handle.
     */
    int supportHandle();

    /**
     * Query transaction model by game type id.
     * 
     * @param respCtx
     *            response context
     * @param targetTrans
     *            Query transaction object by transaction message id
     * @return model transaction object
     */
    Object getTransactionModel(Context respCtx, Transaction targetTrans) throws ApplicationException;
}
