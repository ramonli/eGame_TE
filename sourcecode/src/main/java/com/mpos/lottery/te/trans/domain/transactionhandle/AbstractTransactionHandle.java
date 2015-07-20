package com.mpos.lottery.te.trans.domain.transactionhandle;

import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

public abstract class AbstractTransactionHandle implements TransactionHandle, InitializingBean {

    @Resource(name = "transactionHandleFactory")
    private TransactionHandleFactory transactionHandleFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        transactionHandleFactory.register(this);
    }

    public TransactionHandleFactory getTransactionHandleFactory() {
        return transactionHandleFactory;
    }

    public void setTransactionHandleFactory(TransactionHandleFactory transactionHandleFactory) {
        this.transactionHandleFactory = transactionHandleFactory;
    }
}
