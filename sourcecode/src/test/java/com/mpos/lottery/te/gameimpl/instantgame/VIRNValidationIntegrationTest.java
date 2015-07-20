package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class VIRNValidationIntegrationTest extends BaseServletIntegrationTest {
    private Log logger = LogFactory.getLog(VIRNValidationIntegrationTest.class);
    @Resource(name = "instantTicketDao")
    private InstantTicketDao instantTicketDao;
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;
    @Resource(name = "payoutDetailDao")
    private PayoutDetailDao payoutDetailDao;

    @Rollback(true)
    @Test
    public void testValidate_CASH_OK() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681983");
        ticket.setTicketXOR3("37330218");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("11111"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        PrizeLevelDto respDto = (PrizeLevelDto) respCtx.getModel();
        respDto.calculateAmount();
        assertNotNull(respDto);
        assertNotNull(respDto.getTicket());
        assertEquals(11111.0, respDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1000.0, respDto.getTaxAmount().doubleValue(), 0);
        assertEquals(10000.0, respDto.getActualAmount().doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.add(respDto.getCashActualAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(respDto.getCashActualAmount());
        expectedTrans.setGameId("IG-112");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(reqCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(reqCtx.getTerminalId());
        expectedTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectedTrans.setType(reqCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert ticket
        InstantTicket dbTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        assertNotNull(dbTicket);
        assertEquals(InstantTicket.STATUS_VALIDATED, dbTicket.getStatus());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        Payout expPayout = new Payout();
        Transaction trans = new Transaction();
        trans.setId(respCtx.getTransactionID());
        expPayout.setTransaction(trans);
        expPayout.setDevId(reqCtx.getTerminalId());
        expPayout.setMerchantId(111);
        expPayout.setOperatorId(reqCtx.getOperatorId());
        expPayout.setGameId("IG-112");
        expPayout.setGameInstanceId("IGII-112");
        expPayout.setBeforeTaxTotalAmount(respDto.getCashPrizeAmount());
        expPayout.setTotalAmount(respDto.getCashActualAmount());
        expPayout.setTicketSerialNo(ticket.getSerialNo());
        this.assertPayout(expPayout, payouts.get(0));

        // assert payout detail
        List<PayoutDetail> payoutDetails = this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        assertEquals(1, payoutDetails.size());
        assertEquals(respDto.getActualAmount().doubleValue(), payoutDetails.get(0).getActualAmount().doubleValue(), 0);
        assertEquals(respDto.getPrizeAmount().doubleValue(), payoutDetails.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(1, payoutDetails.get(0).getNumberOfObject());
    }

    /**
     * Unmatched XOR which will cause system to count the failed validation attempts.
     */
    @Test
    public void testValidate_UnmatchedXOR() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681983");
        ticket.setTicketXOR3("37330218-X");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("11111"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // assert response
        assertEquals(SystemException.CODE_NO_INSTANTPRIZE, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testValidate_CASH_Reversal() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681983");
        ticket.setTicketXOR3("37330218");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("11111"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1. validation request
        Context validationReqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(),
                payout);
        Context validationRespCtx = this.doPost(this.mockRequest(validationReqCtx));
        PrizeLevelDto respDto = (PrizeLevelDto) validationRespCtx.getModel();
        respDto.calculateAmount();

        // 2. cancel by transaction request
        Transaction trans = new Transaction();
        trans.setDeviceId(validationReqCtx.getTerminalId());
        trans.setTraceMessageId(validationReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = this.doPost(this.mockRequest(cancelReqCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, validationRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(cancelRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(respDto.getCashActualAmount());
        expectedTrans.setGameId("IG-112");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(cancelReqCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(cancelReqCtx.getTerminalId());
        expectedTrans.setTraceMessageId(cancelReqCtx.getTraceMessageId());
        expectedTrans.setType(cancelReqCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert ticket
        InstantTicket dbTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        assertNotNull(dbTicket);
        assertEquals(InstantTicket.STATUS_ACTIVE, dbTicket.getStatus());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(Payout.STATUS_REVERSED, payouts.get(0).getStatus());
    }

    @Test
    public void testValidate_FO_OK() throws Exception {
        printMethod();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("testValidate_FO_OK");
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681983");
        ticket.setTicketXOR3("27330200");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("31111"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        PrizeLevelDto respDto = (PrizeLevelDto) respCtx.getModel();
        respDto.calculateAmount();
        assertNotNull(respDto);
        assertNotNull(respDto.getTicket());
        assertEquals(31111.0, respDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(3000.0, respDto.getTaxAmount().doubleValue(), 0);
        assertEquals(30000.0, respDto.getActualAmount().doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.add(respDto.getCashActualAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(respDto.getCashActualAmount());
        expectedTrans.setGameId("IG-112");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(reqCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(reqCtx.getTerminalId());
        expectedTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectedTrans.setType(reqCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert ticket
        InstantTicket dbTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        assertNotNull(dbTicket);
        assertEquals(InstantTicket.STATUS_VALIDATED, dbTicket.getStatus());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        Payout expPayout = new Payout();
        Transaction trans = new Transaction();
        trans.setId(respCtx.getTransactionID());
        expPayout.setTransaction(trans);
        expPayout.setDevId(reqCtx.getTerminalId());
        expPayout.setMerchantId(111);
        expPayout.setOperatorId(reqCtx.getOperatorId());
        expPayout.setGameId("IG-112");
        expPayout.setGameInstanceId("IGII-112");
        expPayout.setBeforeTaxTotalAmount(respDto.getCashPrizeAmount());
        expPayout.setTotalAmount(respDto.getCashActualAmount());
        expPayout.setTicketSerialNo(ticket.getSerialNo());
        expPayout.setBeforeTaxObjectAmount(respDto.getPrizeAmount().subtract(respDto.getCashPrizeAmount()));
        expPayout.setNumberOfObject(1);
        this.assertPayout(expPayout, payouts.get(0));

        // assert payout detail
        List<PayoutDetail> payoutDetails = this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        assertEquals(1, payoutDetails.size());
        assertEquals(respDto.getActualAmount().doubleValue(), payoutDetails.get(0).getActualAmount().doubleValue(), 0);
        assertEquals(respDto.getPrizeAmount().doubleValue(), payoutDetails.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(1, payoutDetails.get(0).getNumberOfObject());

        stopWatch.stop();
        logger.debug(stopWatch.getLastTaskName() + " elapse " + stopWatch.getTotalTimeSeconds() + " seconds.");
    }

    @Test
    public void testValidate_FO_Reversal() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681983");
        ticket.setTicketXOR3("27330200");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("31111"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1. validation request
        Context validationReqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(),
                payout);
        Context validationRespCtx = this.doPost(this.mockRequest(validationReqCtx));
        PrizeLevelDto respDto = (PrizeLevelDto) validationRespCtx.getModel();
        respDto.calculateAmount();

        // 2. cancel by transaction request
        Transaction trans = new Transaction();
        trans.setDeviceId(validationReqCtx.getTerminalId());
        trans.setTraceMessageId(validationReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = this.doPost(this.mockRequest(cancelReqCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, validationRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(cancelRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(respDto.getCashActualAmount());
        expectedTrans.setGameId("IG-112");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(cancelReqCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(cancelReqCtx.getTerminalId());
        expectedTrans.setTraceMessageId(cancelReqCtx.getTraceMessageId());
        expectedTrans.setType(cancelReqCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert ticket
        InstantTicket dbTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        assertNotNull(dbTicket);
        assertEquals(InstantTicket.STATUS_ACTIVE, dbTicket.getStatus());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(Payout.STATUS_REVERSED, payouts.get(0).getStatus());
    }

    @Test
    public void testValidate_VIRN_Fail_Validated() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681002");
        ticket.setTicketXOR3("27330200");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("31111"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);

        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(SystemException.CODE_VALIDATE_NOACTIVETICKET, respCtx.getResponseCode());
    }

    @Test
    public void testValidate_Repeatedly() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681983");
        ticket.setTicketXOR3("37330218");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("11111"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);

        // 1. make 1st validation
        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // 2. make 2nd validation
        Context secondCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        Context secondRespCtx = this.doPost(this.mockRequest(secondCtx));

        // assert response
        assertEquals(SystemException.CODE_VALIDATE_REPEAT, secondRespCtx.getResponseCode());
    }

    protected void switchValidationType(int validationType) {
        this.jdbcTemplate.update("update ig_game_instance set VALIDATION_TYPE=" + validationType);
    }

    public InstantTicketDao getInstantTicketDao() {
        return instantTicketDao;
    }

    public void setInstantTicketDao(InstantTicketDao instantTicketDao) {
        this.instantTicketDao = instantTicketDao;
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public PayoutDetailDao getPayoutDetailDao() {
        return payoutDetailDao;
    }

    public void setPayoutDetailDao(PayoutDetailDao payoutDetailDao) {
        this.payoutDetailDao = payoutDetailDao;
    }

}
