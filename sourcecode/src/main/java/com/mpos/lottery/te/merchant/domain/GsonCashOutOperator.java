package com.mpos.lottery.te.merchant.domain;

import java.math.BigDecimal;

public class GsonCashOutOperator {
    // operator/merchant deducted\
    private int operatorMerchantType;

    private String operatorMerchantid;

    private String operatorid;

    private BigDecimal totalAmount = new BigDecimal("0");

    private BigDecimal commission = new BigDecimal("0");

    private BigDecimal payout = new BigDecimal("0");

    private BigDecimal cashout = new BigDecimal("0");

    // plus
    private int plusOperatorMerchantType;
    // operator/ plus
    private String plusOperatorid;

    private BigDecimal plusOperatorCashoutBalance = new BigDecimal("0");

    private BigDecimal plusOperatorCommissionBalance = new BigDecimal("0");

    private BigDecimal plusOperatorCommissionRate = new BigDecimal("0");

    // merchant/ plus
    private String plusMerchantid;

    private BigDecimal plusMerchantCashoutBalance = new BigDecimal("0");

    private BigDecimal plusMerchantCommissionBalance = new BigDecimal("0");

    private BigDecimal plusMerchantCommissionRate = new BigDecimal("0");

    public int getOperatorMerchantType() {
        return operatorMerchantType;
    }

    public void setOperatorMerchantType(int operatorMerchantType) {
        this.operatorMerchantType = operatorMerchantType;
    }

    public String getOperatorMerchantid() {
        return operatorMerchantid;
    }

    public void setOperatorMerchantid(String operatorMerchantid) {
        this.operatorMerchantid = operatorMerchantid;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public BigDecimal getPayout() {
        return payout;
    }

    public void setPayout(BigDecimal payout) {
        this.payout = payout;
    }

    public BigDecimal getCashout() {
        return cashout;
    }

    public void setCashout(BigDecimal cashout) {
        this.cashout = cashout;
    }

    public int getPlusOperatorMerchantType() {
        return plusOperatorMerchantType;
    }

    public void setPlusOperatorMerchantType(int plusOperatorMerchantType) {
        this.plusOperatorMerchantType = plusOperatorMerchantType;
    }

    public String getPlusOperatorid() {
        return plusOperatorid;
    }

    public void setPlusOperatorid(String plusOperatorid) {
        this.plusOperatorid = plusOperatorid;
    }

    public BigDecimal getPlusOperatorCashoutBalance() {
        return plusOperatorCashoutBalance;
    }

    public void setPlusOperatorCashoutBalance(BigDecimal plusOperatorCashoutBalance) {
        this.plusOperatorCashoutBalance = plusOperatorCashoutBalance;
    }

    public BigDecimal getPlusOperatorCommissionBalance() {
        return plusOperatorCommissionBalance;
    }

    public void setPlusOperatorCommissionBalance(BigDecimal plusOperatorCommissionBalance) {
        this.plusOperatorCommissionBalance = plusOperatorCommissionBalance;
    }

    public BigDecimal getPlusOperatorCommissionRate() {
        return plusOperatorCommissionRate;
    }

    public void setPlusOperatorCommissionRate(BigDecimal plusOperatorCommissionRate) {
        this.plusOperatorCommissionRate = plusOperatorCommissionRate;
    }

    public String getPlusMerchantid() {
        return plusMerchantid;
    }

    public void setPlusMerchantid(String plusMerchantid) {
        this.plusMerchantid = plusMerchantid;
    }

    public BigDecimal getPlusMerchantCashoutBalance() {
        return plusMerchantCashoutBalance;
    }

    public void setPlusMerchantCashoutBalance(BigDecimal plusMerchantCashoutBalance) {
        this.plusMerchantCashoutBalance = plusMerchantCashoutBalance;
    }

    public BigDecimal getPlusMerchantCommissionBalance() {
        return plusMerchantCommissionBalance;
    }

    public void setPlusMerchantCommissionBalance(BigDecimal plusMerchantCommissionBalance) {
        this.plusMerchantCommissionBalance = plusMerchantCommissionBalance;
    }

    public BigDecimal getPlusMerchantCommissionRate() {
        return plusMerchantCommissionRate;
    }

    public void setPlusMerchantCommissionRate(BigDecimal plusMerchantCommissionRate) {
        this.plusMerchantCommissionRate = plusMerchantCommissionRate;
    }

    public String getOperatorid() {
        return operatorid;
    }

    public void setOperatorid(String operatorid) {
        this.operatorid = operatorid;
    }

}
