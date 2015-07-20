package com.mpos.lottery.te.gamespec.prize.support;

import com.mpos.lottery.te.config.exception.ApplicationException;

/**
 * A interface for calculate the internal amount.
 * 
 * @author Ramon
 * 
 */
public interface AmountCalculator {

    void calculate() throws ApplicationException;
}
