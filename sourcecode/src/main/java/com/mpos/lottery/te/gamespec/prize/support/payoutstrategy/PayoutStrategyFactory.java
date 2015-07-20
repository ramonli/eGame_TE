package com.mpos.lottery.te.gamespec.prize.support.payoutstrategy;

/**
 * The interface for implementation of {@link com.mpos.lottery.te.gamespec.prize.service.PrizeService} to lookup a
 * appropriate <code>PayoutStrategy</code>. In general each game type should have its own implementation, however there
 * is a default <code>DefaultPayoutStrategyFactory</code> for in convenient usage.
 * 
 * @author Ramon
 * 
 */
public interface PayoutStrategyFactory {

    public PayoutStrategy lookupPayoutStrategy(int payoutMode);

}
