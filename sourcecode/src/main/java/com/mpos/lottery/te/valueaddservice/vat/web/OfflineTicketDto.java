package com.mpos.lottery.te.valueaddservice.vat.web;

public class OfflineTicketDto {
    private String rawSerialNo; // retrieve from client request

    private String validationCode;

    private String barcode;

    public String getRawSerialNo() {
        return rawSerialNo;
    }

    public void setRawSerialNo(String rawSerialNo) {
        this.rawSerialNo = rawSerialNo;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

}
