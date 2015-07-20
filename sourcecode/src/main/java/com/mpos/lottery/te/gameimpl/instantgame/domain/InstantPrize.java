package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity(name = "INSTANT_TICKET_PRIZE")
public class InstantPrize {
    public static final int TYPE_CASH = 1; // cash
    public static final int TYPE_INVENTORY = 2; // Get prize from a dedicated
                                                // prize system.
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "PRIZE_LEVEL")
    private Integer prizeLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IG_GAME_INSTANCE_ID", nullable = false)
    private InstantGameDraw gameDraw;

    @Column(name = "PRIZE_AMOUNT")
    private BigDecimal prizeAmount = new BigDecimal("0"); // before tax

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount = new BigDecimal("0");

    @Column(name = "ACTUAL_PAYOUT")
    private BigDecimal actualPayout = new BigDecimal("0"); // after tax

    @Column(name = "PRIZE_DETAIL")
    private String detail;

    // refer to InstantVIRNPrize.prizeType
    @Column(name = "PRIZE_TYPE")
    private String prizeType;

    @Transient
    private InstantVIRNPrize virnPrize;

    /**
     * To be compitable with VIRN, and avoid changing the interface of ValidationStrategy, new field 'instantPrize' is
     * introduced. Actually InstantPrize has been deprecated, the underlying table 'instant_ticket_prize' has been
     * dropped too.
     */
    @Transient
    private PrizeLevelDto instantPayoutDto;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(Integer prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public InstantGameDraw getGameDraw() {
        return gameDraw;
    }

    public void setGameDraw(InstantGameDraw gameDraw) {
        this.gameDraw = gameDraw;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getActualPayout() {
        return actualPayout;
    }

    public void setActualPayout(BigDecimal actualPayout) {
        this.actualPayout = actualPayout;
    }

    public InstantVIRNPrize getVirnPrize() {
        return virnPrize;
    }

    public void setVirnPrize(InstantVIRNPrize virnPrize) {
        this.virnPrize = virnPrize;
    }

    public String getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(String prizeType) {
        this.prizeType = prizeType;
    }

    public PrizeLevelDto getInstantPayoutDto() {
        return instantPayoutDto;
    }

    public void setInstantPayoutDto(PrizeLevelDto instantPrize) {
        this.instantPayoutDto = instantPrize;
    }

}
