package com.mpos.lottery.te.gamespec.sale;

import com.mpos.lottery.te.common.dao.SettlementEntity;
import com.mpos.lottery.te.config.exception.SystemException;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "OFFLINE_TICKETS")
public class OfflineTicketLog extends SettlementEntity {
    private static final long serialVersionUID = -564991314218620958L;
    public static final int STATUS_NO_COUNT = 0;
    public static final int STATUS_COUNTED = 1;

    @Column(name = "TRANSACTION_ID")
    private String transactionId;
    @Column(name = "TICKET_SERIAL")
    private String serialNo;
    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;
    @Column(name = "GAME_ID")
    private String gameId;
    @Column(name = "GAME_TYPE")
    private int gameType;
    @Column(name = "COUNTED_GAME_INSTANCE_ID")
    private String uploadedGameInstanceId;
    @Column(name = "RESPONSE_CODE")
    private int statusCode = SystemException.CODE_OK;
    @Column(name = "STATUS")
    private int status = STATUS_NO_COUNT;
    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount = new BigDecimal("0");
    /* Refer to TICKET_TYPE_XXX */
    @Column(name = "TICKET_TYPE")
    private int ticketType = BaseTicket.TICKET_TYPE_NORMAL;
    /* Refer to TICKET_FROM_XXX */
    @Column(name = "TICKET_FROM")
    private int ticketFrom;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public String getUploadedGameInstanceId() {
        return uploadedGameInstanceId;
    }

    public void setUploadedGameInstanceId(String uploadedGameInstanceId) {
        this.uploadedGameInstanceId = uploadedGameInstanceId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTicketType() {
        return ticketType;
    }

    public void setTicketType(int ticketType) {
        this.ticketType = ticketType;
    }

    public int getTicketFrom() {
        return ticketFrom;
    }

    public void setTicketFrom(int ticketFrom) {
        this.ticketFrom = ticketFrom;
    }

}
