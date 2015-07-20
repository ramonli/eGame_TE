package com.mpos.lottery.te.merchant.domain;

import com.mpos.lottery.te.gamespec.game.Game;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "MERCHANT_GAME_PROPERTIES")
public class OperatorCommission implements Serializable {
    private static final long serialVersionUID = -5404167634460580014L;

    @Id
    @Column(name = "MRID")
    private String id;

    @Column(name = "OPERATOR_ID")
    private String operatorId;

    @Column(name = "MERCHANT_ID")
    private long merchantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GAME_ID", nullable = false)
    private Game game;
    //
    // @Column(name="COMMISSION_RATE")
    // private BigDecimal rate;
    //
    @Column(name = "COMMISSION_RATE")
    private BigDecimal saleRate;

    @Column(name = "COMMISSION_RATE_PAYOUT")
    private BigDecimal payoutRate;

    @Transient
    private String gameId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public BigDecimal getSaleRate() {
        return saleRate;
    }

    public void setSaleRate(BigDecimal saleRate) {
        this.saleRate = saleRate;
    }

    public BigDecimal getPayoutRate() {
        return payoutRate;
    }

    public void setPayoutRate(BigDecimal payoutRate) {
        this.payoutRate = payoutRate;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    // public BigDecimal getRate() {
    // return rate;
    // }
    //
    // public void setRate(BigDecimal rate) {
    // this.rate = rate;
    // }

}
