package com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill;

/**
 * Coobill client bean.
 * 
 * @author terry
 */
public class TopupMsisdnReq {

    private String agent;
    private String password;
    private String msisdn;
    private int amout;
    private String refTrx;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public int getAmout() {
        return amout;
    }

    public void setAmout(int amout) {
        this.amout = amout;
    }

    public String getRefTrx() {
        return refTrx;
    }

    public void setRefTrx(String refTrx) {
        this.refTrx = refTrx;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
