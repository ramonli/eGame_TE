package com.mpos.lottery.te.gamespec.prize.service.impl;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.dao.GameDao;
import com.mpos.lottery.te.gamespec.prize.TaxThreshold;
import com.mpos.lottery.te.gamespec.prize.dao.TaxThresholdDao;
import com.mpos.lottery.te.gamespec.prize.service.TaxService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Date;

/**
 * A default implementation to support both tax policies: fixed amount and percentage. As the algorithm isn't
 * complicated, only one implementing class to handle all.
 * 
 * @author Ramon Li
 */
public class DefaultTaxService implements TaxService {
    private Log logger = LogFactory.getLog(DefaultTaxService.class);

    // sprint dependencies
    private TaxThresholdDao taxThresholdDao;
    private GameDao gameDao;

    /**
     * @see TaxService#tax(BigDecimal, String)
     */
    @Override
    public BigDecimal tax(BigDecimal amount, String gameId) {
        Game game = this.getGameDao().findById(Game.class, gameId);

        BigDecimal tax = new BigDecimal("0");
        if (amount.compareTo(new BigDecimal("0")) > 0) {
            TaxThreshold taxThreshold = this.getTaxThresholdDao().getByPolicyAndAmountAndDateRange(
                    game.getTaxPolicyId(), amount, new Date());
            if (taxThreshold == null) {
                throw new SystemException("can NOT find Tax-Threshold definition by (taxPolicyId="
                        + game.getTaxPolicyId() + ",prizeAmount=" + amount + ",currentDate:" + new Date().toString()
                        + ").");
            }
            tax = this.calculate(amount, taxThreshold);
            if (logger.isDebugEnabled()) {
                logger.debug("Calculate tax for amount:" + amount + " based on threshold(" + taxThreshold.toString()
                        + ")...tax:" + tax);
            }
        }
        return tax;
    }

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
    public BigDecimal VatAndLevyTax(BigDecimal inputAmount, String taxPolicyId) {
        TaxThreshold taxThreshold = this.getTaxThresholdDao().getByPolicyAndAmountAndDateRange(taxPolicyId,
                inputAmount, new Date());
        if (taxThreshold == null) {
            throw new SystemException("can NOT find vat or levy Tax-Threshold definition by (taxPolicyId="
                    + taxPolicyId + ",prizeAmount=" + inputAmount + ",currentDate:" + new Date().toString() + ").");
        }
        BigDecimal tax = this.calculateVatAndLevy(inputAmount, taxThreshold);
        if (logger.isDebugEnabled()) {
            logger.debug("Calculate vat or levy tax for amount:" + inputAmount + " based on threshold("
                    + taxThreshold.toString() + ")...tax:" + tax);
        }
        return tax;
    }

    /**
     * calculate the tax based on give policy and amount.
     * 
     * @param inputAmount
     *            The give amount need to calculate tax.
     * @param taxThreshold
     *            The tax threshold.
     * @return the final tax after calculating.
     */
    protected BigDecimal calculateVatAndLevy(BigDecimal inputAmount, TaxThreshold taxThreshold) {
        BigDecimal tax = null;
        // calculate the tax based on given amount
        if (TaxThreshold.RULETYPE_FIXAMOUNT == taxThreshold.getRuleType()) {
            tax = taxThreshold.getTaxAmount();
        } else {
            tax = SimpleToolkit.mathMultiple(inputAmount, taxThreshold.getTaxAmount());
        }
        return tax.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * calculate the tax based on give policy and amount.
     * 
     * @param inputAmount
     *            The give amount need to calculate tax.
     * @param taxThreshold
     *            The tax threshold.
     * @return the final tax after calculating.
     */
    protected BigDecimal calculate(BigDecimal inputAmount, TaxThreshold taxThreshold) {
        BigDecimal tax = null;
        // calculate the tax based on given amount
        if (TaxThreshold.RULETYPE_FIXAMOUNT == taxThreshold.getRuleType()) {
            tax = taxThreshold.getTaxAmount();
        } else {
            if (TaxThreshold.TAXBASE_PRIZE == taxThreshold.getTaxBase()) {
                // tax = inputAmount.multiply(taxThreshold.getTaxAmount());
                tax = SimpleToolkit.mathMultiple(inputAmount, taxThreshold.getTaxAmount());
            } else if (TaxThreshold.TAXBASE_PAYOUT == taxThreshold.getTaxBase()) {
                // tax = inputAmount.divide(new
                // BigDecimal("1").add(taxThreshold.getTaxAmount()), 2,
                // BigDecimal.ROUND_HALF_EVEN).multiply(taxThreshold.getTaxAmount());
                tax = SimpleToolkit.mathMultiple(
                        SimpleToolkit.mathDivide(inputAmount, new BigDecimal("1").add(taxThreshold.getTaxAmount())),
                        taxThreshold.getTaxAmount());
            }
        }
        return tax.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }

    // -------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -------------------------------------

    public TaxThresholdDao getTaxThresholdDao() {
        return taxThresholdDao;
    }

    public void setTaxThresholdDao(TaxThresholdDao taxThresholdDao) {
        this.taxThresholdDao = taxThresholdDao;
    }

    public GameDao getGameDao() {
        return gameDao;
    }

    public void setGameDao(GameDao gameDao) {
        this.gameDao = gameDao;
    }

}
