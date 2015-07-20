package com.mpos.lottery.te.thirdpartyservice.playeraccount.web;

import com.mpos.lottery.te.thirdpartyservice.playeraccount.User;

import java.io.Serializable;
import java.math.BigDecimal;

public class CashoutRequest implements Serializable {
    private static final long serialVersionUID = -3198231494325406734L;
    private String mobile;
    private String userPIN;
    private BigDecimal cashoutAmount;
    private String referenceNum;

    public User getUser() {
        User user = new User();
        user.setMobile(this.getMobile());
        user.setPIN(this.getUserPIN());
        return user;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUserPIN() {
        return userPIN;
    }

    public void setUserPIN(String userPIN) {
        this.userPIN = userPIN;
    }

    public BigDecimal getCashoutAmount() {
        return cashoutAmount;
    }

    public void setCashoutAmount(BigDecimal cashoutAmount) {
        this.cashoutAmount = cashoutAmount;
    }

    public String getReferenceNum() {
        return referenceNum;
    }

    public void setReferenceNum(String referenceNum) {
        this.referenceNum = referenceNum;
    }

}
