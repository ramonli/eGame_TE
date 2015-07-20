package com.mpos.lottery.te.trans.domain;

import com.mpos.lottery.te.common.dao.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Once operator perform a settlement, no any transactions allowed that day.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "CARD_PAYOUT_HISTORY")
public class SettlementLog extends BaseEntity {
    private static final long serialVersionUID = 543433517607839204L;
    public static final int STATE_VALID = 1;

    @Column(name = "OPERATOR_ID")
    private String operatorId;
    @Column(name = "STATUS")
    private int status;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
