package com.mpos.lottery.te.gameimpl.magic100.sale;

import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "LK_PRIZE_PARAMETERS")
public class LuckyNumber implements Serializable {
    private static final long serialVersionUID = -5255082186005575960L;
    @Id
    @Column(name = "ID")
    private String id;
    /**
     * <code>sequenceOfNumber</code> must be sequential, and step by 1. The lucky number will be sold from the smallest
     * to the largest.
     */
    @Column(name = "NUMBER_SEQ")
    private long sequenceOfNumber;
    @Column(name = "LUCKY_NUM")
    private String luckyNumber;
    @Column(name = "PRICE_AMOUNT")
    private BigDecimal prizeAmount = new BigDecimal("0");
    @Transient
    private BigDecimal taxAmount = new BigDecimal("0");
    /**
     * If client requests to cancel a winning lucky number, the cancel counter of this number will be increased by 1 to
     * indicate that this lucky number must be sold one more time at next cycle.
     */
    @Column(name = "CANCEL_COUNTER")
    private int cancelCounter;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LK_GAME_INSTACE_ID", nullable = false)
    private Magic100GameInstance gameInstance;
    @Column(name = "UPDATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    public LuckyNumber() {
    }

    public LuckyNumber(String id, long sequenceOfNumber, String luckyNumber, BigDecimal prizeAmount, int cancelCounter,
            Magic100GameInstance gameInstance) {
        super();
        this.id = id;
        this.sequenceOfNumber = sequenceOfNumber;
        this.luckyNumber = luckyNumber;
        this.prizeAmount = prizeAmount;
        this.cancelCounter = cancelCounter;
        this.gameInstance = gameInstance;
    }

    public LuckyNumber(RequeuedNumbersItem requeueItem) {
        super();
        this.sequenceOfNumber = requeueItem.getSequenceOfNumber();
        this.luckyNumber = requeueItem.getLuckyNumber();
        this.prizeAmount = requeueItem.getPrizeAmount();
        this.taxAmount = requeueItem.getTaxAmount();
    }

    /**
     * Whether this lucky nujber is winning?
     * 
     * @return true if winning, otherwise false.
     */
    public boolean isWinning() {
        if (this.getPrizeAmount().compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        return false;
    }

    /**
     * THe id of a <code>LuckyNumber</code> must be sequential, and step by 1. System will sell the lucky number from
     * smallest to largest by its ID.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLuckyNumber() {
        return luckyNumber;
    }

    public void setLuckyNumber(String luckyNumber) {
        this.luckyNumber = luckyNumber;
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

    public int getCancelCounter() {
        return cancelCounter;
    }

    public void setCancelCounter(int cancelCounter) {
        this.cancelCounter = cancelCounter;
    }

    public Magic100GameInstance getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(Magic100GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public long getSequenceOfNumber() {
        return sequenceOfNumber;
    }

    public void setSequenceOfNumber(long sequenceOfNumber) {
        this.sequenceOfNumber = sequenceOfNumber;
    }

}
