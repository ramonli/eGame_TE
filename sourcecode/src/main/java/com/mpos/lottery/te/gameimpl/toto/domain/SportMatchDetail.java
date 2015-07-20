package com.mpos.lottery.te.gameimpl.toto.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * SPORT_MATCH_DETAIL table's po
 * 
 * @author lee
 * @version [Version NO, Apr 30, 2010]
 * @since [product/Modul version]
 */
@Entity(name = "SPORT_MATCH_DETAIL")
public class SportMatchDetail implements Serializable {
    /**
     * Annotation contents
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "MATCH_DETAIL_ID")
    private String matchDetailId;

    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;

    @Column(name = "MATCH_TYPE_ID")
    private String matchTypeId;

    @Column(name = "HOME_ID")
    private String homeId;

    @Column(name = "AWAY_ID")
    private String awayId;

    @Column(name = "BET_OPTION_ID")
    private String betoptionId;

    /**
     * The type definition of SPORT_BET_OPTION:
     * <ol>
     * <li>0 - 3 option[0/1/2]</li>
     * <li>1 - 3 option handicap[0/1/2]</li>
     * <li>2 - 2 option[0/1]</li>
     * <li>3 - 2 option handicap[0/1]</li>
     * <li>4 - Any value, eg:1|2,0,1,1|0</li>
     * </ol>
     */
    @Column(name = "BET_TYPE_ID")
    private String betTypeId;

    @Column(name = "BET_OPTION_VALUE")
    private BigDecimal betOptionValue;

    @Column(name = "GAME_DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date gameDateTime;

    @Column(name = "COUNT_IN_POOL")
    private BigDecimal countInPool;

    @Column(name = "RESULT")
    private String result;

    @Column(name = "WIN_OPTION")
    private BigDecimal winOption;

    @Column(name = "QUARTER_TYPE")
    private int quarterType;

    @Column(name = "QUARTER_VALUE")
    private int quarterValue;

    @Column(name = "MATCH_SEQ")
    private int matchSeq;

    public String getMatchDetailId() {
        return matchDetailId;
    }

    public void setMatchDetailId(String matchDetailId) {
        this.matchDetailId = matchDetailId;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public String getMatchTypeId() {
        return matchTypeId;
    }

    public void setMatchTypeId(String matchTypeId) {
        this.matchTypeId = matchTypeId;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public String getAwayId() {
        return awayId;
    }

    public void setAwayId(String awayId) {
        this.awayId = awayId;
    }

    public String getBetoptionId() {
        return betoptionId;
    }

    public void setBetoptionId(String betoptionId) {
        this.betoptionId = betoptionId;
    }

    public String getBetTypeId() {
        return betTypeId;
    }

    public void setBetTypeId(String betTypeId) {
        this.betTypeId = betTypeId;
    }

    public BigDecimal getBetOptionValue() {
        return betOptionValue;
    }

    public void setBetOptionValue(BigDecimal betOptionValue) {
        this.betOptionValue = betOptionValue;
    }

    public Date getGameDateTime() {
        return gameDateTime;
    }

    public void setGameDateTime(Date gameDateTime) {
        this.gameDateTime = gameDateTime;
    }

    public BigDecimal getCountInPool() {
        return countInPool;
    }

    public void setCountInPool(BigDecimal countInPool) {
        this.countInPool = countInPool;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public BigDecimal getWinOption() {
        return winOption;
    }

    public void setWinOption(BigDecimal winOption) {
        this.winOption = winOption;
    }

    public int getQuarterType() {
        return quarterType;
    }

    public void setQuarterType(int quarterType) {
        this.quarterType = quarterType;
    }

    public int getQuarterValue() {
        return quarterValue;
    }

    public void setQuarterValue(int quarterValue) {
        this.quarterValue = quarterValue;
    }

    public int getMatchSeq() {
        return matchSeq;
    }

    public void setMatchSeq(int matchSeq) {
        this.matchSeq = matchSeq;
    }
}
