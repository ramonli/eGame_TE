package com.mpos.lottery.te.trans.domain.logic;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.trans.domain.Transaction;

/**
 * Any transactions if it can be reversed or cancelled by transaction(not cancel by ticket) can has a corresponding
 * <code>ReversalOrCancelStrategy</code>. As reversal and cancel by transaction fall in same semantics, both are
 * responsible of undoing previous transaction, we don't need to define 2 separated interfaces.
 * 
 * @author Ramon Li
 */
public interface ReversalOrCancelStrategy {

    /**
     * Cancel the target transaction.
     * 
     * @param respCtx
     *            The context of cancellation request.
     * @param targetTrans
     *            The transaction which is intended to be cancelled.
     * @return true if cancel declined, or false.
     */
    boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException;

    /**
     * Determine what kind of transaction will be cancelled/reversed by the concrete
     * <code>ReversalOrCancelStrategy</code> implementation.
     */
    RoutineKey supportedReversalRoutineKey();
}
