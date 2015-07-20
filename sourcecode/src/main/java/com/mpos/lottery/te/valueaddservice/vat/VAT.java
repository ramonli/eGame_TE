package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.common.dao.BaseEntity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "VAT")
public class VAT extends BaseEntity {
    private static final long serialVersionUID = -3873357993635404848L;
    public static final int STATUS_VALID = 1;
    public static final int STATUS_INVALID = 0;
    public static final int ROUND_UP = 0;
    public static final int ROUND_DOWN = 1;

    @Column(name = "VAT_CODE")
    private String code;

    // refer to ROUND_XXX
    @Column(name = "ROUND_IS_UP_DOWN")
    private int roundUpDown;

    // refer to STATUS_XX
    @Column(name = "STATUS")
    private int status;

    /**
     * The total amount of VAT.
     */
    @Transient
    private BigDecimal totalAmount = new BigDecimal("0");

    /**
     * The tax reference No. of buyer. Only present when client is in B2B mode.
     */
    @Transient
    private String buyerTaxNo;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getRoundUpDown() {
        return roundUpDown;
    }

    public void setRoundUpDown(int roundUpDown) {
        this.roundUpDown = roundUpDown;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBuyerTaxNo() {
        return buyerTaxNo;
    }

    public void setBuyerTaxNo(String buyerTaxNo) {
        this.buyerTaxNo = buyerTaxNo;
    }

}
