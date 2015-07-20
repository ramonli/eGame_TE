package com.mpos.lottery.te.thirdpartyservice.playeraccount;

/**
 * The representation of player.
 * 
 * @author Ramon
 * 
 */
public class User {
    private String id;
    private String mobile;
    private String PIN;
    private String creditCardSN;

    public User() {
    }

    public User(String id, String mobile, String creditCardSN) {
        super();
        this.id = id;
        this.mobile = mobile;
        this.creditCardSN = creditCardSN;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String pIN) {
        PIN = pIN;
    }

    public String getCreditCardSN() {
        return creditCardSN;
    }

    public void setCreditCardSN(String creditCardSN) {
        this.creditCardSN = creditCardSN;
    }

}
