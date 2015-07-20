package com.mpos.lottery.te.gameimpl.lfn.prize;

import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LFN_WINNING")
@AttributeOverrides({ @AttributeOverride(name = "gameInstanceId", column = @Column(name = "LFN_GAME_INSTANCE_ID")) })
public class LfnWinningItem extends BaseWinningItem {

    private static final long serialVersionUID = 5325630426375066662L;
    /**
     * it is prize level amount, that says the prize amount of this entry of this level is prizeAmount*numberOfPrize
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

    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
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

}
