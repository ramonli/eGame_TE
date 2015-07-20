package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.common.dao.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Define the relationship between VAT and merchant. Administrator has to allocate VAT to a merchant first before s/he
 * can operator VAT.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "VAT_MERCHANT")
public class Vat2Merchant extends BaseEntity {
    private static final long serialVersionUID = -7127055492604438235L;
    public static int STATUS_INVALID = 0;
    public static int STATUS_VALID = 1;

    @Column(name = "VAT_ID")
    private String vatId;

    @Column(name = "MERCHANT_ID")
    private long merchantId;

    @Column(name = "STATUS")
    private int status;

    public String getVatId() {
        return vatId;
    }

    public void setVatId(String vatId) {
        this.vatId = vatId;
    }

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
