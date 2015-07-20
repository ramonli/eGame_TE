package com.mpos.lottery.te.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TransactionLocker {
    private Log logger = LogFactory.getLog(TransactionLocker.class);
    private Map<String, Locker> transMap = Collections.synchronizedMap(new HashMap<String, Locker>());
    private static TransactionLocker transLocker;
    private final Object globeLock = new Object();

    private TransactionLocker() {
    }

    public synchronized static TransactionLocker getInstance() {
        if (transLocker == null) {
            transLocker = new TransactionLocker();
        }
        return transLocker;
    }

    /**
     * Record which transaction(deviceId+operatorId+batchNo+transType) is in progress.
     * 
     * @param key
     *            deviceId-operatorId-batchNo-transType
     */
    public void acquire(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument 'key' can NOT be null");
        }
        // all threads handling same type of
        // transaction(deviceId+operatorId+batchNo+transType)
        // must monitor the same object, by contrast the thread handling
        // different type transaction
        // must monitor different object.
        Locker locker = null;
        synchronized (globeLock) {
            locker = transMap.get(key);
            if (locker == null) {
                locker = new Locker();
                locker.setKey(key);
                transMap.put(key, locker);
                return;
            }
        }
        synchronized (locker) {
            if (locker != null) { // the lock has been assigned.
                if (logger.isDebugEnabled()) {
                    logger.debug("A same type transaction(" + key + ") is in progress, current "
                            + "transaction will be blocked until the previous transaction finished.");
                }
                locker.addWaiter();
                try {
                    // the current thread must be the object's monitor, and
                    // wait() will release
                    // the ownership of this monitor.
                    locker.wait();

                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
                // got notification...means some thread invoke release().
                locker.removeWaiter();
                if (logger.isDebugEnabled()) {
                    logger.debug("Got transaction lock(" + key + "), resume current transaction.");
                }
            }
        }
    }

    /**
     * Release the lock, then other transaction of same type can continue.
     */
    public void release(String key) {
        if (key == null) {
            return;
        }
        Locker locker = transMap.get(key);
        if (locker != null) {
            if (locker.getWaiter() > 0) {
                synchronized (locker) {
                    logger.debug("Release lock(" + key + "), there are " + locker.getWaiter() + " waiters.");
                    locker.notify();
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No threads waiting for this lock(" + key + "), remove it directly.");
                }
                // or transMap will result in outofmemory.
                transMap.remove(key);
            }
        }
    }

    public static class Locker {
        private String key;
        // How many threads waiting for the lock?
        private int waiter;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getWaiter() {
            return waiter;
        }

        public void addWaiter() {
            this.waiter++;
        }

        public void removeWaiter() {
            this.waiter--;
            if (this.waiter <= 0) {
                this.waiter = 0;
            }
        }

        public String toString() {
            return this + "(key=" + this.key + ",waiter=" + this.waiter + ")";
        }
    }
}
