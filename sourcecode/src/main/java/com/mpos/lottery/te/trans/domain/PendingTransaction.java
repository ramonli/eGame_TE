package com.mpos.lottery.te.trans.domain;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TE_PENDING_TRANSACTION")
public class PendingTransaction {
    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "TICKET_SERIAL_NO")
    private String ticketSerialNo;
    @Column(name = "DEVICE_ID")
    private long deviceId;
    @Column(name = "TRACE_MESSAGE_ID")
    private String traceMsgId;
    @Column(name = "IS_DEAL")
    private boolean dealed;
    @Column(name = "TRAN_TYPE")
    private int transType;
    @Column(name = "CREATE_TIME")
    private Date createTime;
    @Column(name = "UPDATE_TIME")
    private Date updateTime;
    @Column(name = "CREATE_BY")
    private int createdBy;
    @Column(name = "UPDATE_BY")
    private int updatedBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicketSerialNo() {
        return ticketSerialNo;
    }

    public void setTicketSerialNo(String ticketSerialNo) {
        this.ticketSerialNo = ticketSerialNo;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getTraceMsgId() {
        return traceMsgId;
    }

    public void setTraceMsgId(String traceMsgId) {
        this.traceMsgId = traceMsgId;
    }

    public boolean isDealed() {
        return dealed;
    }

    public void setDealed(boolean dealed) {
        this.dealed = dealed;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
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

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
