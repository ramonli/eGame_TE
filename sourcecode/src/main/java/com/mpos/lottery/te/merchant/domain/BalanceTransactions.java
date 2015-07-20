package com.mpos.lottery.te.merchant.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "BALANCE_TRANSACTIONS")
public class BalanceTransactions implements java.io.Serializable {
    private static final long serialVersionUID = 6139641558287268977L;
    public static int PAYMENT_TYPE_PLUSING_MONEY = 1;
    public static int PAYMENT_TYPE_DEDUCTING_MONEY = 2;

    public static int OWNER_TYPE_OPERATOR = 1;
    public static int OWNER_TYPE_MERCHANT = 2;

    public static int STATUS_INVALID = 0;
    public static int STATUS_VALID = 1;
    public static BigDecimal ZERO = new BigDecimal("0");

    public static int TRANSFER_SALE_BALANCE_TRANS_TYPE = 1161;
    public static int TRANSFER_PAYOUT_BALANCE_TRANS_TYPE = 1162;
    public static int TRANSFER_COMMISSION_BALANCE_TRANS_TYPE = 1163;
    public static int TRANSFER_CASHOUT_BALANCE_TRANS_TYPE = 1164;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "balance_transaction_seq")
    @SequenceGenerator(name = "balance_transaction_seq", sequenceName = "BALANCE_TRANSACTIONS_SEQ")
    private long id;

    @Column(name = "TE_TRANSACTION_ID")
    private String teTransactionId;

    @Column(name = "MERCHANT_ID")
    private long merchantId;

    @Column(name = "DEVICE_ID")
    private long deviceId;

    @Column(name = "OPERATOR_ID")
    private String operatorId;

    @Column(name = "OWNER_ID")
    private String ownerId;

    @Column(name = "OWNER_TYPE")
    private int ownerType;

    @Column(name = "PAYMENT_TYPE")
    private int paymentType;

    @Column(name = "TRANSACTION_TYPE")
    private int transactionType;

    @Column(name = "ORG_TRANSACTION_TYPE")
    private int originalTransType;

    @Column(name = "GAME_ID")
    private String gameId;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "BALANCE_AMOUNT")
    private BigDecimal transactionAmount = new BigDecimal("0");

    @Column(name = "COMMISION_AMOUNT")
    private BigDecimal commissionAmount = new BigDecimal("0");

    @Column(name = "UPDATE_TIME", nullable = true)
    private Timestamp updateTime = new Timestamp(System.currentTimeMillis());

    @Column(name = "COMMISION_RATE")
    private BigDecimal commissionRate = new BigDecimal("0");

    @Column(name = "FROM_PARENT_MERCHANT_ID")
    private Long fromParentMerchantId;

    @Column(name = "TO_PARENT_MERCHANT_ID")
    private Long toParentMerchantId;

    // refer to CREDIT_TYPE_XXX
    @Column(name = "CREATE_TIME")
    private Timestamp createTime = new Timestamp(System.currentTimeMillis());

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTeTransactionId() {
        return teTransactionId;
    }

    public void setTeTransactionId(String teTransactionId) {
        this.teTransactionId = teTransactionId;
    }

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(int ownerType) {
        this.ownerType = ownerType;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal balanceAmount) {
        this.transactionAmount = balanceAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commisionAmount) {
        this.commissionAmount = commisionAmount;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commisionRate) {
        this.commissionRate = commisionRate;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getOriginalTransType() {
        return originalTransType;
    }

    public void setOriginalTransType(int originalTransType) {
        this.originalTransType = originalTransType;
    }

    public Long getFromParentMerchantId() {
        return fromParentMerchantId;
    }

    public void setFromParentMerchantId(Long fromParentMerchantId) {
        this.fromParentMerchantId = fromParentMerchantId;
    }

    public Long getToParentMerchantId() {
        return toParentMerchantId;
    }

    public void setToParentMerchantId(Long toParentMerchantId) {
        this.toParentMerchantId = toParentMerchantId;
    }

}
