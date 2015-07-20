package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.gameimpl.lotto.prize.domain.LuckyWinningItem;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "IG_PAYOUT_DETAIL_TEMP")
public class IGPayoutDetailTemp implements java.io.Serializable {
    private static final long serialVersionUID = -3096168573021845559L;
    public static final int TOPUP_MODE_DEFAULT = 0;
    public static final int TOPUP_MODE_ELECTRONIC = 1;
    public static final int TOPUP_MODE_PREPAID_CARD = 2;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "PAYOUT_ID")
    private String payoutId;

    /**
     * prize amount, before tax...
     * <p>
     * prizeAmount=PrizeLevel.prizeAmount * numberofPrize * numberOfLevel
     */
    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal prizeAmount;

    /**
     * actual amount, after tax...
     * <p>
     * actualAmount=PrizeLevel.actualPayout * numberOfPrize * numberOfLevel
     */
    @Column(name = "CASH_AMOUNT")
    private BigDecimal actualAmount;

    @Column(name = "TOPUP_AMOUNT")
    private BigDecimal topupAmount;

    @Column(name = "TOPUP_MODE")
    private int topupMode;

    @Column(name = "PAYOUT_TYPE")
    private int payoutType;

    @Column(name = "BG_LUCKY_PRIZE_OBJECT_ID")
    private String objectId;

    @Column(name = "BG_LUCKY_PRIZE_OBJECT_NAME")
    private String objectName;
    @Column(name = "OBJECT_NUM")
    private int numberOfObject;
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

    // refer to ObjectPrizeLevelDefItem.type_XXX
    @Column(name = "OBJECT_TYPE")
    private int objectType;

    @Column(name = "IG_BATCH_NUMBER")
    private long iGBatchNumber;

    @Column(name = "OPERATOR_ID")
    private String operatorId;
    // the payout/validation transaction
    @Transient
    private String transactionId;
    // Which game does this payout apply to? Currently this fileds will be only
    // used for generating settlement object.
    @Transient
    private String gameId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayoutId() {
        return payoutId;
    }

    public void setPayoutId(String payoutId) {
        this.payoutId = payoutId;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(BigDecimal totalAmount) {
        this.prizeAmount = totalAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public BigDecimal getTopupAmount() {
        return topupAmount;
    }

    public void setTopupAmount(BigDecimal topupAmount) {
        this.topupAmount = topupAmount;
    }

    public int getTopupMode() {
        return topupMode;
    }

    public void setTopupMode(int topupMode) {
        this.topupMode = topupMode;
    }

    /**
     * Refer to {@link LuckyWinningItem#PRIZE_TYPE_CASH}.
     */
    public int getPayoutType() {
        return payoutType;
    }

    /**
     * Refer to {@link LuckyWinningItem#PRIZE_TYPE_CASH}.
     */
    public void setPayoutType(int payoutType) {
        this.payoutType = payoutType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
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

    public int getNumberOfObject() {
        return numberOfObject;
    }

    public void setNumberOfObject(int numberOfObject) {
        this.numberOfObject = numberOfObject;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }

    public long getiGBatchNumber() {
        return iGBatchNumber;
    }

    public void setiGBatchNumber(long iGBatchNumber) {
        this.iGBatchNumber = iGBatchNumber;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }
}
