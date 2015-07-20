package com.mpos.lottery.te.merchant.web;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.math.BigDecimal;

public class OperatorTopupDto {
    private String operatorId;
    private String voucherSerialNo;
    private BigDecimal amount = new BigDecimal("0");
    private BigDecimal saleBalance = new BigDecimal("0");

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getSaleBalance() {
        return saleBalance;
    }

    public void setSaleBalance(BigDecimal saleBalance) {
        this.saleBalance = saleBalance;
    }

    public String toString() {
        return new ToStringBuilder(this).toString();
    }

    public String getVoucherSerialNo() {
        return voucherSerialNo;
    }

    public void setVoucherSerialNo(String voucherSerialNo) {
        this.voucherSerialNo = voucherSerialNo;
    }

}
