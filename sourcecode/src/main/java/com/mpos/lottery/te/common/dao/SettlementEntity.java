package com.mpos.lottery.te.common.dao;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/*
 * All jpa entity should extends this class.
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class SettlementEntity extends BaseEntity {

    @Column(name = "OPERATOR_ID")
    private String operatorId;

    @Column(name = "DEV_ID")
    private long devId;

    @Column(name = "MERCHANT_ID")
    private long merchantId;

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

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    // public String getBatchNo() {
    // return batchNo;
    // }
    //
    // public void setBatchNo(String batchNo) {
    // this.batchNo = batchNo;
    // }

}
