package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.common.dao.BaseEntity;
import com.mpos.lottery.te.gamespec.game.Game;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Define the relationship between VAT and GAME. Administrator has to allocate game to a VAT first before merchant can
 * operator VAT.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "VAT_GAME")
public class Vat2Game extends BaseEntity {
    private static final long serialVersionUID = 2901582470637287421L;
    public static int STATUS_INVALID = 0;
    public static int STATUS_VALID = 1;

    @Column(name = "VAT_ID")
    private String vatId;

    @Column(name = "GAME_ID")
    private String gameId;

    // refer to DeviceBizType.BIZ_XXX
    @Column(name = "VAT_MERCHANT_TYPE_ID")
    private String businessType;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "VAT_RATE")
    private BigDecimal rate = new BigDecimal("0");

    @Column(name = "MINIMUM_AMOUNT")
    private BigDecimal minThresholdAmount = new BigDecimal("0");

    @Transient
    private Game game;

    public String getVatId() {
        return vatId;
    }

    public void setVatId(String vatId) {
        this.vatId = vatId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getMinThresholdAmount() {
        return minThresholdAmount;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setMinThresholdAmount(BigDecimal minThresholdAmount) {
        this.minThresholdAmount = minThresholdAmount;
    }

}
