package com.mpos.lottery.te.merchant.web;

import java.math.BigDecimal;

public class ActivityReportItem {
    // transaction type
    private int transType;
    private int numberOfTrans;
    // before tax. for sale
    private BigDecimal amount = new BigDecimal("0");
    // if payout, vat is tax, levy should be zero.
    private BigDecimal tax = new BigDecimal("0");

    private BigDecimal commission = new BigDecimal("0");

    // private BigDecimal levy = new BigDecimal("0");

    public ActivityReportItem() {
    }

    public ActivityReportItem(int transType) {
        this.transType = transType;
    }

    public ActivityReportItem(ActivityReportItem reportItem) {
        this(reportItem.getTransType(), reportItem.getNumberOfTrans(), reportItem.getAmount(), reportItem.getTax(),
                reportItem.getCommission());
    }

    public ActivityReportItem(int transType, int numberOfTrans, BigDecimal amount, BigDecimal tax, BigDecimal commission) {
        this.transType = transType;
        this.numberOfTrans = numberOfTrans;
        this.amount = amount;
        this.tax = tax;
        this.commission = commission;
        // this.levy = levy;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    public int getNumberOfTrans() {
        return numberOfTrans;
    }

    public void setNumberOfTrans(int numberOfTrans) {
        this.numberOfTrans = numberOfTrans;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTax() {
        return this.tax;
    }

    public void setTax(BigDecimal vat) {
        this.tax = vat;
    }

    /**
     * @return commission
     */
    public BigDecimal getCommission() {
        return commission;
    }

    /**
     * @param commission
     */
    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

}
