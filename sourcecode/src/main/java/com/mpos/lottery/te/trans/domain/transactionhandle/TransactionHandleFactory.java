package com.mpos.lottery.te.trans.domain.transactionhandle;

import com.mpos.lottery.te.config.exception.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("transactionHandleFactory")
public class TransactionHandleFactory {
    private Log logger = LogFactory.getLog(TransactionHandleFactory.class);
    private Map<Integer, TransactionHandle> transactionHandleFactory = new HashMap<Integer, TransactionHandle>();

    /**
     * Lookup a <code>TransactionHandle</code> by given game type id.
     */
    public TransactionHandle lookupHandle(int gameTypeId) {
        for (Integer handleGameTypeId : transactionHandleFactory.keySet()) {
            if (gameTypeId == handleGameTypeId) {
                TransactionHandle handle = this.transactionHandleFactory.get(gameTypeId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Found " + TransactionHandleFactory.class.getSimpleName() + "(" + handle
                            + ") for transaction handle " + handleGameTypeId);
                }
                return handle;
            }
        }
        return null;
    }

    /**
     * Register a <code>TransactionHandle</code> instance with given game type id. Only a single instance can be bound
     * to a given game type id.
     */
    public void register(TransactionHandle handle) {
        TransactionHandle exited = this.lookupHandle(handle.supportHandle());
        if (exited != null) {
            throw new SystemException("THe game type id(" + handle.supportHandle() + ") has been bounded by " + exited);
        }
        this.transactionHandleFactory.put(handle.supportHandle(), handle);
    }
}
