package com.mpos.lottery.te.gameimpl.instantgame.domain;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "instant_ticket_virn")
public class InstantVIRNPrize implements java.io.Serializable {
    private static final long serialVersionUID = -8757371574481428087L;
    public static final String PRIZE_TYPE_CASH = "CC";
    public static final String PRIZE_TYPE_FREEIG = "FI";
    public static final String PRIZE_TYPE_FREELOTTO = "FO";
    public static final int PRIZE_TYPE_FI = 101;
    public static final int PRIZE_TYPE_FO = 102;

    @Id
    @Column(name = "ID")
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IG_GAME_INSTANCE_ID", nullable = false)
    private InstantGameDraw gameDraw;

    @Column(name = "VIRN")
    private String virn;

    @Column(name = "PRIZE_VALUE")
    private BigDecimal prizeAmount; // before tax

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount;

    @Column(name = "ACTUAL_PAYOUT")
    private BigDecimal actualPayout;

    @Column(name = "IS_VALIDATED")
    private boolean isValidated;

    @Column(name = "PRIZE_TYPE")
    private String prizeType;

    public int getIntPirzeType() {
        if (PRIZE_TYPE_CASH.equalsIgnoreCase(this.getPrizeType())) {
            return PrizeLevelDto.PRIZE_TYPE_CASH;
        } else if (PRIZE_TYPE_FREEIG.equalsIgnoreCase(this.getPrizeType())) {
            return PRIZE_TYPE_FI;
        } else if (PRIZE_TYPE_FREELOTTO.equalsIgnoreCase(this.getPrizeType())) {
            return PRIZE_TYPE_FO;
        } else {
            throw new SystemException("Unsupported VIRN prize type:" + this.getPrizeType());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InstantGameDraw getGameDraw() {
        return gameDraw;
    }

    public void setGameDraw(InstantGameDraw gameDraw) {
        this.gameDraw = gameDraw;
    }

    public String getVirn() {
        return virn;
    }

    public void setVirn(String virn) {
        this.virn = virn;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public void setPrizeAmount(BigDecimal prizeAmount) {
        this.prizeAmount = prizeAmount;
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void setValidated(boolean isValidated) {
        this.isValidated = isValidated;
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

    public String getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(String prizeType) {
        this.prizeType = prizeType;
    }

}
