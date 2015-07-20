package com.mpos.lottery.te.merchant.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name = "CASHOUT_PASS")
@Entity
public class CashoutPass implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "OPERATOR_ID")
    private String operatorId;

    @Column(name = "CASHOUT_AMOUNT")
    private BigDecimal cashoutAmount = new BigDecimal("0");

    @Column(name = "CASHOUT_PASSWORD")
    private String cashoutPassword;

    @Column(name = "EXPIRE_TIME")
    private Date expireTime;

    @Column(name = "TRIED_TIMES")
    private int triedTimes;

    @Column(name = "TE_TRANSACTION_ID")
    private String teTransactionId;

    @Column(name = "CASHOUT_BARCODE")
    private String cashoutBarCode;

    @Column(name = "CASHOUT_TE_TRANSACTION_ID")
    private String cashoutTeTransactionId;

    @Column(name = "CREATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "CREATE_BY")
    private String createBy;

    @Column(name = "UPDATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "UPDATE_BY")
    private String updateBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public BigDecimal getCashoutAmount() {
        return cashoutAmount;
    }

    public void setCashoutAmount(BigDecimal cashoutAmount) {
        this.cashoutAmount = cashoutAmount;
    }

    public String getCashoutPassword() {
        return cashoutPassword;
    }

    public void setCashoutPassword(String cashoutPassword) {
        this.cashoutPassword = cashoutPassword;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public int getTriedTimes() {
        return triedTimes;
    }

    public void setTriedTimes(int triedTimes) {
        this.triedTimes = triedTimes;
    }

    public String getTeTransactionId() {
        return teTransactionId;
    }

    public void setTeTransactionId(String teTransactionId) {
        this.teTransactionId = teTransactionId;
    }

    public String getCashoutBarCode() {
        return cashoutBarCode;
    }

    public void setCashoutBarCode(String cashoutBarCode) {
        this.cashoutBarCode = cashoutBarCode;
    }

    public String getCashoutTeTransactionId() {
        return cashoutTeTransactionId;
    }

    public void setCashoutTeTransactionId(String cashoutTeTransactionId) {
        this.cashoutTeTransactionId = cashoutTeTransactionId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

}
