package com.mpos.lottery.te.thirdpartyservice.playeraccount.web;

import net.mpos.apc.entry.Cashout.ResCashout;

import java.math.BigDecimal;

public class CashoutResponse extends CashoutRequest {
    private static final long serialVersionUID = 169208457558323729L;
    // How much can be cash out after this operation.
    private BigDecimal leftCashoutAmount = new BigDecimal("0");

    public CashoutResponse() {
    }

    public CashoutResponse(CashoutRequest reqCashout, ResCashout resCashout) {
        this.setCashoutAmount(new BigDecimal(resCashout.getCashoutAmount()));
        this.setMobile(reqCashout.getMobile());
        this.setUserPIN(reqCashout.getUserPIN());
        this.leftCashoutAmount = new BigDecimal(resCashout.getPrizeAmount());
    }

    public BigDecimal getLeftCashoutAmount() {
        return leftCashoutAmount;
    }

    public void setLeftCashoutAmount(BigDecimal leftCashoutAmount) {
        this.leftCashoutAmount = leftCashoutAmount;
    }

}
