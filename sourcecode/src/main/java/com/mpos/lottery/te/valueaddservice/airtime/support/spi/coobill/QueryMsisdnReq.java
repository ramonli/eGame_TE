package com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill;

/**
 * Coobill client bean.
 * 
 * @author terry
 */
public class QueryMsisdnReq {

    private String telNo;
    private String reqTransid;

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getReqTransid() {
        return reqTransid;
    }

    public void setReqTransid(String reqTransid) {
        this.reqTransid = reqTransid;
    }

}
