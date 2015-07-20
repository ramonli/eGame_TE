package com.mpos.lottery.te.merchant.web;

import java.math.BigDecimal;

public class CashOutByOperatorPassDto {

    // request
    private String barcode;

    private String password;

    // response
    private BigDecimal amount = new BigDecimal("0");

    private String operatorId;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

}
