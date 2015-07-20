package com.mpos.lottery.te.gamespec.prize;

import com.mpos.lottery.te.common.dao.VersionEntity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * A base winning statistics for all game type.
 * 
 * @author Ramon Li
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class BaseWinningStatistics extends VersionEntity {
    @Column(name = "GAME_INSTANCE_ID", nullable = false)
    private String gameInstanceId;

    @Column(name = "PRIZE_LEVEL")
    private int prizeLevel;

    @Column(name = "PRIZE_NUMBER")
    private int numberOfPrize; // how many prize are wined?

    @Column(name = "PRIZE_AMOUNT")
    private BigDecimal prizeAmount; // before tax

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount; // the tax amount

    @Column(name = "ACTUAL_PAYOUT")
    private BigDecimal actualAmount; // after tax

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public int getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(int prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public int getNumberOfPrize() {
        return numberOfPrize;
    }

    public void setNumberOfPrize(int numberOfPrize) {
        this.numberOfPrize = numberOfPrize;
    }

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
