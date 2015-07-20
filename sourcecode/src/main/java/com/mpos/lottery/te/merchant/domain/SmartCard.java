package com.mpos.lottery.te.merchant.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "DW_CARD")
@Entity
public class SmartCard implements Serializable {
    private static final long serialVersionUID = -7879865683982536640L;
    public static final int STATUS_UNFORMATTED = 0;
    public static final int STATUS_FORMATTED = 1;
    public static final int STATUS_ACTIVED = 2;
    public static final int STATUS_IN_BLACKLIST = 3;

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "MERCHANT_ID")
    private long merchantId;
    @Column(name = "SN")
    private String serialNo;
    @Column(name = "PIN")
    private String PIN;
    @Column(name = "STATUS")
    private int status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String pIN) {
        PIN = pIN;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
