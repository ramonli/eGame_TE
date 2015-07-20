package com.mpos.lottery.te.gamespec.sale.support;

import org.springframework.util.Assert;

import java.math.BigDecimal;

/**
 * A entry may have multiple chances to win prize, for example a entry '1,2,3,4,5,6,7' of LOTTO game 6/29 will have 7
 * chance to win a prize. This class will represent a chance of a given entry.
 * 
 * @author Ramon
 */
public class ChanceOfEntry {
    // the bet option of chance
    private int betOption;
    // the selected number of chance
    private String selectedNumber;
    // the betting amount of this chance.
    private BigDecimal amount = new BigDecimal("0");

    public ChanceOfEntry(String selectedNumber, BigDecimal amount, int betOption) {
        Assert.notNull(selectedNumber);
        Assert.notNull(amount);
        this.selectedNumber = selectedNumber;
        this.amount = amount;
        this.betOption = betOption;
    }

    public String getSelectedNumber() {
        return selectedNumber;
    }

    public void setSelectedNumber(String selectedNumber) {
        this.selectedNumber = selectedNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getBetOption() {
        return betOption;
    }

    public void setBetOption(int betOption) {
        this.betOption = betOption;
    }

}
