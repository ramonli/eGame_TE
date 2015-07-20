package com.mpos.lottery.te.merchant.domain;

import com.mpos.lottery.te.gamespec.game.Game;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "GAME_MERCHANT")
public class MerchantCommission {
    public static final int COMMTYPE_PAYOUT = 1;
    public static final int COMMTYPE_SALE = 2;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "MERCHANT_ID")
    private long merchantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GAME_ID", nullable = false)
    private Game game;

    @Column(name = "COMMISSION_RATE_SALES")
    private BigDecimal saleCommissionRate;

    @Column(name = "COMMISSION_RATE_PAYOUT")
    private BigDecimal payoutCommissionRate;

    @Column(name = "ALLOWED_PAYOUT")
    private boolean allowPayout;

    @Column(name = "ALLOWED_SELLING")
    private boolean allowSale;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public BigDecimal getSaleCommissionRate() {
        return saleCommissionRate;
    }

    public void setSaleCommissionRate(BigDecimal saleCommissionRate) {
        this.saleCommissionRate = saleCommissionRate;
    }

    public BigDecimal getPayoutCommissionRate() {
        return payoutCommissionRate;
    }

    public void setPayoutCommissionRate(BigDecimal payoutCommissionRate) {
        this.payoutCommissionRate = payoutCommissionRate;
    }

    public boolean isAllowPayout() {
        return allowPayout;
    }

    public void setAllowPayout(boolean allowPayout) {
        this.allowPayout = allowPayout;
    }

    public boolean isAllowSale() {
        return allowSale;
    }

    public void setAllowSale(boolean allowSale) {
        this.allowSale = allowSale;
    }

}
