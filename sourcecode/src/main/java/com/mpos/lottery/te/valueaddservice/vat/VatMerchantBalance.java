package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.common.dao.BaseEntity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VAT_MERCHANT_BALANCE")
public class VatMerchantBalance extends BaseEntity {
    private static final long serialVersionUID = -6079293539386864850L;
    private long merchantId;
    @Column(name = "MERCHANT_SALE_BALANCE")
    private BigDecimal saleBalance = new BigDecimal("0");
    @Column(name = "MERCHANT_PAYOUT_BALANCE")
    private BigDecimal payoutBalance = new BigDecimal("0");

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
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
