package com.mpos.lottery.te.gameimpl.extraball.sale;

import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;

import java.math.BigDecimal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "EB_OPERATION_PARAMETERS")
@AttributeOverride(name = "baseAmount", column = @Column(name = "INTERVAL"))
public class ExtraBallOperationParameter extends BaseOperationParameter {
    private static final long serialVersionUID = -3227947608474639719L;
    @Column(name = "MIN_AMOUNT")
    private BigDecimal minAmount = new BigDecimal("0");

    @Column(name = "MAX_AMOUNT")
    private BigDecimal maxAmount = new BigDecimal("0");

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

}
