package com.mpos.lottery.te.merchant.service.balance;

import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractBalanceStrategy implements BalanceStrategy, InitializingBean {
    private DefaultBalanceService balanceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.getBalanceService().registerBalanceStrategy(this);
    }

    public DefaultBalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(DefaultBalanceService balanceService) {
        this.balanceService = balanceService;
    }

}
