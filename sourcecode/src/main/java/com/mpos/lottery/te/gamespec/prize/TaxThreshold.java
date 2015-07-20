package com.mpos.lottery.te.gamespec.prize;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "WINNER_TAX_THRESHOLDS")
public class TaxThreshold implements Serializable {
    private static final long serialVersionUID = 7076349213591298064L;
    public static final int RULETYPE_FIXAMOUNT = 0;
    public static final int RULETYPE_PERCENTAGE = 1;
    public static final int TAXBASE_PRIZE = 0; // based on winning prize, before
                                               // tax.
    public static final int TAXBASE_PAYOUT = 1; // based on actual payout, after
                                                // tax.
    @Id
    @Column(name = "THRESHOLD_ID")
    private String id;

    @Column(name = "WINNER_TAX_POLICY_ID")
    private String taxPolicyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DATE_RANGE_ID", nullable = false)
    private TaxDateRange taxDateRange;

    @Column(name = "AMOUNT_FROM")
    private BigDecimal minAmount;

    @Column(name = "AMOUNT_To")
    private BigDecimal maxAmount;

    @Column(name = "TAX_RULE")
    private int ruleType;

    @Column(name = "TAX_VALUE")
    private BigDecimal taxAmount;

    @Column(name = "CALCULATION_METHOD")
    private int taxBase;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaxPolicyId() {
        return taxPolicyId;
    }

    public void setTaxPolicyId(String taxPolicyId) {
        this.taxPolicyId = taxPolicyId;
    }

    public TaxDateRange getTaxDateRange() {
        return taxDateRange;
    }

    public void setTaxDateRange(TaxDateRange taxDateRange) {
        this.taxDateRange = taxDateRange;
    }

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

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public int getTaxBase() {
        return taxBase;
    }

    public void setTaxBase(int taxBase) {
        this.taxBase = taxBase;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
