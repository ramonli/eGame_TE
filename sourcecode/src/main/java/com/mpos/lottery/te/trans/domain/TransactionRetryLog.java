package com.mpos.lottery.te.trans.domain;

import com.mpos.lottery.te.common.dao.VersionEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "TRANSACTION_RETRY_LOG")
public class TransactionRetryLog extends VersionEntity {
    private static final long serialVersionUID = -7827630300779217369L;
    // here we will use version column to represent whether a retry log is valid(soft removed).
    public static final int VERSION_VALID = 1;
    public static final int VERSION_INVALID = 99;

    @Column(name = "TOTOAL_TRY")
    private int totalRetry;

    @Column(name = "TICKET_SERIALNO")
    private String ticketSerialNo;

    @Column(name = "DEVICE_ID")
    private long deviceId;

    @Column(name = "TRANS_TYPE")
    private int transType;

    @Transient
    private int allowedRetry;

    public int getTotalRetry() {
        return totalRetry;
    }

    public void setTotalRetry(int totalRetry) {
        this.totalRetry = totalRetry;
    }

    public String getTicketSerialNo() {
        return ticketSerialNo;
    }

    public void setTicketSerialNo(String ticketSerialNo) {
        this.ticketSerialNo = ticketSerialNo;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public int getAllowedRetry() {
        return allowedRetry;
    }

    public void setAllowedRetry(int allowedRetry) {
        this.allowedRetry = allowedRetry;
    }

    public boolean isExceedMaxValidationTimes() {
        if (this.totalRetry > this.allowedRetry) {
            return true;
        }
        return false;
    }
}
