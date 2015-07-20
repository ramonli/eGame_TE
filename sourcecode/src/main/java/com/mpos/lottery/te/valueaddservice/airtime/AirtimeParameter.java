package com.mpos.lottery.te.valueaddservice.airtime;

import com.mpos.lottery.te.common.dao.BaseEntity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "AIRTIME_PARAMETERS")
public class AirtimeParameter extends BaseEntity {
    private static final long serialVersionUID = 6067920268840100244L;
    @Column(name = "MINIMUM_AMOUNT")
    private BigDecimal minAmount = new BigDecimal("0");
    @Column(name = "MAXIMUM_AMOUNT")
    private BigDecimal maxAmount = new BigDecimal("0");
    @Column(name = "IS_SUPPORT_STEPPING")
    private boolean amountStepSupport;
    @Column(name = "BET_AMOUNT_STEPPING")
    private BigDecimal stepAmount = new BigDecimal("0");
    @Column(name = "TELCO_COMPANY_ID")
    private int airtimeProviderId;

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public boolean isAmountStepSupport() {
        return amountStepSupport;
    }

    public void setAmountStepSupport(boolean amountStepSupport) {
        this.amountStepSupport = amountStepSupport;
    }

    public BigDecimal getStepAmount() {
        return stepAmount;
    }

    public void setStepAmount(BigDecimal stepAmount) {
        this.stepAmount = stepAmount;
    }

    public int getAirtimeProviderId() {
        return airtimeProviderId;
    }

    public void setAirtimeProviderId(int airtimeProviderId) {
        this.airtimeProviderId = airtimeProviderId;
    }

}
