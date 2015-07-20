package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class ConfirmBatchPayoutDto implements Serializable {
    private static final long serialVersionUID = -5773948148987471128L;
    private BigDecimal actualAmount = new BigDecimal("0");
    private BigDecimal taxAmount = new BigDecimal("0");
    private long totalSuccess;
    private long totalFail;
    private long batchNumber;

    private List<PrizeLevelDto> payouts = new LinkedList<PrizeLevelDto>();

    public long getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(long batchNumber) {
        this.batchNumber = batchNumber;
    }

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

    public long getTotalSuccess() {
        return totalSuccess;
    }

    public void setTotalSuccess(long totalSuccess) {
        this.totalSuccess = totalSuccess;
    }

    public long getTotalFail() {
        return totalFail;
    }

    public void setTotalFail(long totalFail) {
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
