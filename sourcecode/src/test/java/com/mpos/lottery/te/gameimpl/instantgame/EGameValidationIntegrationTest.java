package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.Constants;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class EGameValidationIntegrationTest extends BaseServletIntegrationTest {
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
    @Resource(name = "operatorDao")
    private OperatorDao operatorDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    /**
     * Win cash prize
     */
    @Test
    public void testValidate_Cash_OK_Operator() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("157823119021");
        ticket.setTicketXOR3("95497797");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("400000"));
        payout.setTicket(ticket);

        this.jdbcTemplate
                .update("update merchant_game_properties mg set mg.commission_rate_payout=0.1  where MRID like '%-IG-%'");
        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);

        // request validation
        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_INSTANT_GAME + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        PrizeLevelDto respDto = (PrizeLevelDto) respCtx.getModel();
        respDto.calculateAmount();
        assertNotNull(respDto);
        assertNotNull(respDto.getTicket());
        assertEquals(400000.0, respDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(80000.0, respDto.getTaxAmount().doubleValue(), 0);
        assertEquals(320000.0, respDto.getActualAmount().doubleValue(), 0);

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
        expPayout.setGameInstanceId("IGII-111");
        expPayout.setBeforeTaxTotalAmount(respDto.getPrizeAmount());
        expPayout.setTotalAmount(respDto.getActualAmount());
        expPayout.setTicketSerialNo(ticket.getSerialNo());
        this.assertPayout(expPayout, payouts.get(0));

        // assert payout detail
        List<PayoutDetail> payoutDetails = this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        assertEquals(1, payoutDetails.size());
        assertEquals(respDto.getActualAmount().doubleValue(), payoutDetails.get(0).getActualAmount().doubleValue(), 0);
        assertEquals(respDto.getPrizeAmount().doubleValue(), payoutDetails.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(1, payoutDetails.get(0).getNumberOfObject());

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();
        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {

            // commission
            Operator operator = operatorDao.findById(Operator.class, "OPERATOR-111");
            assertEquals(32000.0, operator.getCommisionBalance().doubleValue(), 0);

            // commission balance
            List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
            BalanceTransactions balanceTransactions = list.get(0);
            assertEquals(1, list.size());
            assertEquals(BalanceTransactions.STATUS_VALID, balanceTransactions.getStatus());
            assertEquals(320000.0, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
            assertEquals("OPERATOR-111", balanceTransactions.getOwnerId());
            assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, balanceTransactions.getOwnerType());
            assertEquals(32000.0, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
            assertEquals(0.1, balanceTransactions.getCommissionRate().doubleValue(), 0.0);
        }
    }

    /**
     * Unmatched XOR which will cause system to count the failed validation attempts.
     */
    @Test
    public void testValidate_UnmatchedXOR() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("157823119021");
        ticket.setTicketXOR3("954977974");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("400000"));
        payout.setTicket(ticket);

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);

        // request validation
        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_INSTANT_GAME + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // assert response
        assertEquals(SystemException.CODE_XORMD5_NOTMATCH, respCtx.getResponseCode());

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
        ticket.setRawSerialNo("157823119021");
        ticket.setTicketXOR3("95497797");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("400000"));
        payout.setTicket(ticket);
        this.jdbcTemplate
                .update("update merchant_game_properties mg set mg.commission_rate_payout=0.1  where MRID like '%-IG-%'");
        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);
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

        this.entityManager.flush();
        this.entityManager.clear();

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

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();
        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {

            // commission
            Operator operator = operatorDao.findById(Operator.class, "OPERATOR-111");
            assertEquals(0.0, operator.getCommisionBalance().doubleValue(), 0);

            // operator = operatorDao.findById(Operator.class, "OPERATOR-111");
            // assertEquals(0.0, operator.getCommisionBalance().doubleValue(), 0);

            List<BalanceTransactions> cancellationList = balanceTransactionsDao.findBalanceTransactions(cancelRespCtx
                    .getTransactionID());
            List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(validationRespCtx
                    .getTransactionID());
            BalanceTransactions balanceTransactions = list.get(0);
            BalanceTransactions cancellationBalanceTransactions = cancellationList.get(0);

            assertEquals(1, cancellationList.size());
            assertEquals(BalanceTransactions.STATUS_VALID, cancellationBalanceTransactions.getStatus());
            assertEquals(-320000.0, cancellationBalanceTransactions.getTransactionAmount().doubleValue(), 0.0);
            assertEquals("OPERATOR-111", cancellationBalanceTransactions.getOwnerId());
            assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, cancellationBalanceTransactions.getOwnerType());
            assertEquals(-32000.0, cancellationBalanceTransactions.getCommissionAmount().doubleValue(), 0.0);

            assertEquals(1, list.size());
            assertEquals(BalanceTransactions.STATUS_INVALID, balanceTransactions.getStatus());
        }
    }

    /**
     * Win both cash and object
     */
    @Test
    public void testValidate_Object_Cash_OK() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("157823119021");
        ticket.setTicketXOR3("95497797");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("1404000"));
        payout.setTicket(ticket);

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);
        // remove the prize group item
        this.jdbcTemplate.update("update BD_PRIZE_LEVEL set PRIZE_LEVEL_TYPE=3 where ID='PL-1'");
        this.jdbcTemplate
                .update("update BD_PRIZE_LEVEL_ITEM set BD_PRIZE_LEVEL_ID='PL-1' where BD_PRIZE_LEVEL_ID='PL-4'");

        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_INSTANT_GAME + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        PrizeLevelDto respDto = (PrizeLevelDto) respCtx.getModel();
        respDto.calculateAmount();
        assertNotNull(respDto);
        assertNotNull(respDto.getTicket());
        assertEquals(1404000.0, respDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(161000.0, respDto.getTaxAmount().doubleValue(), 0);
        assertEquals(1243000.0, respDto.getActualAmount().doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        // only cash prize will be counted into payout credit level
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
        expPayout.setGameInstanceId("IGII-111");
        // cash prize amount, before tax
        expPayout.setBeforeTaxTotalAmount(new BigDecimal("1400000"));
        // cash prize amount, after tax
        expPayout.setTotalAmount(new BigDecimal("1240000"));
        expPayout.setTicketSerialNo(ticket.getSerialNo());
        expPayout.setBeforeTaxObjectAmount(new BigDecimal("4000"));
        expPayout.setNumberOfObject(2);
        this.assertPayout(expPayout, payouts.get(0));

        // assert payout detail
        List<PayoutDetail> payoutDetails = this.getPayoutDetailDao().findByPayout(payouts.get(0).getId());
        assertEquals(3, payoutDetails.size());
        this.sortPayoutDetailByPrizeAmount(payoutDetails);
        // <Item name="Sony Camera" numberOfItem="2" prizeAmount="2000"
        // taxAmount="500" type="2"/>
        assertEquals(2000.0, payoutDetails.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(1500.0, payoutDetails.get(0).getActualAmount().doubleValue(), 0);
        assertEquals(2, payoutDetails.get(0).getNumberOfObject());
        // <Item numberOfItem="1" prizeAmount="400000" taxAmount="80000"
        // type="1"/>
        assertEquals(400000.0, payoutDetails.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(320000.0, payoutDetails.get(1).getActualAmount().doubleValue(), 0);
        assertEquals(1, payoutDetails.get(1).getNumberOfObject());
        // <Item numberOfItem="1" prizeAmount="1000000" taxAmount="80000"
        // type="1"/>
        assertEquals(1000000.0, payoutDetails.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(920000.0, payoutDetails.get(2).getActualAmount().doubleValue(), 0);
        assertEquals(1, payoutDetails.get(2).getNumberOfObject());
    }

    @Test
    public void testValidate_Object_Cash_Reversal() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("157823119021");
        ticket.setTicketXOR3("95497797");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("1404000"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);
        // make sure the ticket wins cash and object
        this.jdbcTemplate.update("update BD_PRIZE_LEVEL set PRIZE_LEVEL_TYPE=3 where ID='PL-1'");
        this.jdbcTemplate
                .update("update BD_PRIZE_LEVEL_ITEM set BD_PRIZE_LEVEL_ID='PL-1' where BD_PRIZE_LEVEL_ID='PL-4'");

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

        this.entityManager.flush();
        this.entityManager.clear();

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
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("984161896312");
        ticket.setTicketXOR3("14556304");
        PrizeLevelDto payout = new PrizeLevelDto();
        payout.setClientPrizeAmount(new BigDecimal("500000"));
        payout.setTicket(ticket);

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);

        Context reqCtx = this.getDefaultContext(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), payout);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_INSTANT_GAME + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(SystemException.CODE_VALIDATE_NOACTIVETICKET, respCtx.getResponseCode());

        // check database output
        InstantTicket dbTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        assertNotNull(dbTicket);
        assertEquals(InstantTicket.STATUS_INACTIVE, dbTicket.getStatus());
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

    /**
     * @return return operatorDao
     */
    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

}
