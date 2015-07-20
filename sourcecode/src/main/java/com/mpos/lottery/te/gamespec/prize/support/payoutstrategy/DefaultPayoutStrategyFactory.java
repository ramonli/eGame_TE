package com.mpos.lottery.te.gamespec.prize.support.payoutstrategy;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultPayoutStrategyFactory implements PayoutStrategyFactory {
    private Log logger = LogFactory.getLog(DefaultPayoutStrategyFactory.class);
    private PayoutStrategy newprintPayoutStrategy;
    private PayoutStrategy refundPayoutStrategy;

    @Override
    public PayoutStrategy lookupPayoutStrategy(int payoutMode) {
        PayoutStrategy strategy = null;
        switch (payoutMode) {
            case BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET :
                strategy = this.getNewprintPayoutStrategy();
                break;
            case BaseOperationParameter.PAYOUTMODE_REFUND :
                strategy = this.getRefundPayoutStrategy();
                break;
            default :
                throw new SystemException("Unsupported payout mode:" + payoutMode);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Usage payout strategy(" + strategy + ") for payout mode:" + payoutMode);
        }
        return strategy;
    }

    public PayoutStrategy getNewprintPayoutStrategy() {
        return newprintPayoutStrategy;
    }

    public void setNewprintPayoutStrategy(PayoutStrategy newprintPayoutStrategy) {
        this.newprintPayoutStrategy = newprintPayoutStrategy;
    }

    public PayoutStrategy getRefundPayoutStrategy() {
        return refundPayoutStrategy;
    }

    public void setRefundPayoutStrategy(PayoutStrategy refundPayoutStrategy) {
        this.refundPayoutStrategy = refundPayoutStrategy;
    }

}
