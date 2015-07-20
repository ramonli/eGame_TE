package com.mpos.lottery.te.valueaddservice.vat.web;

import java.io.Serializable;

public class VatReprintTicketReqDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String serialNo;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

}
