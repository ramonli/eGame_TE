package com.mpos.lottery.te.gameimpl.extraball.prize.support;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class PayoutStrategyFactory {
    private Log logger = LogFactory.getLog(PayoutStrategyFactory.class);
    private Map<String, PayoutStrategy> strategyMap = new HashMap<String, PayoutStrategy>();

    public void register(String key, PayoutStrategy strategy) {
        this.strategyMap.put(key, strategy);
    }

    public PayoutStrategy lookupPayoutStrategy(int gameType, int payoutMode) {
        // generate the key of strategy
        String key = gameType + "-" + payoutMode;
        PayoutStrategy strategy = this.strategyMap.get(key);
        if (strategy == null) {
            // lookup again
            key = Game.TYPE_UNDEF + "-" + payoutMode;
            strategy = this.strategyMap.get(key);
        }
        if (strategy == null) {
            throw new SystemException("No payout strategy found by key:" + key);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Found PayoutStrategy(" + strategy + ") by key:" + key);
        }
        return strategy;
    }

}
