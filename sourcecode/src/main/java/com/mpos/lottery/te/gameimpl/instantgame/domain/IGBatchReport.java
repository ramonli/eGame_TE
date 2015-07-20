package com.mpos.lottery.te.gameimpl.instantgame.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "IG_BATCH_REPORT")
public class IGBatchReport implements java.io.Serializable {
    private static final long serialVersionUID = -3096168573021845559L;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "OPERATOR_ID")
    private String operatorId;

    @Column(name = "BATCH_ID")
    private long batchId;
    @Column(name = " FAILED_TICKETS_COUNT")
    private long failedTicketsCount;
    @Column(name = "SUCCEDED_TICKETS_COUNT")
    private long succeededTicketsCount;
    @Column(name = " ACTUAL_AMOUNT")
    private BigDecimal actualAmount;
    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount;

    @Column(name = "CREATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime = new Date();

    @Column(name = "CREATE_BY")
    private String createBy;

    @Column(name = "UPDATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "UPDATE_BY")
    private String updateBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public long getBatchId() {
        return batchId;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }

    public long getFailedTicketsCount() {
        return failedTicketsCount;
    }

    public void setFailedTicketsCount(long failedTicketsCount) {
        this.failedTicketsCount = failedTicketsCount;
    }

    public long getSucceededTicketsCount() {
        return succeededTicketsCount;
    }

    public void setSucceededTicketsCount(long succeededTicketsCount) {
        this.succeededTicketsCount = succeededTicketsCount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

}
