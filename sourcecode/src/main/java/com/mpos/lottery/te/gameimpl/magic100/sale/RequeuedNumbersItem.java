package com.mpos.lottery.te.gameimpl.magic100.sale;

import com.mpos.lottery.te.common.dao.BaseEntity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "LK_REQUEUE_NUMBERS_ITEM")
public class RequeuedNumbersItem extends BaseEntity {
    public static final int STATE_VALID = 1;
    public static final int STATE_INVALID = 0;
    private static final long serialVersionUID = 5162049097712782497L;
    @ManyToOne
    @JoinColumn(name = "LK_REQUEUE_NUMBERS_ID")
    private RequeuedNumbers requeuedNumbers;
    @Column(name = "NUMBER_SEQ ")
    private long sequenceOfNumber;
    @Column(name = "LUCKY_NUM")
    private String luckyNumber;
    @Column(name = "PRIZE_AMOUNT")
    private BigDecimal prizeAmount = new BigDecimal("0");
    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount = new BigDecimal("0");
    @Column(name = "STATE")
    private int state = STATE_INVALID;

    public RequeuedNumbersItem() {
    }

    public RequeuedNumbersItem(LuckyNumber luckyNumber) {
        this.sequenceOfNumber = luckyNumber.getSequenceOfNumber();
        this.luckyNumber = luckyNumber.getLuckyNumber();
        this.prizeAmount = luckyNumber.getPrizeAmount();
        this.taxAmount = luckyNumber.getTaxAmount();
    }

    public RequeuedNumbers getRequeuedNumbers() {
        return requeuedNumbers;
    }

    public void setRequeuedNumbers(RequeuedNumbers requeuedNumbers) {
        this.requeuedNumbers = requeuedNumbers;
    }

    public long getSequenceOfNumber() {
        return sequenceOfNumber;
    }

    public void setSequenceOfNumber(long sequenceOfNumber) {
        this.sequenceOfNumber = sequenceOfNumber;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

}
