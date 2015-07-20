package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.common.dao.VersionEntity;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "IG_PAYOUT_TEMP")
public class IGPayoutTemp extends VersionEntity {
    private static final long serialVersionUID = -5971451175462596149L;
    public static final int TYPE_WINNING = 1;
    public static final int TYPE_RETURN = 2;
    public static final int TYPE_LUCKY = 3;

    public static final int STATUS_PAID = 1;
    public static final int STATUS_REVERSED = 2;

    public static final int INPUT_CHANNEL_MANUAL = 1;
    public static final int INPUT_CHANNEL_SCANNER = 2;

    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "TRANSACTION_ID", nullable = false)
    private Transaction transaction;

    @Column(name = "TICKET_SERIALNO")
    private String ticketSerialNo;

    // the prize amount after tax
    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    // cash prize amount, before tax.
    @Column(name = "TOTAL_AMOUNT_B4_TAX")
    private BigDecimal beforeTaxTotalAmount = new BigDecimal("0");

    // object prize amount, before tax
    @Column(name = "OBJECT_AMOUNT")
    private BigDecimal beforeTaxObjectAmount = new BigDecimal("0");

    @Column(name = "OBJECT_NUM")
    private int numberOfObject;

    @Column(name = "TYPE")
    private int type;

    @Column(name = "IS_VALID")
    private boolean isValid = true;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "IS_BY_MANUAL")
    private int inputChannel;

    @Column(name = "OPERATOR_ID")
    private String operatorId;

    @Column(name = "DEV_ID")
    private long devId;

    @Column(name = "MERCHANT_ID")
    private int merchantId;

    @Column(name = "GAME_ID")
    private String gameId;

    @Column(name = "IG_BATCH_NUMBER")
    private long iGBatchNumber;

    @Transient
    private BaseGameInstance gameInstance;
    @Transient
    private List<IGPayoutDetailTemp> payoutDetails = new LinkedList<IGPayoutDetailTemp>();

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public long getDevId() {
        return devId;
    }

    public void setDevId(long devId) {
        this.devId = devId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public BaseGameInstance getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getTicketSerialNo() {
        return ticketSerialNo;
    }

    public void setTicketSerialNo(String ticketSerialNo) {
        this.ticketSerialNo = ticketSerialNo;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BigDecimal getBeforeTaxTotalAmount() {
        return beforeTaxTotalAmount;
    }

    public void setBeforeTaxTotalAmount(BigDecimal beforeTaxTotalAmount) {
        this.beforeTaxTotalAmount = beforeTaxTotalAmount;
    }

    public int getInputChannel() {
        return inputChannel;
    }

    public void setInputChannel(int inputChannel) {
        this.inputChannel = inputChannel;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public BigDecimal getBeforeTaxObjectAmount() {
        return beforeTaxObjectAmount;
    }

    public void setBeforeTaxObjectAmount(BigDecimal beforeTaxObjectAmount) {
        this.beforeTaxObjectAmount = beforeTaxObjectAmount;
    }

    public int getNumberOfObject() {
        return numberOfObject;
    }

    public void setNumberOfObject(int numberOfObject) {
        this.numberOfObject = numberOfObject;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getiGBatchNumber() {
        return iGBatchNumber;
    }

    public void setiGBatchNumber(long iGBatchNumber) {
        this.iGBatchNumber = iGBatchNumber;
    }

    public List<IGPayoutDetailTemp> getPayoutDetails() {
        return payoutDetails;
    }

    public void setPayoutDetails(List<IGPayoutDetailTemp> payoutDetails) {
        this.payoutDetails = payoutDetails;
    }

}
