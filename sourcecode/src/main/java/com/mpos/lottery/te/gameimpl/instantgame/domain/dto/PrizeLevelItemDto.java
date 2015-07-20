package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Id;

public class PrizeLevelItemDto {
    @Id
    @Column(name = "ID")
    private String id;
    // if prize type is OBJECT, this filed should be set.
    @Column(name = "BD_PRIZE_OBJECT_ID")
    private String objectId;
    @Column(name = "OBJECT_NAME")
    private String objectName;
    /**
     * Refer to {@code PrizeLevel#PRIZE_TYPE_OBJECT} and {@code PrizeLevel#PRIZE_TYPE_CASH}.
     */
    @Column(name = "ITEM_TYPE")
    private int prizeType;
    @Column(name = "PRIZE_AMOUNT")
    private BigDecimal prizeAmount = new BigDecimal("0");
    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount = new BigDecimal("0");
    @Column(name = "ACTUAL_PAYOUT")
    private BigDecimal actualAmount = new BigDecimal("0");
    // How many objects should be returned? if cash, this value should be 1.
    @Column(name = "PRIZE_LEVEL_NUM")
    private Integer numberOfObject = 1;

    public PrizeLevelItemDto() {
    }

    public PrizeLevelItemDto(com.mpos.lottery.te.gamespec.prize.PrizeLevelItem levelItem) {
        this.id = levelItem.getId();
        this.objectId = levelItem.getObjectId();
        this.objectName = levelItem.getObjectName();
        this.prizeType = levelItem.getPrizeType();
        this.prizeAmount = levelItem.getPrizeAmount();
        this.taxAmount = levelItem.getTaxAmount();
        this.actualAmount = levelItem.getActualAmount();
        this.numberOfObject = levelItem.getNumberOfObject();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrizeType() {
        return prizeType;
    }

    public void setPrizeType(int prizeType) {
        this.prizeType = prizeType;
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

    public Integer getNumberOfObject() {
        return numberOfObject;
    }

    public void setNumberOfObject(Integer numberOfObject) {
        if (numberOfObject == 0) {
            // default 1 prize item.
            numberOfObject = 1;
        }
        this.numberOfObject = numberOfObject;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

}
