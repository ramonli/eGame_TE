package com.mpos.lottery.te.trans.domain;

import com.mpos.lottery.te.common.dao.VersionEntity;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "TE_TRANSACTION")
public class Transaction extends VersionEntity implements Cloneable {
    private static final long serialVersionUID = 4442383038683184008L;

    @Column(name = "OPERATOR_ID")
    private String operatorId;

    @Column(name = "GPE_ID")
    private String gpeId;

    @Column(name = "DEV_ID")
    private long deviceId;

    @Column(name = "MERCHANT_ID")
    private long merchantId;

    @Column(name = "TYPE")
    private int type; // refer to TransaciontType

    @Column(name = "TRACE_MESSAGE_ID")
    private String traceMessageId;

    @Column(name = "TRANS_TIMESTAMP")
    private Date transTimestamp;

    @Column(name = "RESPONSE_CODE")
    private int responseCode;

    @Column(name = "TICKET_SERIAL_NO")
    private String ticketSerialNo;

    @Column(name = "MERCHANTS")
    private String parentMerchants; // the parent merchants

    @Column(name = "BATCH_NO")
    private String batchNumber;

    @Column(name = "LONGTITUDE")
    private BigDecimal longitude;
    @Column(name = "LATITUDE")
    private BigDecimal latitude;

    @Column(name = "DESTINATION_OPEATOR")
    private String destinationOpeator;

    @Column(name = "VIRN")
    private String virn;

    @Column(name = "GAME_ID")
    private String gameId;

    /**
     * To a transaction which can be cancelled, such as sale, payout etc, this field means who cancelled it.
     * <p/>
     * However to a cancellation transaction itself, this field means which transaction has been cancelled by it.
     */
    @Column(name = "CANCEL_TE_TRANSACTION_ID")
    private String cancelTransactionId;

    /**
     * To a transaction which can be cancelled, such as sale, payout etc, this field means the transaction type of
     * cancellation transaction.
     * <p/>
     * However to a cancellation transaction itself, this field means the transaction type of the transaction which
     * transaction has been cancelled by it.
     */
    @Column(name = "CANCEL_TRANSACTION_TYPE")
    private Integer cancelTransactionType;

    /**
     * The total amount of transaction. To (multiple-draw)ticket, it is the sale/cancel total amount. To 'payout', it is
     * the total mount of payout(pay+return) after tax.
     */
    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount = new BigDecimal("0");

    // remove this association, or this association will always fetch
    // TransMessage eagerly, and
    // TransMessge maybe a big text.
    // @OneToOne(mappedBy="transaction",cascade={CascadeType.PERSIST,
    // CascadeType.REMOVE})
    @Transient
    private TransactionMessage transMessage;

    // use to generate response xml
    @Transient
    private BaseTicket ticket;

    @Transient
    private Object object;
    /**
     * Is the cancellation manual or auto? only affect when 'cancel by ticket'(201).
     */
    @Transient
    private boolean manualCancel;

    // @Column(name="SETTLEMENT_FLAG")
    @Transient
    private int settlementFlag;

    public Transaction() {
    }

    public Transaction(long deviceId, String traceMessageId) {
        super();
        this.deviceId = deviceId;
        this.traceMessageId = traceMessageId;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getGpeId() {
        return gpeId;
    }

    public void setGpeId(String gpeId) {
        this.gpeId = gpeId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTraceMessageId() {
        return traceMessageId;
    }

    public void setTraceMessageId(String traceMessageId) {
        this.traceMessageId = traceMessageId;
    }

    public Date getTransTimestamp() {
        return transTimestamp;
    }

    public void setTransTimestamp(Date transTimestamp) {
        this.transTimestamp = transTimestamp;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getTicketSerialNo() {
        return ticketSerialNo;
    }

    public void setTicketSerialNo(String ticketSerialNo) {
        this.ticketSerialNo = ticketSerialNo;
    }

    public TransactionMessage getTransMessage() {
        return transMessage;
    }

    public void setTransMessage(TransactionMessage transMessage) {
        this.transMessage = transMessage;
    }

    public BaseTicket getTicket() {
        return ticket;
    }

    public void setTicket(BaseTicket ticket) {
        this.ticket = ticket;
    }

    public String getParentMerchants() {
        return parentMerchants;
    }

    public void setParentMerchants(String parentMerchants) {
        this.parentMerchants = parentMerchants;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public int getSettlementFlag() {
        return settlementFlag;
    }

    public void setSettlementFlag(int settlementFlag) {
        this.settlementFlag = settlementFlag;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getVirn() {
        return virn;
    }

    public void setVirn(String virn) {
        this.virn = virn;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getDestinationOpeator() {
        return destinationOpeator;
    }

    public void setDestinationOpeator(String destinationOpeator) {
        this.destinationOpeator = destinationOpeator;
    }

    public String getCancelTransactionId() {
        return cancelTransactionId;
    }

    public void setCancelTransactionId(String cancelTransactionId) {
        this.cancelTransactionId = cancelTransactionId;
    }

    public Integer getCancelTransactionType() {
        return cancelTransactionType;
    }

    public void setCancelTransactionType(Integer targetTransTypeOfCancel) {
        this.cancelTransactionType = targetTransTypeOfCancel;
    }

    public boolean isManualCancel() {
        return manualCancel;
    }

    public void setManualCancel(boolean manualCancel) {
        this.manualCancel = manualCancel;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

}
