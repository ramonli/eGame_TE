package com.mpos.lottery.te.gameimpl.toto.domain;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "TOTO_GAME_INSTANCE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "GAME_INSTANCE_ID")),
        @AttributeOverride(name = "drawDate", column = @Column(name = "GAME_ANNOUNCE")) })
public class ToToGameInstance extends BaseGameInstance {
    private static final long serialVersionUID = 9042476652264630192L;

    @Column(name = "NUM_MATCH")
    private BigDecimal matchNum;

    @Column(name = "BASE_AMOUNT")
    private BigDecimal baseAmount;

    @Column(name = "OMR_GAME_SET")
    private String omrGameSet;

    @Column(name = "PRIZE_LOGIC_ID")
    private String prizeLogicId;

    @Transient
    private String gameTypeId;

    public String getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(String gameTypeId) {
        this.gameTypeId = gameTypeId;
    }

    public BigDecimal getMatchNum() {
        return matchNum;
    }

    public void setMatchNum(BigDecimal matchNum) {
        this.matchNum = matchNum;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public String getOmrGameSet() {
        return omrGameSet;
    }

    public void setOmrGameSet(String omrGameSet) {
        this.omrGameSet = omrGameSet;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

}
