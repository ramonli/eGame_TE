package com.mpos.lottery.te.gameimpl.digital.game;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "FD_OPERATION_PARAMETERS")
public class DigitalOperationParameter extends BaseOperationParameter {
    private static final long serialVersionUID = 937366293660209792L;
    public static int ODD_EVEN_SUPPORT = 1;
    public static int ODD_EVEN_NO_SUPPORT = 2;

    // whether Odd&Even bet options will be supported, refer to ODD_EVEN_XXX
    @Column(name = "IS_ODD_EVEN")
    private boolean supportOddEven;

    @Column(name = "IS_SUPPORT_SUM")
    private boolean supportSum;

    /**
     * Here column 'BASE_AMOUNT'/'MAX_BASE_AMOUNT' will be used to limit the range of bet amount.
     */
    @Column(name = "MAX_BASE_AMOUNT")
    private BigDecimal maxBetAmount = new BigDecimal("0");

    public boolean isSupportOddEven() {
        return supportOddEven;
    }

    public void setSupportOddEven(boolean supportOddEven) {
        this.supportOddEven = supportOddEven;
    }

    public boolean isSupportSum() {
        return supportSum;
    }

    public void setSupportSum(boolean supportSum) {
        this.supportSum = supportSum;
    }

    public BigDecimal getMaxBetAmount() {
        return maxBetAmount;
    }

    public void setMaxBetAmount(BigDecimal maxBetAmount) {
        this.maxBetAmount = maxBetAmount;
    }

}
