package com.mpos.lottery.te.thirdpartyservice.playeraccount.web;

import com.mpos.lottery.te.common.util.SimpleToolkit;

import java.util.Date;

public class PlayerAccountHttpHeader {
    public static final String SGPE = "1";
    public static final String TE = "2";
    public static final String WGPE = "3";
    public static final String MLOTTERY = "4";

    // http header definitions
    public static final String HEADER_SYSTEM_ID = "SYSTEM_ID";
    public static final String HEADER_TRANS_TYPE = "TRANS_TYPE";
    public static final String HEADER_REQ_MSGID = "REQ_MSG_ID";
    public static final String HEADER_PROTOCOL_VERSION = "PROTOCAL_VERSION";
    public static final String HEADER_USER_ID = "USER_ID";
    public static final String HEADER_TIME_STAMP = "TIMESTAMP";
    public static final String HEADER_RESPONSE_CODE = "RESPONSE_CODE";
    public static final String HEADER_RESPONSE_DESC = "RESPONSE_DESC";

    private String systemId = TE;
    private int transType;
    // size: 22
    private String requestMsgId;
    private String protocolVersion = "1.0";
    private String userId;
    private int responseCode;
    private String responseDesc = "";
    private String timestamp;

    public PlayerAccountHttpHeader() {
    }

    public PlayerAccountHttpHeader(int transType, String requestMsgId, String userId, Date date) {
        this(TE, transType, requestMsgId, "1.0", userId, date);
    }

    public PlayerAccountHttpHeader(String systemId, int transType, String requestMsgId, String protocolVersion,
            String userId, Date date) {
        super();
        this.systemId = systemId;
        this.transType = transType;
        this.requestMsgId = requestMsgId;
        this.protocolVersion = protocolVersion;
        this.userId = userId;
        this.timestamp = SimpleToolkit.formatDate(date);
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public int getTransType() {
        return transType;
    }

    public void setTransType(int transType) {
        this.transType = transType;
    }

    public String getRequestMsgId() {
        return requestMsgId;
    }

    public void setRequestMsgId(String requestMsgId) {
        this.requestMsgId = requestMsgId;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getResponseDesc() {
        return responseDesc;
    }

    public void setResponseDesc(String responseDesc) {
        this.responseDesc = responseDesc;
    }

    @Override
    public String toString() {
        return "PlayerAccountHttpHeader [systemId=" + systemId + ", transType=" + transType + ", requestMsgId="
                + requestMsgId + ", protocolVersion=" + protocolVersion + ", userId=" + userId + ", responseCode="
                + responseCode + ", timestamp=" + timestamp + "]";
    }

}
