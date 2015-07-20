package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.common.dao.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "VAT_CUSTOMER")
public class VatCompany extends BaseEntity {
    private static final long serialVersionUID = 1367660927788768235L;
    @Column(name = "MERCHANT_ID")
    private long merchantId;

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

}
