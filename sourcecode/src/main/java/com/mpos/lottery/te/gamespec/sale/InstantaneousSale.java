package com.mpos.lottery.te.gamespec.sale;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "INSTANTANEOUS_SALES")
public class InstantaneousSale {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;

    @Column(name = "TICKET_TOTAL")
    private int saleCount;

    @Column(name = "TUNOVER_TOTAL")
    private BigDecimal turnover = new BigDecimal("0");

    @Column(name = "CANCEL_TICKET_COUNT")
    private int cancelTicketCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public int getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(int saleCount) {
        this.saleCount = saleCount;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int getCancelTicketCount() {
        return cancelTicketCount;
    }

    public void setCancelTicketCount(int cancelTicketCount) {
        this.cancelTicketCount = cancelTicketCount;
    }

}
