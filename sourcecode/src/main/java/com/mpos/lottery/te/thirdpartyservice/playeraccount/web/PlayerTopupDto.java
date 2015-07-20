package com.mpos.lottery.te.thirdpartyservice.playeraccount.web;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.math.BigDecimal;

public class PlayerTopupDto {
    private String accountId;
    private String voucherSerialNo;
    private BigDecimal amount = new BigDecimal("0");

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getVoucherSerialNo() {
        return voucherSerialNo;
    }

    public void setVoucherSerialNo(String voucherSerialNo) {
        this.voucherSerialNo = voucherSerialNo;
    }

    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
