package com.mpos.lottery.te.gamespec.prize.web;

import java.io.Serializable;
import java.math.BigDecimal;

public class PrizeLevelObjectItemDto implements Serializable {
    private static final long serialVersionUID = -6120188246919963970L;
    private String objectId;
    private String objectName;
    private BigDecimal price = new BigDecimal("0");
    private BigDecimal taxAmount = new BigDecimal("0");
    private int numberOfObject;

    public PrizeLevelObjectItemDto() {
    }

    public PrizeLevelObjectItemDto(String objectId, String objectName, BigDecimal price, BigDecimal taxAmount,
            int numberOfObject) {
        super();
        this.objectId = objectId;
        this.objectName = objectName;
        this.price = price;
        this.taxAmount = taxAmount;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public int getNumberOfObject() {
        return numberOfObject;
    }

    public void setNumberOfObject(int numberOfObject) {
        this.numberOfObject = numberOfObject;
    }

}
