package com.mpos.lottery.te.merchant.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "OPERATOR_MERCHANT")
public class OperatorMerchant {
    public static final int STATE_INVALID = 0;
    public static final int STATE_VALID = 1;
    @Id
    @Column(name = "OMID")
    private String id;
    @Column(name = "OPERATOR_ID")
    private String operatorID;
    @Column(name = "MERCHANT_ID")
    private long merchantID;
    @Column(name = "INCENTIVE_BETS_COUNTER")
    private int incentiveCount;
    // refer to STATE_XXX
    @Column(name = "STATUS")
    private int state;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    public long getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(long merchantID) {
        this.merchantID = merchantID;
    }

    public int getIncentiveCount() {
        return incentiveCount;
    }

    public void setIncentiveCount(int incentiveCount) {
        this.incentiveCount = incentiveCount;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

}
