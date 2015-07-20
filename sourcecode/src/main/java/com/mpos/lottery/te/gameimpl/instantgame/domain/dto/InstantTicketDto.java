package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import java.io.Serializable;

public class InstantTicketDto implements Serializable {
    private static final long serialVersionUID = -6093218680795644912L;
    private String bookNumber;
    private String serialNo;
    private int errorCode;
    private String errorMsg;

    public String getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(String bookNumber) {
        this.bookNumber = bookNumber;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
