package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "INSTANT_TICKET")
public class InstantTicket {
    public static final int STATUS_INACTIVE = 1;
    public static final int STATUS_SOLD = 2; // sold to distributor.
    public static final int STATUS_ACTIVE = 3;
    public static final int STATUS_VALIDATED = 5;
    public static final int STATUS_PROCESSING = 6;// for confirm batch validation

    public static final int PHYSICAL_STATUS_DAMAGED = -1;
    public static final int PHYSICAL_STATUS_INSTOCK = 0;
    public static final int PHYSICAL_STATUS_STOCKOUT = 1; // stock out by sales
    public static final int PHYSICAL_STATUS_RETURN = 2; // returned by sales
    public static final int PHYSICAL_STATUS_CASHOUT = 3; // cash out by sales

    @Id
    @Column(name = "ID")
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IG_GAME_INSTANCE_ID", nullable = false)
    private InstantGameDraw gameDraw;

    // Due to different game instance may be assign same book number, here
    // 'GGGBBBBBB' will be
    // stored to 'BOOK_NUMBER' column
    @Column(name = "BOOK_NUMBER")
    private String bookNumber;

    // The data format of serialNO should be 'GGGBBBBBBTTT,'
    @Column(name = "TICKET_SERIAL")
    private String serialNo;

    @Transient
    private String rawSerialNo;

    @Column(name = "TICKET_MAC")
    private String ticketMAC;

    // XOR1 and XOR2 are for EGAME
    @Column(name = "TICKET_XOR1")
    private String ticketXOR1;

    @Column(name = "TICKET_XOR2")
    private String ticketXOR2;

    // the 'VIRN' uploaded by client will be set to this field.
    @Column(name = "TICKET_XOR3")
    private String ticketXOR3;

    @Column(name = "REFERENCE_INDEX")
    private Integer prizeLevelIndex;

    @Column(name = "SOLD_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date soldTime;

    @Column(name = "IS_SOLD_TO_CUSTOMER")
    private boolean isSoldToCustomer;

    @Column(name = "STATUS")
    private int status; // refer to static final STATUS_XXX

    @Column(name = "IS_IN_BLACKLIST")
    private boolean isInBlacklist;

    @Column(name = "IS_SUSPEND_ACTIVATION")
    private boolean suspendActivation;

    @Column(name = "IS_SUSPEND_PAYOUT")
    private boolean suspendValidation;

    @Column(name = "PRIZE_LEVEL", nullable = true)
    private Integer prizeLevel;

    // refer to PHYSICAL_STATUS_XXX
    @Column(name = "PHYSICAL_STATUS")
    private int physicalStatus;

    // If the associated validation transaction has been settled, if true, this
    // ticket
    // can be move to history by archiving process...NOT USE currently.
    @Column(name = "IS_VALIDATION_SETTLED")
    private boolean validationSettled;

    // a MD5 of XOR string, hex string
    @Column(name = "XOR_MD5")
    private String xorMD5;

    @Column(name = "PRIZE_AMOUNT")
    private BigDecimal prizeAmount; // before tax

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount;

    @Column(name = "OPERATOR_ID")
    private String operatorId;

    @Column(name = "DEV_ID")
    private String devId;

    @Column(name = "MERCHANT_ID")
    private String merchantId;

    @Column(name = "SALE_TRANSACTION_ID")
    private String saleTransId;

    @Transient
    private int inputChannel;

    // only for cliet response, the face amount of ticket.
    @Transient
    private BigDecimal totalAmount;

    @Transient
    private int errorCode;

    @Transient
    private Transaction transaction;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(String packetSerialNo) {
        this.bookNumber = packetSerialNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getTicketMAC() {
        return ticketMAC;
    }

    public void setTicketMAC(String ticketMAC) {
        this.ticketMAC = ticketMAC;
    }

    public String getTicketXOR1() {
        return ticketXOR1;
    }

    public void setTicketXOR1(String ticketXOR1) {
        this.ticketXOR1 = ticketXOR1;
    }

    public String getTicketXOR3() {
        return ticketXOR3;
    }

    public void setTicketXOR3(String ticketXOR3) {
        this.ticketXOR3 = ticketXOR3;
    }

    public Integer getPrizeLevelIndex() {
        return prizeLevelIndex;
    }

    public void setPrizeLevelIndex(Integer prizeLevelIndex) {
        this.prizeLevelIndex = prizeLevelIndex;
    }

    public Date getSoldTime() {
        return soldTime;
    }

    public void setSoldTime(Date soldTime) {
        this.soldTime = soldTime;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public InstantGameDraw getGameDraw() {
        return gameDraw;
    }

    public void setGameDraw(InstantGameDraw gameDraw) {
        this.gameDraw = gameDraw;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isInBlacklist() {
        return isInBlacklist;
    }

    public void setInBlacklist(boolean isInBlacklist) {
        this.isInBlacklist = isInBlacklist;
    }

    public boolean isSoldToCustomer() {
        return isSoldToCustomer;
    }

    public void setSoldToCustomer(boolean isSoldToCustomer) {
        this.isSoldToCustomer = isSoldToCustomer;
    }

    public String getTicketXOR2() {
        return ticketXOR2;
    }

    public void setTicketXOR2(String ticketXOR2) {
        this.ticketXOR2 = ticketXOR2;
    }

    public boolean isSuspendActivation() {
        return suspendActivation;
    }

    public void setSuspendActivation(boolean suspendActivation) {
        this.suspendActivation = suspendActivation;
    }

    public boolean isSuspendValidation() {
        return suspendValidation;
    }

    public void setSuspendValidation(boolean suspendValidation) {
        this.suspendValidation = suspendValidation;
    }

    public Integer getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(Integer prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isValidationSettled() {
        return validationSettled;
    }

    public void setValidationSettled(boolean validationSettled) {
        this.validationSettled = validationSettled;
    }

    public String getXorMD5() {
        return xorMD5;
    }

    public void setXorMD5(String xorMD5) {
        this.xorMD5 = xorMD5;
    }

    public int getPhysicalStatus() {
        return physicalStatus;
    }

    public void setPhysicalStatus(int physicalStatus) {
        this.physicalStatus = physicalStatus;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public int getInputChannel() {
        return inputChannel;
    }

    public void setInputChannel(int inputChannel) {
        this.inputChannel = inputChannel;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getSaleTransId() {
        return saleTransId;
    }

    public void setSaleTransId(String saleTransId) {
        this.saleTransId = saleTransId;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getRawSerialNo() {
        return rawSerialNo == null ? this.serialNo : this.rawSerialNo;
    }

    public void setRawSerialNo(String rawSerialNo) {
        this.setRawSerialNo(true, rawSerialNo);
    }

    public void setRawSerialNo(boolean needToUpdateSerialNo, String rawSerialNo) {
        this.rawSerialNo = rawSerialNo;
        if (needToUpdateSerialNo) {
            this.setSerialNo(BaseTicket.encryptSerialNo(rawSerialNo));
        }
    }
}
