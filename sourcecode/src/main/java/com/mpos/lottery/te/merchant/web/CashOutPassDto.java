package com.mpos.lottery.te.merchant.web;

import java.math.BigDecimal;

public class CashOutPassDto {
    // request
    private BigDecimal amount = new BigDecimal("0");

    private String password;

    // response
    // amount
    private String expireTime;

    private String barcode;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(String expireTime) {
        this.expireTime = expireTime;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

}
