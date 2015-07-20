package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class InstantBatchPayoutDto implements Serializable {
    private static final long serialVersionUID = -5773948148987471128L;
    private BigDecimal actualAmount = new BigDecimal("0");
    private BigDecimal taxAmount = new BigDecimal("0");
    private int totalSuccess;
    private int totalFail;
    private List<PrizeLevelDto> payouts = new LinkedList<PrizeLevelDto>();

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public int getTotalSuccess() {
        return totalSuccess;
    }

    public void setTotalSuccess(int totalSuccess) {
        this.totalSuccess = totalSuccess;
    }

    public int getTotalFail() {
        return totalFail;
    }

    public void setTotalFail(int totalFail) {
        this.totalFail = totalFail;
    }

    public List<PrizeLevelDto> getPayouts() {
        return payouts;
    }

    public void setPayouts(List<PrizeLevelDto> payouts) {
        this.payouts = payouts;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
