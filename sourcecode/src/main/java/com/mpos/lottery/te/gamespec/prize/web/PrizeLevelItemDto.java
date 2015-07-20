package com.mpos.lottery.te.gamespec.prize.web;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.prize.support.AmountCalculator;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * The CASH prize information of a given ticket in a single game instance.
 * 
 * @author Ramon Li
 */
public class PrizeLevelItemDto implements java.io.Serializable, AmountCalculator {
    private static final long serialVersionUID = 8487594818103123875L;
    /**
     * The cash prize amount(before tax).
     */
    private BigDecimal prizeAmount = new BigDecimal("0");
    /**
     * The cash tax amount of prize.
     */
    private BigDecimal taxAmount = new BigDecimal("0");
    /**
     * cash actualAmount := prizeAmount - taxAmount
     */
    private BigDecimal actualAmount = new BigDecimal("0");
    /**
     * The total prize amount of object, just for reference.
     */
    private BigDecimal objectPrizeAmount = new BigDecimal("0");
    /**
     * The total tax amount of object, just for reference
     */
    private BigDecimal objectTaxAmount = new BigDecimal("0");
    /**
     * How many prize level is won?
     */
    private int numberOfPrizeLevel;
    private String prizeLevel;
    private List<PrizeLevelObjectItemDto> prizeLevelObjectItems = new LinkedList<PrizeLevelObjectItemDto>();

    @Override
    public void calculate() throws ApplicationException {
        // reset amount which needs to be re-calculated first
        this.objectPrizeAmount = new BigDecimal("0");
        this.objectTaxAmount = new BigDecimal("0");
        for (PrizeLevelObjectItemDto objectItem : this.prizeLevelObjectItems) {
            this.objectPrizeAmount = this.objectPrizeAmount.add(SimpleToolkit.mathMultiple(
                    SimpleToolkit.mathMultiple(objectItem.getPrice(), new BigDecimal(objectItem.getNumberOfObject()
                            + "")), new BigDecimal(this.numberOfPrizeLevel + "")));
            this.objectTaxAmount = this.objectTaxAmount.add(SimpleToolkit.mathMultiple(
                    SimpleToolkit.mathMultiple(objectItem.getTaxAmount(), new BigDecimal(objectItem.getNumberOfObject()
                            + "")), new BigDecimal(this.numberOfPrizeLevel + "")));
        }
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public int getNumberOfPrizeLevel() {
        return numberOfPrizeLevel;
    }

    public void setNumberOfPrizeLevel(int numberOfPrizeLevel) {
        this.numberOfPrizeLevel = numberOfPrizeLevel;
    }

    public String getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(String prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public BigDecimal getObjectPrizeAmount() {
        return objectPrizeAmount;
    }

    public void setObjectPrizeAmount(BigDecimal objectPrizeAmount) {
        this.objectPrizeAmount = objectPrizeAmount;
    }

    public BigDecimal getObjectTaxAmount() {
        return objectTaxAmount;
    }

    public void setObjectTaxAmount(BigDecimal objectTaxAmount) {
        this.objectTaxAmount = objectTaxAmount;
    }

    public List<PrizeLevelObjectItemDto> getPrizeLevelObjectItems() {
        return prizeLevelObjectItems;
    }

    public void setPrizeLevelObjectItems(List<PrizeLevelObjectItemDto> prizeLevelObjectItems) {
        this.prizeLevelObjectItems = prizeLevelObjectItems;
    }

}
