package com.mpos.lottery.te.thirdpartyservice.playeraccount.service;

import com.mpos.lottery.te.common.http.HttpClientProtoBuffChannel;
import com.mpos.lottery.te.common.http.HttpMethod;
import com.mpos.lottery.te.common.http.MessageResponse;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.thirdpartyservice.PaymentResponseCode;
import com.mpos.lottery.te.thirdpartyservice.PaymentTransactionType;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerAccountHttpHeader;
import com.mpos.lottery.te.workingkey.domain.Gpe;

import net.mpos.apc.entry.GetAccountInfo.ReqGetAccountInfo;
import net.mpos.apc.entry.GetAccountInfo.ResGetAccountInfo;
import net.mpos.fk.util.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.util.Assert;

import java.io.IOException;

public class DefaultAccountInfoService implements AccountInfoService {
    private Log logger = LogFactory.getLog(DefaultAccountInfoService.class);
    // SPRING DEPENDENCIES
    private HttpClientProtoBuffChannel accountSystemChannel;

    @Override
    public ResGetAccountInfo enquiry(Context<?> responseCtx, String mobileNo) throws ApplicationException {
        Assert.notNull(mobileNo, "No mobileNo supplied");

        // call remote service to get user info
        try {
            ReqGetAccountInfo.Builder builder = ReqGetAccountInfo.newBuilder();
            builder.setMobileNo(mobileNo);
            // only SGPE will affect Free_SMS
            if (Gpe.TYPE_SGPE == responseCtx.getGpe().getType()) {
                builder.setFreeRequest(1);
            } else {
                builder.setFreeRequest(0);
            }
            ReqGetAccountInfo reqAccountInfo = builder.build();

            StopWatch cashoutStopWach = new CommonsLogStopWatch("Remote GetAccountInfo");
            MessageResponse response = this.getAccountSystemChannel()
                    .send(PaymentTransactionType.ENQUIRY_ACCOUNT,
                            HttpMethod.POST,
                            new PlayerAccountHttpHeader(PaymentTransactionType.ENQUIRY_ACCOUNT.getTransType(),
                                    StringUtils.getGeneralID().toPlainString(), null, responseCtx.getTransaction()
                                            .getUpdateTime()),
                            reqAccountInfo,
                            new HttpClientProtoBuffChannel.MessageResponseHandler(ResGetAccountInfo
                                    .getDefaultInstance()), null);

            // whether the remote service has handled request successfully
            ResGetAccountInfo resAccountInfo = (ResGetAccountInfo) response.getMessageBody();
            cashoutStopWach.stop();

            int responseCode = response.getRespHeader().getResponseCode();
            if (responseCode != PaymentResponseCode.OK.getCode()) {
                throw new ApplicationException(responseCode, response.getRespHeader().getResponseDesc());
            } else {
                return resAccountInfo;
            }
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    // -------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -------------------------------------------------------

    public HttpClientProtoBuffChannel getAccountSystemChannel() {
        return accountSystemChannel;
    }

    public void setAccountSystemChannel(HttpClientProtoBuffChannel accountSystemChannel) {
        this.accountSystemChannel = accountSystemChannel;
    }

}
