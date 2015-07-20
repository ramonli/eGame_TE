package com.mpos.lottery.te.gameimpl.magic100.sale;

import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "LK_TE_ENTRY")
public class Magic100Entry extends BaseEntry implements Cloneable {

    private static final long serialVersionUID = 419711008778901622L;

    @Column(name = "NUMBER_SEQ")
    private long sequenceOfNumber;

    @Column(name = "PRIZE_AMOUNT")
    private BigDecimal prizeAmount = new BigDecimal("0");
    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount = new BigDecimal("0");
    @Column(name = "IS_WINNING")
    private boolean winning;

    public static Magic100Entry defaultEntry() {
        Magic100Entry entry = new Magic100Entry();
        entry.setBetOption(BETOPTION_SINGLE);
        entry.setInputChannel(INPUT_CHANNEL_NOTQP_NOTOMR);
        entry.setSelectNumber(DEFAULT_SELECTED_NUMBER);
        entry.setTotalBets(1);
        return entry;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        if (prizeAmount != null) {
            this.prizeAmount = prizeAmount;
        }
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        if (taxAmount != null) {
            this.taxAmount = taxAmount;
        }
    }

    public long getSequenceOfNumber() {
        return sequenceOfNumber;
    }

    public void setSequenceOfNumber(long sequenceOfNumber) {
        this.sequenceOfNumber = sequenceOfNumber;
    }

    public boolean isWinning() {
        return winning;
    }

    public void setWinning(boolean isWinning) {
        this.winning = isWinning;
    }

}
