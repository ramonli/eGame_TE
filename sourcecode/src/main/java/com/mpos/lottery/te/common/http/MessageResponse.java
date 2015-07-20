package com.mpos.lottery.te.common.http;

import com.google.protobuf.Message;

import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerAccountHttpHeader;

import org.apache.http.HttpResponse;

public class MessageResponse {
    private HttpResponse httpResponse;
    private PlayerAccountHttpHeader respHeader;
    private Message messageBody;

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public Message getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(Message messageBody) {
        this.messageBody = messageBody;
    }

    public PlayerAccountHttpHeader getRespHeader() {
        return respHeader;
    }

    public void setRespHeader(PlayerAccountHttpHeader respHeader) {
        this.respHeader = respHeader;
    }

}
