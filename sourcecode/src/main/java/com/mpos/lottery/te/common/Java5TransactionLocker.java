package com.mpos.lottery.te.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Java5TransactionLocker {
    private Log logger = LogFactory.getLog(Java5TransactionLocker.class);
    private static Java5TransactionLocker queue;
    private Map<String, ReentrantLock> lockMap = Collections.synchronizedMap(new HashMap<String, ReentrantLock>());
    private final Lock globalLock = new ReentrantLock();

    private Java5TransactionLocker() {
    }

    public synchronized static Java5TransactionLocker getInstance() {
        if (queue == null) {
            queue = new Java5TransactionLocker();
        }
        return queue;
    }

    /**
     * Acquire a reentrant lock associating with a key. DO NOT forget release the lock by invoking release().
     * 
     * @param key
     *            The key to uniquely identify a lock.
     * @return a reentrant lock.
     */
    public KeyValuePair<String, ReentrantLock> acquire(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument 'key' can NOT be null.");
        }
        ReentrantLock transLock = null;
        globalLock.lock();
        try {
            transLock = lockMap.get(key);
            if (transLock == null) {
                transLock = new ReentrantLock();
                lockMap.put(key, transLock);
            }
        } finally {
            globalLock.unlock();
        }
        transLock.lock();
        if (logger.isDebugEnabled()) {
            logger.debug("Acquired the lock:" + transLock);
        }
        return new KeyValuePair<String, ReentrantLock>(key, transLock);
    }

    public void release(KeyValuePair<String, ReentrantLock> transLockMap) {
        if (transLockMap == null) {
            return;
        }
        ReentrantLock lock = transLockMap.getValue();
        if (lock == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Release the lock:" + lock + "(holdCount=" + lock.getHoldCount() + ",queueLength()="
                    + lock.getQueueLength() + ").");
        }
        try {
            int count = ((ReentrantLock) lock).getHoldCount();
            for (int i = 0; i < count; i++) {
                lock.unlock();
            }
            // remove lock from map, or maybe out of memory
            if (lock.getQueueLength() == 0) {
                // how many threads are waiting to
                // acquire this lock?
                this.lockMap.remove(transLockMap.getKey());
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
