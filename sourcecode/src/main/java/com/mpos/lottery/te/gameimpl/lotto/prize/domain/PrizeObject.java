package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import java.math.BigDecimal;

/**
 * Use this domain class to represent 2nd chance and daily cash prize.
 */
public class PrizeObject {
    private String prizeLogicId;
    private int prizeLevel;
    // the identifer of object...in general it is from bd_prize_object
    private String id;
    private String name;
    private BigDecimal prizeAmount = new BigDecimal("0");
    private BigDecimal tax = new BigDecimal("0");
    private String description;
    // refer to PrizeLevel.PRIZE_TYPE_XXX
    private int prizeType;
    private int numberOfPrize = 1;

    public PrizeObject() {
    }

    public PrizeObject(String prizeLogicId, int prizeLevel, String id, String name, BigDecimal prizeAmount,
            BigDecimal tax, int prizeType, int numberOfPrize, String description) {
        this.prizeLogicId = prizeLogicId;
        this.prizeLevel = prizeLevel;
        this.id = id;
        this.name = name;
        this.prizeAmount = prizeAmount;
        this.tax = tax;
        this.description = description;
        this.prizeType = prizeType;
        this.numberOfPrize = numberOfPrize;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public int getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(int prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(int prizeType) {
        this.prizeType = prizeType;
    }

    public int getNumberOfPrize() {
        return numberOfPrize;
    }

    public void setNumberOfPrize(int numberOfPrize) {
        this.numberOfPrize = numberOfPrize;
    }

}
