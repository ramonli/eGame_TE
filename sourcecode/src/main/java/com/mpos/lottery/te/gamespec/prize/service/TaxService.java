package com.mpos.lottery.te.gamespec.prize.service;

import java.math.BigDecimal;

/**
 * This bean will be responsible of calculating tax of a given amount. In general there are two policies of calculating
 * tax: fixed amount and percentage based. The implementation must support both policies.
 * 
 * @author Ramon Li
 */
public interface TaxService {

    /**
     * Calculate tax upon the given amount and tax policy.
     * 
     * @param inputAmount
     *            The given amount will be calculate tax upon.
     * @param gameId
     *            The identify of game. Different game may require different tax policy.
     * @return the final tax after calculating.
     */
    BigDecimal tax(BigDecimal inputAmount, String gameId);

    /**
     * Calculate tax upon the given amount and tax policy(only vat and levy).
     * 
     * @param inputAmount
     *            The given amount will be calculate tax upon.
     * @param taxPolicyId
     *            Represents the tax policy: fixed amount of percentage.
     * @return the final tax after calculating.
     * @author Lee
     */
    BigDecimal VatAndLevyTax(BigDecimal inputAmount, String taxPolicyId);
}
