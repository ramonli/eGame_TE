package com.mpos.lottery.te.gameimpl.lfn.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnTicket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class LfnPayoutReversalIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;
    @Resource(name = "newPrintTicketDao")
    private NewPrintTicketDao newPrintTicketDao;

    @Rollback(true)
    @Test
    public void testPayout_TaxOnWinnerAnalysis_NewPrint_MutiDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from ld_winning");

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1. make payout
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));
        PrizeDto prize = (PrizeDto) payoutRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. reversal
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutCtx.getTraceMessageId());
        Context reversalCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(),
                payoutTrans);
        reversalCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context reversalRespCtx = doPost(this.mockRequest(reversalCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, reversalRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert payout transaction
        Transaction dbPayoutTrans = this.getTransactionDao().findById(Transaction.class,
                payoutRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbPayoutTrans.getResponseCode());
        assertEquals(reversalRespCtx.getTransactionID(), dbPayoutTrans.getCancelTransactionId());
        assertEquals(reversalCtx.getTransType(), dbPayoutTrans.getCancelTransactionType().intValue());

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(reversalRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("LFN-1");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(reversalCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(reversalCtx.getTerminalId());
        expectedTrans.setTraceMessageId(reversalCtx.getTraceMessageId());
        expectedTrans.setType(reversalCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, reversalRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);
        assertEquals(payoutRespCtx.getTransactionID(), dbTrans.getCancelTransactionId());
        assertEquals(payoutCtx.getTransType(), dbTrans.getCancelTransactionType().intValue());

        // assert old Tickets
        List<LfnTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(3, hostTickets.size());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(1).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(2).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertTrue(hostTickets.get(1).isCountInPool());
        assertTrue(hostTickets.get(2).isCountInPool());
        // assert new printed ticket
        List<LfnTicket> newPrintTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class,
                prize.getNewPrintTicket().getSerialNo(), false);
        assertEquals(2, newPrintTickets.size());
        assertEquals(BaseTicket.STATUS_INVALID, newPrintTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_INVALID, newPrintTickets.get(1).getStatus());
        assertFalse(newPrintTickets.get(0).isCountInPool());
        assertFalse(newPrintTickets.get(1).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(22200.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(24400.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("LFN-1", payouts.get(0).getGameId());
        assertEquals("GII-111", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_REVERSED, payouts.get(0).getStatus());
        assertTrue(payouts.get(0).isValid());

        // assert Log of new printed tickets
        NewPrintTicket newPrintTicketLog = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
        assertEquals(prize.getNewPrintTicket().getSerialNo(), newPrintTicketLog.getNewTicketSerialNo());
        assertEquals(NewPrintTicket.STATUS_REVERSED, newPrintTicketLog.getStatus());
    }

    @Test
    public void testPayout_TaxOnWinnerAnalysis_NewPrint_SingleDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from LFN_TE_TICKET where id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1. make payout
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));
        PrizeDto prize = (PrizeDto) payoutRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2.issue payout confirmation
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutCtx.getTraceMessageId());
        Context reversalCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(),
                payoutTrans);
        reversalCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context reversalRespCtx = doPost(this.mockRequest(reversalCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, reversalRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert payout transaction
        Transaction dbSaleTrans = this.getTransactionDao()
                .findById(Transaction.class, payoutRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(reversalRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("LFN-1");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(reversalCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(reversalCtx.getTerminalId());
        expectedTrans.setTraceMessageId(reversalCtx.getTraceMessageId());
        expectedTrans.setType(reversalCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, reversalRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert old Tickets
        List<LfnTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(1, hostTickets.size());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(0).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(22200.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(24400.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("LFN-1", payouts.get(0).getGameId());
        assertEquals("GII-111", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_REVERSED, payouts.get(0).getStatus());
        assertTrue(payouts.get(0).isValid());
    }

    @Test
    public void testPayout_TaxOnWinnerAnalysis_Refund_MultiDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("update LFN_OPERATION_PARAMETERS set PAYOUT_MODEL=0 where ID='LFN-OP-1'");
        this.jdbcTemplate.update("delete from ld_winning");

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1.Issue payout request.
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));
        PrizeDto prize = (PrizeDto) payoutRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2.issue payout confirmation
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutCtx.getTraceMessageId());
        Context reversalCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(),
                payoutTrans);
        reversalCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context reversalRespCtx = doPost(this.mockRequest(reversalCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, reversalRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(reversalRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("LFN-1");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(reversalCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(reversalCtx.getTerminalId());
        expectedTrans.setTraceMessageId(reversalCtx.getTraceMessageId());
        expectedTrans.setType(reversalCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, reversalRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert Tickets
        List<LfnTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(1).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(2).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertTrue(hostTickets.get(1).isCountInPool());
        assertTrue(hostTickets.get(2).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(3, payouts.size());
        Payout normalPayout = this.findPayoutByGameInstance("GII-111", payouts);
        assertEquals(22200.0, normalPayout.getTotalAmount().doubleValue(), 0);
        assertEquals(24400.0, normalPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("LFN-1", normalPayout.getGameId());
        assertEquals("GII-111", normalPayout.getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, normalPayout.getType());
        assertEquals(Payout.STATUS_REVERSED, normalPayout.getStatus());
        assertTrue(normalPayout.isValid());
    }

    @Test
    public void testPayout_TaxOnWinnerAnalysis_Refund_SingleDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("update LFN_OPERATION_PARAMETERS set PAYOUT_MODEL=0 where ID='LFN-OP-1'");
        this.jdbcTemplate.update("delete from LFN_TE_TICKET where id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1.Issue payout request.
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));
        PrizeDto prize = (PrizeDto) payoutRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2.issue payout confirmation
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutCtx.getTraceMessageId());
        Context reversalCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(),
                payoutTrans);
        reversalCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context reversalRespCtx = doPost(this.mockRequest(reversalCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, reversalRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(reversalRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("LFN-1");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(reversalCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(reversalCtx.getTerminalId());
        expectedTrans.setTraceMessageId(reversalCtx.getTraceMessageId());
        expectedTrans.setType(reversalCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, reversalRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert Tickets
        List<LfnTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(0).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(22200.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(24400.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("LFN-1", payouts.get(0).getGameId());
        assertEquals("GII-111", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_REVERSED, payouts.get(0).getStatus());
        assertTrue(payouts.get(0).isValid());
    }

    /**
     * For testing AMQP reversal message.
     */
    @Rollback(true)
    @Test
    public void testReversePayout_TaxOnWinnerAnalysis_NewPrint_MutiDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from ld_winning");

        // 1. make payout
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));
        PrizeDto prize = (PrizeDto) payoutRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. reversal
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutCtx.getTraceMessageId());
        Context reversalCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), payoutTrans);
        reversalCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context reversalRespCtx = doPost(this.mockRequest(reversalCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, reversalRespCtx.getResponseCode());
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

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public NewPrintTicketDao getNewPrintTicketDao() {
        return newPrintTicketDao;
    }

    public void setNewPrintTicketDao(NewPrintTicketDao newPrintTicketDao) {
        this.newPrintTicketDao = newPrintTicketDao;
    }

}
