package com.mpos.lottery.te.trans.domain;

import com.mpos.lottery.te.common.dao.BaseEntity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Once operator perform a settlement of a day, no any transactions allowed that day.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "CARD_PAYOUT_HISTORY_ITEM")
public class SettlementLogItem extends BaseEntity {
    private static final long serialVersionUID = 7975485332409262867L;

    @ManyToOne
    @JoinColumn(name = "CARD_PAYOUT_HISTORY_ID", nullable = false)
    private SettlementLog settlementLog;
    @Column(name = "OPERATOR_ID")
    private String operatorId;
    // check which day's transaction
    @Column(name = "PAYOUT_TIME")
    private Date checkDay;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public SettlementLog getSettlementLog() {
        return settlementLog;
    }

    public void setSettlementLog(SettlementLog settlementLog) {
        this.settlementLog = settlementLog;
    }

    public Date getCheckDay() {
        return checkDay;
    }

    public void setCheckDay(Date checkDay) {
        this.checkDay = checkDay;
    }

}
