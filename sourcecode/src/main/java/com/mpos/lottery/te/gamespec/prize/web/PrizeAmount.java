package com.mpos.lottery.te.gamespec.prize.web;

import java.math.BigDecimal;

public class PrizeAmount {
    private BigDecimal prizeAmount = new BigDecimal("0");
    private BigDecimal taxAmount = new BigDecimal("0");
    private BigDecimal actualAmount = new BigDecimal("0");

    public PrizeAmount() {
    }

    public PrizeAmount(BigDecimal prizeAmount, BigDecimal taxAmount, BigDecimal actualAmount) {
        super();
        this.prizeAmount = prizeAmount;
        this.taxAmount = taxAmount;
        this.actualAmount = actualAmount;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

}
