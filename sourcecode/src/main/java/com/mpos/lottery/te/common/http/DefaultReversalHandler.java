package com.mpos.lottery.te.common.http;

import com.google.protobuf.Message;

import com.mpos.lottery.te.thirdpartyservice.PaymentTransactionType;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerAccountHttpHeader;

import net.mpos.apc.entry.Reversal.ResReversal;

import java.io.IOException;
import java.net.URI;

public class DefaultReversalHandler implements ReversalHandler<MessageResponse> {
    private HttpClientProtoBuffChannel httpClientChannel;
    private PlayerAccountHttpHeader httpHeaders;
    private ReversalMessageBuilder reversalMessageBuilder;

    /**
     * Default constructor.
     */
    public DefaultReversalHandler(PlayerAccountHttpHeader httpHeaders, HttpClientProtoBuffChannel httpClientChannel,
            ReversalMessageBuilder reversalMessageBuilder) {
        if (httpClientChannel == null) {
            throw new IllegalArgumentException("argument 'httpClientChannel' can NOT be null");
        }
        this.httpClientChannel = httpClientChannel;
        this.httpHeaders = httpHeaders;
        this.reversalMessageBuilder = reversalMessageBuilder;
    }

    @Override
    public MessageResponse reverse(URI uri, Message orignalMsg) throws IOException {
        return this.httpClientChannel.send(PaymentTransactionType.REVERSAL, uri, HttpMethod.POST, this.httpHeaders,
                this.reversalMessageBuilder.build(orignalMsg), new HttpClientProtoBuffChannel.MessageResponseHandler(
                        ResReversal.getDefaultInstance()), null);
    }

}
