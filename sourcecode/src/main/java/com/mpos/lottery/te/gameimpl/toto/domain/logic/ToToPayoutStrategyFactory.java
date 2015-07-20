package com.mpos.lottery.te.gameimpl.toto.domain.logic;

import com.mpos.lottery.te.gamespec.prize.support.payoutstrategy.PayoutStrategy;
import com.mpos.lottery.te.gamespec.prize.support.payoutstrategy.PayoutStrategyFactory;

public class ToToPayoutStrategyFactory implements PayoutStrategyFactory {
    private PayoutStrategy payoutStrategy;

    /**
     * For TOTO game, 'payoutMode'(print new ticket or refund) is meaningless, as only single draw ticket is allowed.
     */
    @Override
    public PayoutStrategy lookupPayoutStrategy(int payoutMode) {
        return this.payoutStrategy;
    }

    public PayoutStrategy getPayoutStrategy() {
        return payoutStrategy;
    }

    public void setPayoutStrategy(PayoutStrategy payoutStrategy) {
        this.payoutStrategy = payoutStrategy;
    }

}
