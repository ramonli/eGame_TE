package com.mpos.lottery.te.valueaddservice.voucher;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "VAS_VOUCHER_OPERATOR_PARA")
public class VoucherOperationParameter {

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "VOUCHER_TYPE")
    private int voucherProviderType;
    @Column(name = "EXPIRED_DAY")
    private int bufferExpireDay;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVoucherProviderType() {
        return voucherProviderType;
    }

    public void setVoucherProviderType(int voucherProviderType) {
        this.voucherProviderType = voucherProviderType;
    }

    public int getBufferExpireDay() {
        return bufferExpireDay;
    }

    public void setBufferExpireDay(int bufferExpireDay) {
        this.bufferExpireDay = bufferExpireDay;
    }

}
