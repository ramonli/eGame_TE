package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import java.math.BigDecimal;

public class InstantTicketResult {
    private String serialNo;
    private BigDecimal totalAmount;
    private int code;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
