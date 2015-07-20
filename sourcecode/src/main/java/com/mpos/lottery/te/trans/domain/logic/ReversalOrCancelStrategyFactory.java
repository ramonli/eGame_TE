package com.mpos.lottery.te.trans.domain.logic;

import com.mpos.lottery.common.router.Version;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class ReversalOrCancelStrategyFactory {
    private Log logger = LogFactory.getLog(ReversalOrCancelStrategyFactory.class);

    private Map<RoutineKey, ReversalOrCancelStrategy> reversalStrategyMap = new HashMap<RoutineKey, ReversalOrCancelStrategy>();

    /**
     * Register a reversal strategy for specific transaction.
     * 
     * @param routineKey
     *            The rountine key which identify which transaction can canceled by this
     *            <code>ReversalOrCancelStrategy</code>.
     * @param reversalStrategy
     *            The reversal handler of a specific transaction.
     */
    public void register(RoutineKey routineKey, ReversalOrCancelStrategy reversalStrategy) {
        // RoutineKey key = reversalStrategy.getStrategyKey();
        ReversalOrCancelStrategy strategy = this.reversalStrategyMap.get(routineKey);
        if (strategy == null) {
            logger.debug("Register ReversalOrCancelStrategy(" + reversalStrategy + ") by key(" + routineKey + ")");
            this.reversalStrategyMap.put(routineKey, reversalStrategy);
        } else {
            throw new SystemException("Found duplicated reversal strategy definitions for handling transaction(key="
                    + routineKey + "):" + strategy.getClass() + ", " + reversalStrategy);
        }
    }

    public ReversalOrCancelStrategy lookupReversalStrategy(Context<?> respCtx, Transaction targetTrans) {
        // Routine key should be generated from target transaction(assemble
        // gametype).
        // Lookup process will try most accurate till less.
        // Lookup by gameType+transType+protocolVersion
        RoutineKey key = new RoutineKey(respCtx.getGameTypeIdIntValue(), targetTrans.getType(),
                Version.from(targetTrans.getVersion() + ""));
        // 1st: Lookup by gameType+transType+version
        ReversalOrCancelStrategy strategy = this.reversalStrategyMap.get(key);
        if (strategy == null) {
            // 2nd: Lookup by gameType+transType
            key = new RoutineKey(respCtx.getGameTypeIdIntValue(), targetTrans.getType(), null);
            strategy = this.reversalStrategyMap.get(key);
            if (strategy == null) {
                // 3rd: Lookup by only transType
                key = new RoutineKey(targetTrans.getType());
                strategy = this.reversalStrategyMap.get(key);
            }
        }

        if (strategy == null) {
            throw new SystemException("NO instance of " + ReversalOrCancelStrategy.class
                    + " found for Transaction(traceMessageId=" + targetTrans.getTraceMessageId() + ",terminalId="
                    + targetTrans.getDeviceId() + ",transType=" + targetTrans.getType() + ",gameType="
                    + respCtx.getGameTypeId() + ",protocolVersion=" + Version.from(respCtx.getProtocalVersion()) + ").");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Found ReversalStrategy(" + strategy.getClass() + ") to cancel transaction(gameType="
                        + respCtx.getGameTypeId() + ",terminalId=" + targetTrans.getDeviceId() + ",transType="
                        + targetTrans.getType() + ",protocolVersion=" + Version.from(respCtx.getProtocalVersion())
                        + ").");
            }
        }
        return strategy;
    }

    // ------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ------------------------------------------------
}
