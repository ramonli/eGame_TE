package com.mpos.lottery.te.gamespec.game;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@SuppressWarnings("serial")
@MappedSuperclass
public class BaseOperationParameter implements java.io.Serializable {
    public static final int PAYOUTMODE_REFUND = 0;
    public static final int PAYOUTMODE_PRINTNEWTICKET = 1;

    @Id
    @Column(name = "ID")
    // // create seqence TE_SEQ start with 1 increment by 1;
    // @SequenceGenerator(name="TE_SEQ", sequenceName="TE_SEQ")
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="TE_SEQ")
    private String id;

    @Column(name = "BASE_AMOUNT")
    private BigDecimal baseAmount = new BigDecimal("0");

    @Column(name = "MIN_MULTI_DRAW")
    private Integer minAllowedMultiDraw;

    @Column(name = "MAX_MULTI_DRAW")
    private Integer maxAllowedMultiDraw;

    @Column(name = "PAYOUT_MODEL")
    private Integer payoutMode = PAYOUTMODE_PRINTNEWTICKET;

    @Column(name = "ALLOW_CANCELLATION")
    private boolean allowManualCancellation;

    @Column(name = "BANKER")
    private boolean isBankerBetOptionSupported;

    @Column(name = "MULTIPLE")
    private boolean isMultipleBetOptionSupported;

    @Column(name = "ALLOW_SKIP_DRAW")
    private boolean allowSaleOnNewGameInstance;

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public Integer getMinAllowedMultiDraw() {
        return minAllowedMultiDraw;
    }

    public void setMinAllowedMultiDraw(Integer minAllowedMultiDraw) {
        this.minAllowedMultiDraw = minAllowedMultiDraw;
    }

    public Integer getMaxAllowedMultiDraw() {
        return maxAllowedMultiDraw;
    }

    public void setMaxAllowedMultiDraw(Integer maxAllowedMultiDraw) {
        this.maxAllowedMultiDraw = maxAllowedMultiDraw;
    }

    public Integer getPayoutMode() {
        return payoutMode;
    }

    public void setPayoutMode(Integer payoutMode) {
        this.payoutMode = payoutMode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAllowManualCancellation() {
        return allowManualCancellation;
    }

    public void setAllowManualCancellation(boolean allowManualCancellation) {
        this.allowManualCancellation = allowManualCancellation;
    }

    public boolean isBankerBetOptionSupported() {
        return isBankerBetOptionSupported;
    }

    public void setBankerBetOptionSupported(boolean isBankerBetOptionSupported) {
        this.isBankerBetOptionSupported = isBankerBetOptionSupported;
    }

    public boolean isMultipleBetOptionSupported() {
        return isMultipleBetOptionSupported;
    }

    public void setMultipleBetOptionSupported(boolean isMultipleBetOptionSupported) {
        this.isMultipleBetOptionSupported = isMultipleBetOptionSupported;
    }

    public boolean isAllowSaleOnNewGameInstance() {
        return allowSaleOnNewGameInstance;
    }

    public void setAllowSaleOnNewGameInstance(boolean allowSaleOnNewGameInstance) {
        this.allowSaleOnNewGameInstance = allowSaleOnNewGameInstance;
    }

}
