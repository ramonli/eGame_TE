package com.mpos.lottery.te.gameimpl.magic100.sale;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This entity will be used to trace the current and next sequence of lucky number.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "LK_OFFLINE_CANCELLATION")
public class OfflineCancellation implements Serializable {
    private static final long serialVersionUID = -5547782922325333017L;

    // there should be only one instance, and the ID will be fixed to 1.
    public static final String ID = "1";

    public static final int STATE_PROCESSING = 0; // processing

    public static final int STATE_DONE = 1; // done

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "GAME_ID")
    private String gameId;

    @Column(name = "START_NUMBER")
    private long startNumber;

    @Column(name = "END_NUMBER")
    private long endNumber;

    @Column(name = "CURRENT_NUMBER")
    private long currentNumber;

    @Column(name = "IS_HANDLED")
    private int isHandled;

    @Column(name = "TE_TRANSACTION_ID")
    private String teTransactionId;

    @Column(name = "CREATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime = new Date();

    @Column(name = "UPDATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime = createTime;

    @Column(name = "CREATE_BY")
    private String createBy;

    @Column(name = "UPDATE_BY")
    private String updateBy;

    public void setId(String id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(long startNumber) {
        this.startNumber = startNumber;
    }

    public long getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(long endNumber) {
        this.endNumber = endNumber;
    }

    public long getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(long currentNumber) {
        this.currentNumber = currentNumber;
    }

    public int getIsHandled() {
        return isHandled;
    }

    public void setIsHandled(int isHandled) {
        this.isHandled = isHandled;
    }

    public String getTeTransactionId() {
        return teTransactionId;
    }

    public void setTeTransactionId(String teTransactionId) {
        this.teTransactionId = teTransactionId;
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

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public static String getId() {
        return ID;
    }

}
