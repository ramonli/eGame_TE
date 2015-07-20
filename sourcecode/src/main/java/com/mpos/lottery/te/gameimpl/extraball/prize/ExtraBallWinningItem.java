package com.mpos.lottery.te.gameimpl.extraball.prize;

import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "EB_WINNING")
@AttributeOverrides({ @AttributeOverride(name = "gameInstanceId", column = @Column(name = "EB_GAME_INSTANCE_ID")) })
public class ExtraBallWinningItem extends BaseWinningItem {
    private static final long serialVersionUID = -2972441214694954417L;
    /**
     * it is prize level amount, that says the prize amount of this entry of this level is prizeAmount*numberOfPrize.
     */
    @Column(name = "PRIZE_AMOUNT")
    private BigDecimal prizeAmount = new BigDecimal("0");

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount = new BigDecimal("0");

    @Column(name = "ACTUAL_PAYOUT")
    private BigDecimal actualAmount = new BigDecimal("0");

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

}
