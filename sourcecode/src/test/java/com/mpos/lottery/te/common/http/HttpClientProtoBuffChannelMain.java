package com.mpos.lottery.te.common.http;

import com.mpos.lottery.te.thirdpartyservice.PaymentTransactionType;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.service.DefaultCashoutService.CashoutReversalMessageBuilder;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerAccountHttpHeader;

import net.mpos.apc.entry.Cashout.ReqCashout;
import net.mpos.apc.entry.Cashout.ResCashout;
import net.mpos.fk.util.StringUtils;

import java.util.Date;

public class HttpClientProtoBuffChannelMain {
    private HttpClientProtoBuffChannel paChannel;

    public HttpClientProtoBuffChannelMain(String paUrl) throws Exception {
        paChannel = new HttpClientProtoBuffChannel(paUrl);
    }

    public void cashout() throws Exception {
        ReqCashout.Builder builder = ReqCashout.newBuilder();
        ReqCashout reqCashout = builder.setCashoutType(0).setFreeRequest(0).setPin("8888").setCashoutAmount("100.00")
                .setMerchantId("1182089").setMerchantCode("12345678").setMerchantName("kf_merchant")
                .setTransactionId("0113081500000000000465").build();

        MessageResponse response = this.paChannel.send(PaymentTransactionType.CASHOUT, HttpMethod.POST,
                new PlayerAccountHttpHeader(PaymentTransactionType.CASHOUT.getTransType(), StringUtils.getGeneralID()
                        .toPlainString(), "13987654321", new Date()), reqCashout,
                new HttpClientProtoBuffChannel.MessageResponseHandler(ResCashout.getDefaultInstance()),
                new DefaultReversalHandler(null, this.paChannel, new CashoutReversalMessageBuilder()));
    }

    public static void main(String args[]) throws Exception {
        HttpClientProtoBuffChannelMain main = new HttpClientProtoBuffChannelMain(
                "http://192.168.2.155:7155/CustomerAccountMgt/CustomerAccountPortal");
        main.cashout();
    }
}
