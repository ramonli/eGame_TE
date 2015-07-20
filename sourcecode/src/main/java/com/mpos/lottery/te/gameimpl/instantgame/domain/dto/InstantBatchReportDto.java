package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InstantBatchReportDto implements Serializable {

    /**
     * Annotation contents.
     */
    private static final long serialVersionUID = 1L;

    private BigDecimal actualAmount = new BigDecimal("0");

    private BigDecimal taxAmount = new BigDecimal("0");

    private long totalSuccess;

    private long totalFail;

    private long batchNumber;

    private List<InstantTicket> tickets = new ArrayList();

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

    public long getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(long batchNumber) {
        this.batchNumber = batchNumber;
    }

    public List<InstantTicket> getTickets() {
        return tickets;
    }

    public void setTickets(List<InstantTicket> tickets) {
        this.tickets = tickets;
    }

}
