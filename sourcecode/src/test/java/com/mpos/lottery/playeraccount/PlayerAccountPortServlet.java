package com.mpos.lottery.playeraccount;

import com.google.protobuf.Message;

import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.common.http.HttpRequestUtil;
import com.mpos.lottery.te.common.util.Base64Coder;
import com.mpos.lottery.te.common.util.HexCoder;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.thirdpartyservice.PaymentTransactionType;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerAccountHttpHeader;

import net.mpos.apc.entry.Cashout.ReqCashout;
import net.mpos.apc.entry.Cashout.ResCashout;
import net.mpos.apc.entry.GetAccountInfo.ReqGetAccountInfo;
import net.mpos.apc.entry.GetAccountInfo.ResGetAccountInfo;
import net.mpos.apc.entry.Reversal.ReqReversal;
import net.mpos.apc.entry.Reversal.ResReversal;
import net.mpos.apc.entry.VoucherActive.ReqVoucherActive;
import net.mpos.apc.entry.VoucherActive.ResVoucherActive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Log4jConfigurer;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is only port exposed by TE. All requests will received by <code>TEPortServlet</code>, and \ then the servlet
 * will dispatch request to <code>DispatchController</code>.
 */
public class PlayerAccountPortServlet extends HttpServlet {
    private static final long serialVersionUID = 9086524025083659379L;
    private static final String DESKEY = "W0JAMTZkYWRmOTU3MTUwYWQxLWNkMTEt";

    // private org.apache.log4j.Logger logger = org.apache.log4j.Logger
    // .getLogger(PlayerAccountPortServlet.class);
    Logger logger = LoggerFactory.getLogger(PlayerAccountPortServlet.class);

    public void init(ServletConfig config) throws ServletException {
        try {
            /**
             * Why?? what is the fuck?? Log4j can't work under jetty plugin of Gradle. I knew that Gradle uses
             * slf4j+logback, but in my understanding, it won't affect the use of common-logging+log4j in my
             * application.
             * <p/>
             * I have tried several times, calling Logger of slf4j, message can be output at WARN log, buy I have no
             * idea where is the configuration files of logback. Log4j completely doesn't function.
             * <p/>
             * Ok, maybe it is time to learn logback :).
             */
            Log4jConfigurer.initLogging("classpath:log4j.properties");
            config.getServletContext().log("Initialize log4j successfully...but it won't work!");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // logger.info("Hello.");
        logger.warn("WARN:Hello-1");
        request.getSession().getServletContext().log("doGet()");
        response.getWriter().write("<html><body>PlayerAccountServlet is launched successfuly.</body></html>");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // descypt transType
            String strTransType = encryptHeader(request.getHeader("TRANS_TYPE"), false);
            int transType = Integer.parseInt(strTransType);

            byte[] encryptedBodyBytes = HttpRequestUtil.getContentAsByte(request);
            byte[] bodyBytes = TriperDESCipher.decrypt(Base64Coder.decode(DESKEY), encryptedBodyBytes,
                    HexCoder.hexToBuffer(TriperDESCipher.STR_IV));

            Message responseMsg = null;
            logger.warn("transaction type: " + transType);
            if (transType == PaymentTransactionType.REVERSAL.getTransType()) {
                responseMsg = this.reverse(request, response, bodyBytes);
            } else if (transType == PaymentTransactionType.CASHOUT.getTransType()) {
                responseMsg = this.cashout(request, response, bodyBytes);
            } else if (transType == PaymentTransactionType.ENQUIRY_ACCOUNT.getTransType()) {
                responseMsg = this.enquiryAccount(request, response, bodyBytes);
            }

            byte[] encryptedResponseBody = TriperDESCipher.encrypt(Base64Coder.decode(DESKEY),
                    responseMsg.toByteArray(), HexCoder.hexToBuffer(TriperDESCipher.STR_IV));

            response.getOutputStream().write(encryptedResponseBody);
            response.getOutputStream().close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Message reverse(HttpServletRequest request, HttpServletResponse response, byte[] bodyBytes)
            throws Exception {
        Thread.currentThread().sleep(10 * 1000);
        ReqReversal reqReversal = ReqReversal.parseFrom(bodyBytes);
        logger.warn("ReqReversal: " + reqReversal);

        Message responseMsg = null;
        if ("1111".equalsIgnoreCase(reqReversal.getTransactionId())) {
            // build response
            responseMsg = ResReversal.newBuilder().build();
            response.setHeader(PlayerAccountHttpHeader.HEADER_RESPONSE_CODE, encryptHeader("201", true));
        } else {
            responseMsg = ResReversal.newBuilder().build();
            response.setHeader(PlayerAccountHttpHeader.HEADER_RESPONSE_CODE, encryptHeader("200", true));
        }
        return responseMsg;
    }

    private Message cashout(HttpServletRequest request, HttpServletResponse response, byte[] bodyBytes)
            throws Exception {
        ReqCashout reqCashout = ReqCashout.parseFrom(bodyBytes);
        logger.warn("ReqCashout: " + reqCashout);

        String userId = encryptHeader(request.getHeader(PlayerAccountHttpHeader.HEADER_USER_ID), false);
        if ("123456".equalsIgnoreCase(userId)) {
            logger.debug("will sleep 40 seconds");
            Thread.currentThread().sleep(40 * 1000);
        } else {
            Thread.currentThread().sleep(10 * 1000);
        }

        Message responseMsg = null;
        if ("13800138000".equalsIgnoreCase(userId)) {
            // build response
            responseMsg = ResCashout.newBuilder().setPrizeAmount("12000.0").setCashoutAmount("11000.0")
                    .setMobileNo(userId).setResponseDesc("What is the fuck?").build();
            response.setHeader(PlayerAccountHttpHeader.HEADER_RESPONSE_CODE, encryptHeader("201", true));
        } else {
            // successful case
            responseMsg = ResCashout.newBuilder().setCashoutAmount("11000.0").setPrizeAmount("12000.0")
                    .setMobileNo(userId).build();
            response.setHeader(PlayerAccountHttpHeader.HEADER_RESPONSE_CODE, encryptHeader("200", true));
        }
        return responseMsg;
    }

    private Message enquiryAccount(HttpServletRequest request, HttpServletResponse response, byte[] bodyBytes)
            throws Exception {
        ReqGetAccountInfo reqAccountInfo = ReqGetAccountInfo.parseFrom(bodyBytes);
        logger.warn("ReqVoucherActive: " + reqAccountInfo);

        Message responseMsg = null;
        responseMsg = ResGetAccountInfo.newBuilder().setMobileNo(reqAccountInfo.getMobileNo())
                .setUserId(reqAccountInfo.getMobileNo()).setCashoutPassword("111").setSellPin("111").setStatus(1)
                .setTopupAmount("1000").setPrizeAmount("2000").setValidPeriodTime("10").setTransactionPwdValid(10)
                .setLastLoginTime("20120227").setFreeSmsCount(0).build();
        response.setHeader(PlayerAccountHttpHeader.HEADER_RESPONSE_CODE, encryptHeader("200", true));
        return responseMsg;
    }

    private Message activeVoucher(byte[] bodyBytes) throws Exception {
        ReqVoucherActive reqVoucherActive = ReqVoucherActive.parseFrom(bodyBytes);
        logger.warn("ReqVoucherActive: " + reqVoucherActive);

        Message responseMsg = null;
        if ("M-1".equalsIgnoreCase(reqVoucherActive.getMerchantCode())) {
            // build response
            responseMsg = ResVoucherActive.newBuilder().setActiveCount(13).setResponseCode(201 + "").build();
        } else if ("M-222".equalsIgnoreCase(reqVoucherActive.getMerchantCode())) {
            logger.warn("Sleep 40 seconds");
            Thread.currentThread().sleep(40 * 1000);
            responseMsg = ResVoucherActive.newBuilder().setActiveCount(13).setResponseCode("200").build();
        } else {
            responseMsg = ResVoucherActive.newBuilder().setActiveCount(13).setResponseCode("200").build();
        }
        return responseMsg;
    }

    protected static String encryptHeader(String headerValue, boolean isEncrypt) {
        try {
            if (isEncrypt) {
                return TriperDESCipher.encrypt(DESKEY, headerValue, TriperDESCipher.STR_IV);
            } else {
                return TriperDESCipher.decrypt(DESKEY, headerValue, TriperDESCipher.STR_IV);
            }
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }
}
