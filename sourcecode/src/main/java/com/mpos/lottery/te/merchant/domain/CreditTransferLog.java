package com.mpos.lottery.te.merchant.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "DW_MERCHANT_TOPUP_LOG")
@Entity
public class CreditTransferLog implements Serializable {
    private static final long serialVersionUID = -7922987068379865365L;
    public static final int TRANSFER_TYPE_WEB = 1;
    public static final int TRANSFER_TYPE_POS = 2;
    public static final int STATUS_VALID = 1;
    public static final int STATUS_REVERSAL = 2;
    public static final int TARGET_TYPE_CARD = 0;
    public static final int TARGET_TYPE_MERCHANT = 1;

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "TRANSACTION_ID")
    private String transactionId;

    @Column(name = "OPERATOR_ID")
    private String fromOperatorId;
    @Column(name = "MERCHANT_ID")
    private long fromMerchantId;
    @Column(name = "TOPUP_OPERATOR_ID")
    private String toOperatorId;
    @Column(name = "TOPUP_DEVICE_ID")
    private long deviceId;
    @Column(name = "TOPUP_MERCHANT_ID")
    private long toMerchantId;
    @Column(name = "TOPUP_MERCHANT_CODE")
    private String toMerchantCode;
    @Column(name = "TOPUP_MERCHANT_NAME")
    private String toMerchantName;

    @Column(name = "TOPUP_SALES_BALANCE_AMOUNT")
    private BigDecimal saleCreditOfTransfer = new BigDecimal("0");
    @Column(name = "TOPUP_PAYOUT_BALANCE_AMOUNT")
    private BigDecimal payoutCreditOfTransfer = new BigDecimal("0");

    @Column(name = "T_SALES_BALANCE_AFTER_AMOUNT")
    private BigDecimal saleCreditOfToOperatorAfter = new BigDecimal("0");
    @Column(name = "T_PAYOUT_BALANCE_AFTER_AMOUNT")
    private BigDecimal payoutCreditOfToOperatorAfter = new BigDecimal("0");

    @Column(name = "SALES_BALANCE_AFTER_AMOUNT")
    private BigDecimal saleCreditOfFromOperatorAfter = new BigDecimal("0");
    @Column(name = "PAYOUT_BALANCE_AFTER_AMOUNT")
    private BigDecimal payoutCreditOfFromOperatorAfter = new BigDecimal("0");

    // refer to TRANSFER_TYPE_XXX
    @Column(name = "TOPUP_TYPE")
    private int transferType;
    @Column(name = "CREATE_TIME")
    private Date createTime;
    @Column(name = "UPDATE_TIME")
    private Date updateTime;
    // refer to STATUS_XXX
    @Column(name = "STATUS")
    private int status;
    // Topup to who? refer to TARGET_TYPE_XXX
    @Column(name = "IS_CARD")
    private int targetType;

    /**
     * Default non-arg constructor for JPA loading.
     */
    public CreditTransferLog() {
    }

    public CreditTransferLog(String id, String transactionId, String fromOperatorId, long fromMerchantId,
            String toOperatorId, long deviceId, long toMerchantId, String toMerchantCode, String toMerchantName,
            BigDecimal saleCreditOfTransfer, BigDecimal payoutCreditOfTransfer, Date createTime) {
        super();
        this.id = id;
        this.transactionId = transactionId;
        this.fromOperatorId = fromOperatorId;
        this.fromMerchantId = fromMerchantId;
        this.toOperatorId = toOperatorId;
        this.deviceId = deviceId;
        this.toMerchantId = toMerchantId;
        this.toMerchantCode = toMerchantCode;
        this.toMerchantName = toMerchantName;
        this.saleCreditOfTransfer = saleCreditOfTransfer;
        this.payoutCreditOfTransfer = payoutCreditOfTransfer;
        this.createTime = createTime;
        this.updateTime = this.createTime;
        this.status = STATUS_VALID;
        this.transferType = TRANSFER_TYPE_POS;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getFromOperatorId() {
        return fromOperatorId;
    }

    public void setFromOperatorId(String fromOperatorId) {
        this.fromOperatorId = fromOperatorId;
    }

    public long getFromMerchantId() {
        return fromMerchantId;
    }

    public void setFromMerchantId(long fromMerchantId) {
        this.fromMerchantId = fromMerchantId;
    }

    public String getToOperatorId() {
        return toOperatorId;
    }

    public void setToOperatorId(String toOperatorId) {
        this.toOperatorId = toOperatorId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getToMerchantId() {
        return toMerchantId;
    }

    public void setToMerchantId(long toMerchantId) {
        this.toMerchantId = toMerchantId;
    }

    public String getToMerchantCode() {
        return toMerchantCode;
    }

    public void setToMerchantCode(String toMerchantCode) {
        this.toMerchantCode = toMerchantCode;
    }

    public String getToMerchantName() {
        return toMerchantName;
    }

    public void setToMerchantName(String toMerchantName) {
        this.toMerchantName = toMerchantName;
    }

    public BigDecimal getSaleCreditOfTransfer() {
        return saleCreditOfTransfer;
    }

    public void setSaleCreditOfTransfer(BigDecimal saleCreditOfTransfer) {
        this.saleCreditOfTransfer = saleCreditOfTransfer;
    }

    public BigDecimal getPayoutCreditOfTransfer() {
        return payoutCreditOfTransfer;
    }

    public void setPayoutCreditOfTransfer(BigDecimal payoutCreditOfTransfer) {
        this.payoutCreditOfTransfer = payoutCreditOfTransfer;
    }

    public BigDecimal getSaleCreditOfToOperatorAfter() {
        return saleCreditOfToOperatorAfter;
    }

    public void setSaleCreditOfToOperatorAfter(BigDecimal saleCreditOfToOperatorAfter) {
        this.saleCreditOfToOperatorAfter = saleCreditOfToOperatorAfter;
    }

    public BigDecimal getPayoutCreditOfToOperatorAfter() {
        return payoutCreditOfToOperatorAfter;
    }

    public void setPayoutCreditOfToOperatorAfter(BigDecimal payoutCreditOfToOperatorAfter) {
        this.payoutCreditOfToOperatorAfter = payoutCreditOfToOperatorAfter;
    }

    public BigDecimal getSaleCreditOfFromOperatorAfter() {
        return saleCreditOfFromOperatorAfter;
    }

    public void setSaleCreditOfFromOperatorAfter(BigDecimal saleCreditOfFromOperatorAfter) {
        this.saleCreditOfFromOperatorAfter = saleCreditOfFromOperatorAfter;
    }

    public BigDecimal getPayoutCreditOfFromOperatorAfter() {
        return payoutCreditOfFromOperatorAfter;
    }

    public void setPayoutCreditOfFromOperatorAfter(BigDecimal payoutCreditOfFromOperatorAfter) {
        this.payoutCreditOfFromOperatorAfter = payoutCreditOfFromOperatorAfter;
    }

    public int getTransferType() {
        return transferType;
    }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public int getAdjustmentType() {
        return transferType;
    }

    public void setAdjustmentType(int adjustmentType) {
        this.transferType = adjustmentType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

}
