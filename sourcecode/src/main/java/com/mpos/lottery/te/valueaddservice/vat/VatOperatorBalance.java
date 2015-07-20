package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.common.dao.BaseEntity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VAT_OPERATOR_BALANCE")
public class VatOperatorBalance extends BaseEntity {
    private static final long serialVersionUID = 5854679854521886296L;
    @Column(name = "OPERATOR_ID")
    private String operatorId;
    @Column(name = "OPERATOR_SALE_BALANCE")
    private BigDecimal saleBalance = new BigDecimal("0");
    @Column(name = "OPERATOR_PAYOUT_BALANCE")
    private BigDecimal payoutBalance = new BigDecimal("0");

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public BigDecimal getSaleBalance() {
        return saleBalance;
    }

    public void setSaleBalance(BigDecimal saleBalance) {
        this.saleBalance = saleBalance;
    }

    public BigDecimal getPayoutBalance() {
        return payoutBalance;
    }

    public void setPayoutBalance(BigDecimal payoutBalance) {
        this.payoutBalance = payoutBalance;
    }

}
