package com.mpos.lottery.te.gamespec.sale;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BD_RISK_BETTING")
public class RiskControlLog {
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;
    @Column(name = "BETTING_NUMBER")
    private String selectedNumber;
    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount = new BigDecimal("0");
    @Column(name = "PRIZE_LEVEL_TYPE")
    private int prizeLevelType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public String getSelectedNumber() {
        return selectedNumber;
    }

    public void setSelectedNumber(String selectedNumber) {
        this.selectedNumber = selectedNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getPrizeLevelType() {
        return prizeLevelType;
    }

    public void setPrizeLevelType(int prizeLevelType) {
        this.prizeLevelType = prizeLevelType;
    }

    @Override
    public String toString() {
        return "RiskControlLog [gameInstanceId=" + gameInstanceId + ", selectedNumber=" + selectedNumber
                + ", totalAmount=" + totalAmount + ", prizeLevelType=" + prizeLevelType + "]";
    }

}
