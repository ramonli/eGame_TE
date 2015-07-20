package com.mpos.lottery.te.thirdpartyservice.playeraccount.service;

import com.mpos.lottery.te.common.http.DefaultReversalHandler;
import com.mpos.lottery.te.common.http.HttpClientProtoBuffChannel;
import com.mpos.lottery.te.common.http.HttpMethod;
import com.mpos.lottery.te.common.http.MessageResponse;
import com.mpos.lottery.te.common.http.RemoteServiceException;
import com.mpos.lottery.te.common.http.ReversalMessageBuilder;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.service.balance.BalanceService;
import com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.thirdpartyservice.PaymentResponseCode;
import com.mpos.lottery.te.thirdpartyservice.PaymentTransactionType;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutRequest;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutResponse;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerAccountHttpHeader;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;
import com.mpos.lottery.te.workingkey.domain.Gpe;

import net.mpos.apc.entry.Cashout.ReqCashout;
import net.mpos.apc.entry.Cashout.ResCashout;
import net.mpos.apc.entry.GetAccountInfo.ResGetAccountInfo;
import net.mpos.apc.entry.Reversal.ReqReversal;
import net.mpos.apc.entry.Reversal.ResReversal;
import net.mpos.fk.util.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;

public class DefaultCashoutService extends AbstractReversalOrCancelStrategy implements CashoutService {
    private Log logger = LogFactory.getLog(DefaultCashoutService.class);
    // SPRING DEPENDENCIES
    private AccountInfoService accountInfoService;
    private CreditService creditService;
    private MerchantService merchantService;
    private HttpClientProtoBuffChannel accountSystemChannel;
    private OperatorDao operatorDao;
    
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;
    @Resource(name = "cashoutMobileCommissionBalanceService")
    private CommissionBalanceService commissionService;
    

    @Override
    public CashoutResponse cashout(Context<?> responseCtx, CashoutRequest request) throws ApplicationException {
        if (request.getReferenceNum() == null && request.getCashoutAmount() == null) {
            throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY,
                    "Either 'referenceNo or amount must be provided.");
        }

        // perform local checking
        Merchant leafMerchant = this.getMerchantService().getMerchant(responseCtx.getMerchant().getId());
        if (request.getCashoutAmount() == null || request.getCashoutAmount().compareTo(new BigDecimal("0")) == 0) {
            logger.info("No need to check cashout limit operation is by reference number.");
        } else {
            this.getMerchantService().allowCashout(responseCtx, request.getCashoutAmount());
        }

        // lookup user info first
        ResGetAccountInfo accountInfo = this.getAccountInfoService().enquiry(responseCtx, request.getMobile());
        // call remote service
        try {
            // build cashout request
            ReqCashout.Builder builder = ReqCashout.newBuilder();
            if (request.getReferenceNum() != null) {
                // SHIT, if setReferenceNo(null), you will get a
                // NullPointerException even this field is optional in protocbuf
                // def file.
                builder.setReferenceNo(request.getReferenceNum());
                builder.setCashoutType(1);
            } else {
                builder.setCashoutAmount(request.getCashoutAmount().toString());
                builder.setCashoutType(0);
            }
            // only SGPE will affect Free_SMS
            if (Gpe.TYPE_SGPE == responseCtx.getGpe().getType()) {
                builder.setFreeRequest(1);
            } else {
                builder.setFreeRequest(0);
            }

            ReqCashout reqCashout = builder.setPin(request.getUserPIN()).setMerchantId(leafMerchant.getId() + "")
                    .setMerchantCode(leafMerchant.getCode()).setMerchantName(leafMerchant.getName())
                    .setTransactionId(responseCtx.getTransaction().getId()).build();

            StopWatch cashoutStopWach = new CommonsLogStopWatch("Remote Cashout");
            MessageResponse response = this.getAccountSystemChannel().send(
                    PaymentTransactionType.CASHOUT,
                    HttpMethod.POST,
                    new PlayerAccountHttpHeader(PaymentTransactionType.CASHOUT.getTransType(), StringUtils
                            .getGeneralID().toPlainString(), accountInfo.getUserId(), responseCtx.getTransaction()
                            .getUpdateTime()),
                    reqCashout,
                    new HttpClientProtoBuffChannel.MessageResponseHandler(ResCashout.getDefaultInstance()),
                    // set reversal handler
                    new DefaultReversalHandler(new PlayerAccountHttpHeader(PaymentTransactionType.REVERSAL
                            .getTransType(), StringUtils.getGeneralID().toPlainString(), accountInfo.getUserId(),
                            new Date()), this.accountSystemChannel, new CashoutReversalMessageBuilder()));

            int responseCode = response.getRespHeader().getResponseCode();
            cashoutStopWach.stop();

            if (responseCode != PaymentResponseCode.OK.getCode()) {
                logger.warn("Get response of transaction(devid=" + responseCtx.getTerminalId() + ",traceMsgId="
                        + responseCtx.getTraceMessageId() + "): responseCode:" + responseCode + ",desc:"
                        + response.getRespHeader().getResponseDesc());
                // can't throw out ApplicationException here, as this exception
                // will mark current transaction as rollback-only, and then the
                // 'cancel by transaction' can't find this transaction.

                // throw new ApplicationException(responseCode,
                // resCashout.getResponseDesc());
                responseCtx.setResponseCode(responseCode);
                // in such case we should'n publish AMQP message
                responseCtx.setProperty(Context.KEY_PUBLISH_AMQP_MESSAGE, false);
            } else {
                // whether the remote service has handled request successfully
                ResCashout resCashout = (ResCashout) response.getMessageBody();
                // update daily cashout-level if get successful response
                this.updateDailyCashoutLevel(responseCtx.getOperator(), new BigDecimal(resCashout.getCashoutAmount()),
                        true);
//                this.getCreditService().credit(responseCtx.getOperatorId(), responseCtx.getMerchant().getId(),
//                        new BigDecimal(resCashout.getCashoutAmount()), true, false);

                // NOTE: update total amount of transaction, reversal request
                // will need this information.
                responseCtx.getTransaction().setTotalAmount(new BigDecimal(resCashout.getCashoutAmount()));
                // save userId for later reversal
                responseCtx.getTransaction().setBatchNumber(accountInfo.getUserId());
                
                // Add Operator cashout balance & commission balance
                // --------------------------------
                // Maintain cashout balance and commission
                // --------------------------------
                // update cash out balance
                Object operatorMerchant = this.getBalanceService().balance(responseCtx, BalanceService.BALANCE_TYPE_CASHOUT,
                       responseCtx.getTransaction().getOperatorId(), true);
                this.getCommissionService().calCommission(responseCtx, operatorMerchant);
                
                return new CashoutResponse(request, resCashout);
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            // can't throw out exception here.
            responseCtx.setResponseCode(SystemException.CODE_INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    /**
     * Reverse the cashout transaction.
     */
    @Override
    public boolean cancelOrReverse(Context respCtx, Transaction originalTrans) throws ApplicationException {
        try {
            // build reversal request
            ReqReversal reqReversal = ReqReversal.newBuilder().setTransactionId(originalTrans.getId())
                    .setTransType(PaymentTransactionType.CASHOUT.getTransType() + "").build();

            MessageResponse response = this.getAccountSystemChannel().send(
                    PaymentTransactionType.REVERSAL,
                    HttpMethod.POST,
                    // user batchNo. to store userId
                    new PlayerAccountHttpHeader(PaymentTransactionType.REVERSAL.getTransType(), StringUtils
                            .getGeneralID().toPlainString(), originalTrans.getBatchNumber(), new Date()), reqReversal,
                    new HttpClientProtoBuffChannel.MessageResponseHandler(ResReversal.getDefaultInstance()), null);

            StopWatch reversalStopWatch = new CommonsLogStopWatch("Remote Cashout Reversal");
            ResReversal resReversal = (ResReversal) response.getMessageBody();
            int responseCode = response.getRespHeader().getResponseCode();
            reversalStopWatch.stop();

            if (responseCode != PaymentResponseCode.OK.getCode()) {
                logger.warn("Get response of reversal transaction: responseCode:" + responseCode);
                throw new ApplicationException(responseCode, "Fail to reverse cashout transaction(id="
                        + originalTrans.getId() + ")");
            } else {
                // perform local reversal
                // restore daily cash out level.
                this.updateDailyCashoutLevel(
                        this.getOperatorDao().findById(Operator.class, originalTrans.getOperatorId()),
                        originalTrans.getTotalAmount(), false);
                // update credit level
//                this.getCreditService().credit(originalTrans.getOperatorId(), originalTrans.getMerchantId(),
//                        originalTrans.getTotalAmount(), false, false);
                // --------------------------------
                // Maintain cash out balance and commission
                // --------------------------------
                // update cash out balance
                Object operatorMerchant = this.getBalanceService().balance(respCtx, BalanceService.BALANCE_TYPE_CASHOUT,
                        originalTrans.getOperatorId(), false);
                this.getCommissionService().cancelCommission(respCtx, originalTrans, operatorMerchant);
            }
        } catch (IOException e) {
            throw new RemoteServiceException(e);
        }

        return false;
    }

    @Override
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(TransactionType.PLAYER_CASH_OUT.getRequestType());
    }

    public static class CashoutReversalMessageBuilder implements ReversalMessageBuilder<ReqCashout> {

        @Override
        public ReqReversal build(ReqCashout reqCashout) {
            return ReqReversal.newBuilder().setTransactionId(reqCashout.getTransactionId())
                    .setTransType(PaymentTransactionType.CASHOUT.getTransType() + "").build();
        }

    }

    protected void updateDailyCashoutLevel(Operator operator, BigDecimal cashoutAmount, boolean isRestore)
            throws ApplicationException {
        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "No operator found.");
        }
        if (isRestore) {
            operator.setDailyCashoutLevel(cashoutAmount.add(operator.getDailyCashoutLevel()));
        } else {
            operator.setDailyCashoutLevel(operator.getDailyCashoutLevel().subtract(cashoutAmount));
        }
        this.getOperatorDao().update(operator);
        if (logger.isDebugEnabled()) {
            logger.debug("After cash out[dailyCashoutLevel: " + operator.getDailyCashoutLevel() + "].");
        }
    }

    // -------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -------------------------------------------------------

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public HttpClientProtoBuffChannel getAccountSystemChannel() {
        return accountSystemChannel;
    }

    public void setAccountSystemChannel(HttpClientProtoBuffChannel accountSystemChannel) {
        this.accountSystemChannel = accountSystemChannel;
    }

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

    public AccountInfoService getAccountInfoService() {
        return accountInfoService;
    }

    public void setAccountInfoService(AccountInfoService accountInfoService) {
        this.accountInfoService = accountInfoService;
    }

    public BalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public CommissionBalanceService getCommissionService() {
        return commissionService;
    }

    public void setCommissionService(CommissionBalanceService commissionService) {
        this.commissionService = commissionService;
    }

}
