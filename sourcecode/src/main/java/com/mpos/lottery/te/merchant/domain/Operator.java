package com.mpos.lottery.te.merchant.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Entity of operator.
 */
@Entity(name = "OPERATOR")
public class Operator implements Serializable {
    private static final long serialVersionUID = -5225159120408352315L;
    public static final int STATUS_INACTIVE = 0;
    public static final int STATUS_ACTIVE = 1;

    @Id
    @Column(name = "OPERATOR_ID")
    private String id;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "OPERATOR_TYPE")
    private String type;

    @Column(name = "LOGIN_NAME")
    private String loginName;

    @Column(name = "PASSWORD")
    private String password;

    /* refer to Merchant.CREDIT_TYPE_XXX */
    @Column(name = "LIMIT_TYPE")
    private int creditType = Merchant.CREDIT_TYPE_DEFINITIVEVALUE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BD_CASH_GROUP_ID", nullable = true)
    private PrizeGroup cashoutGroup; // payout group

    @Column(name = "CASH_OUT_DAY_LEVEL")
    private BigDecimal dailyCashoutLevel = new BigDecimal("0");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BD_PRIZE_GROUP_ID", nullable = true)
    private PrizeGroup prizeGroup;

    @Column(name = "IS_NEED_DO_ENCROLLMENT")
    private boolean needEnrollment;

    @Column(name = "IGNORE_CREDIT")
    private boolean ignoreCredit;

    @Column(name = "SALE_BALANCE")
    private BigDecimal saleCreditLevel = new BigDecimal("0");
    @Column(name = "PAYOUT_BALANCE")
    private BigDecimal payoutCreditLevel = new BigDecimal("0");

    @Column(name = "CASHOUT_BALANCE")
    private BigDecimal cashoutBalance = new BigDecimal("0");

    @Column(name = "COMMISION_BALANCE")
    private BigDecimal commisionBalance = new BigDecimal("0");

    @Column(name = "TOPUP_RATE")
    private BigDecimal topupReat = new BigDecimal("0");

    @Column(name = "CASHOUT_RATE")
    private BigDecimal cashoutRate = new BigDecimal("0");

    @Column(name = "UPDATE_TIME", nullable = true)
    private Timestamp updateTime = new Timestamp(System.currentTimeMillis());

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public int getCreditType() {
        return creditType;
    }

    public void setCreditType(int creditType) {
        this.creditType = creditType;
    }

    public BigDecimal getSaleCreditLevel() {
        return saleCreditLevel;
    }

    public void setSaleCreditLevel(BigDecimal saleCreditLevel) {
        this.saleCreditLevel = saleCreditLevel;
    }

    public BigDecimal getPayoutCreditLevel() {
        return payoutCreditLevel;
    }

    public void setPayoutCreditLevel(BigDecimal payoutCreditLevel) {
        this.payoutCreditLevel = payoutCreditLevel;
    }

    public PrizeGroup getCashoutGroup() {
        return cashoutGroup;
    }

    public void setCashoutGroup(PrizeGroup cashoutGroup) {
        this.cashoutGroup = cashoutGroup;
    }

    public BigDecimal getDailyCashoutLevel() {
        return this.dailyCashoutLevel == null ? new BigDecimal("0") : this.dailyCashoutLevel;
    }

    public void setDailyCashoutLevel(BigDecimal dailyCashoutLevel) {
        this.dailyCashoutLevel = dailyCashoutLevel;
    }

    public PrizeGroup getPrizeGroup() {
        return prizeGroup;
    }

    public void setPrizeGroup(PrizeGroup prizeGroup) {
        this.prizeGroup = prizeGroup;
    }

    public boolean isIgnoreCredit() {
        return ignoreCredit;
    }

    public void setIgnoreCredit(boolean ignoreCredit) {
        this.ignoreCredit = ignoreCredit;
    }

    public boolean isNeedEnrollment() {
        return needEnrollment;
    }

    public void setNeedEnrollment(boolean needEnrollment) {
        this.needEnrollment = needEnrollment;
    }

    public BigDecimal getCashoutBalance() {
        return cashoutBalance;
    }

    @Override
    public String toString() {
        return "Operator [id=" + id + ", loginName=" + loginName + ", creditType=" + creditType + ", saleCreditLevel="
                + saleCreditLevel + ", payoutCreditLevel=" + payoutCreditLevel + ", cashoutBalance=" + cashoutBalance
                + ", commisionBalance=" + commisionBalance + "]";
    }

    public void setCashoutBalance(BigDecimal cashoutBalance) {
        this.cashoutBalance = cashoutBalance;
    }

    public BigDecimal getCommisionBalance() {
        return commisionBalance;
    }

    public void setCommisionBalance(BigDecimal commisionBalance) {
        this.commisionBalance = commisionBalance;
    }

    public BigDecimal getTopupReat() {
        return topupReat;
    }

    public void setTopupReat(BigDecimal topupReat) {
        this.topupReat = topupReat;
    }

    public BigDecimal getCashoutRate() {
        return cashoutRate;
    }

    public void setCashoutRate(BigDecimal cashoutRate) {
        this.cashoutRate = cashoutRate;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
