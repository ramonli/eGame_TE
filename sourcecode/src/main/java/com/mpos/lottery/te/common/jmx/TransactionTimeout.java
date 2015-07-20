package com.mpos.lottery.te.common.jmx;

/**
 * If standard MBean, all attributes, operations, and listeners must be exposed by a interface, and the class name of
 * MBean and its interface must follow a convention:
 * <p>
 * If the class name of MBean is 'XYZ', its interface must be 'XYZMBean'.
 * 
 * @author Ramon
 * 
 */
public class TransactionTimeout implements TransactionTimeoutMBean {
    // unit: second
    private long timeout = -1l;

    @Override
    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
