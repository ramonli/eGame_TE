package com.mpos.lottery.te.gameimpl.instantgame.domain.dto;

import com.mpos.lottery.te.trans.domain.Transaction;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.json.simple.JSONObject;

import java.io.Serializable;

public class ActiveCriteria implements Serializable {
    public static final String TOKEN_SEPERATOR = ",";

    public static final int TYPE_BYLASTTICKET = 1;
    public static final int TYPE_BYRANGETICKET = 2;
    public static final int TYPE_BYFIRSTTICKET = 3;
    public static final int TYPE_BYBATCHBOOK = 4;
    public static final int TYPE_BYBATCHRANGE = 5;
    public static final int TYPE_BYSINGLETICKET = 11;
    private static final long serialVersionUID = -8077056956217045872L;

    private String value;
    private int type;

    // added by Lee,2010-07-22
    private Transaction trans;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCriteria() {
        // if (this.type == TYPE_BYLASTTICKET){
        // return "," + this.getValue();
        // }
        // else if (this.type == TYPE_BYRANGETICKET){
        // return this.getValue();
        // }
        // else if (this.type == TYPE_BYFIRSTTICKET){
        // return this.getValue() + ",";
        // }
        // else if (this.type == TYPE_BYBATCHBOOK){
        // return this.getValue();
        // }
        // else {
        return this.getValue();
        // }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String toJSONString(JSONObject activeResult) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("activeType", this.getType());
        jsonObject.put("activeResult", activeResult);
        return jsonObject.toJSONString();
    }

    public Transaction getTrans() {
        return trans;
    }

    public void setTrans(Transaction trans) {
        this.trans = trans;
    }
}
