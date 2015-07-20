package com.mpos.lottery.te.thirdpartyservice;

public enum HttpHeader {
    TRANS_TYPE("TRANS_TYPE");

    private String header;

    private HttpHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return this.header;
    }
}
