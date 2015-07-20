package com.mpos.lottery.te.valueaddservice.voucher;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "VAS_VOUCHER_PARAMETERS")
public class VoucherStatistics {

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "DENOMINATION")
    private BigDecimal faceAmount;
    @Column(name = "REMAINING_VOUCHER_NUMBER")
    private int remainCount;
    @Column(name = "GAME_ID")
    private String gameId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getFaceAmount() {
        return faceAmount;
    }

    public void setFaceAmount(BigDecimal faceAmount) {
        this.faceAmount = faceAmount;
    }

    public int getRemainCount() {
        return remainCount;
    }

    public void setRemainCount(int remainCount) {
        this.remainCount = remainCount;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

}
