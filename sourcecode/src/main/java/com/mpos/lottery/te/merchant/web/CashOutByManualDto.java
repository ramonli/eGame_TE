package com.mpos.lottery.te.merchant.web;

import java.math.BigDecimal;

public class CashOutByManualDto {

    // request
    private String operatorId;

    private BigDecimal amount = new BigDecimal("0");

    private String password;

    // response
    // amount
    // operatorId

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
