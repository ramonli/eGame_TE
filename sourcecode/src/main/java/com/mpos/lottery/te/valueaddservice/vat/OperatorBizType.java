package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.common.dao.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Define the business type(B2B or B2C) of a device.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "VAT_OPERATOR_MERCHANT_TYPE")
public class OperatorBizType extends BaseEntity {
    private static final long serialVersionUID = -8934403347196560719L;
    public static final String BIZ_B2B = "1";
    public static final String BIZ_B2C = "2";
    public static final int STATUS_INVALID = 0;
    public static final int STATUS_VALID = 1;

    @Column(name = "OPERATOR_ID")
    private String operatorId;
    /**
     * Refer to BIZ_XXX.
     */
    @Column(name = "VAT_MERCHANT_TYPE_ID")
    private String businessType;
    /**
     * Refer to STATUS_XXX.
     */
    @Column(name = "STATUS")
    private int status;

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBizTypeString() {
        return (BIZ_B2B.equalsIgnoreCase(this.getBusinessType()) ? "B2B" : "B2C");
    }
}
