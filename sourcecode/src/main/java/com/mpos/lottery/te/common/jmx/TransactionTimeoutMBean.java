package com.mpos.lottery.te.common.jmx;

public interface TransactionTimeoutMBean {
    public static final String objectName = "com.mpos.lottery.te:type=TransactionTimeoutMBean";

    public long getTimeout();

    public void setTimeout(long timeout);

}
