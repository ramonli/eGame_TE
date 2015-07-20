package com.mpos.lottery.te.merchant.domain;

import com.mpos.lottery.te.common.dao.BaseEntity;
import com.mpos.lottery.te.config.exception.SystemException;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BD_PRIZE_GROUP")
public class PrizeGroup extends BaseEntity {
    private static final long serialVersionUID = -2164348013117082448L;
    public static final int GROUP_TYPE_PAYOUT = 1;
    public static final int GROUP_TYPE_CASHOUT = 2;
    public static final int ALLOWTYPE_UNLIMIT = 1;
    public static final int ALLOWTYPE_USEPARENT = 4;
    public static final int ALLOWTYPE_DEFINITIVE_VALUE = 5;

    @Column(name = "ALLOW_PAYOUT")
    private boolean isPayoutAllowed;
    @Column(name = "MAX_VALUE")
    private BigDecimal maxPayoutAmount = new BigDecimal("-1");
    @Column(name = "MIN_VALUE")
    private BigDecimal minPayoutAmount = new BigDecimal("-1");
    // @Column(name = "GROUP_TYPE")
    // private int groupType;
    @Column(name = "CASH_OUT_DAY_LIMIT")
    private BigDecimal dailyCashoutLimit = new BigDecimal("0");
    @Column(name = "ALLOW_TYPE")
    private Integer allowType;

    public BigDecimal getMaxPayoutAmount() {
        return maxPayoutAmount;
    }

    public void setMaxPayoutAmount(BigDecimal maxPayoutAmount) {
        this.maxPayoutAmount = maxPayoutAmount;
    }

    public BigDecimal getMinPayoutAmount() {
        return minPayoutAmount;
    }

    public void setMinPayoutAmount(BigDecimal minPayoutAmount) {
        this.minPayoutAmount = minPayoutAmount;
    }

    public boolean isPayoutAllowed() {
        return isPayoutAllowed;
    }

    public void setPayoutAllowed(boolean isPayoutAllowed) {
        this.isPayoutAllowed = isPayoutAllowed;
    }

    // public int getGroupType() {
    // return groupType;
    // }
    //
    // public void setGroupType(int groupType) {
    // this.groupType = groupType;
    // }

    public BigDecimal getDailyCashoutLimit() {
        return this.dailyCashoutLimit != null ? this.dailyCashoutLimit : new BigDecimal("0");
    }

    public void setDailyCashoutLimit(BigDecimal dailyCashoutLimit) {
        this.dailyCashoutLimit = dailyCashoutLimit;
    }

    public Integer getAllowType() {
        if (allowType == null) {
            throw new SystemException("undetermined allow type, its value is null(prizeGroupID=" + this.getId() + ").");
        }
        return allowType;
    }

    public void setAllowType(Integer allowType) {
        this.allowType = allowType;
    }

}
