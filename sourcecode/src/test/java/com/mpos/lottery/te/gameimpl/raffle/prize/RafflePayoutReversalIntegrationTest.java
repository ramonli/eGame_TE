package com.mpos.lottery.te.gameimpl.raffle.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
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

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class RafflePayoutReversalIntegrationTest extends BaseServletIntegrationTest {
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

    @Test
    public void testPayout_TaxOnWinnerAnalysis_NewPrint_MutiDraw_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1. make payout
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
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
        Transaction dbSaleTrans = this.getTransactionDao()
                .findById(Transaction.class, payoutRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(reversalRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("RA-1");
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
        List<RaffleTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                ticket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(1).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertTrue(hostTickets.get(1).isCountInPool());
        // assert new printed ticket
        List<RaffleTicket> newPrintTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                prize.getNewPrintTicket().getSerialNo(), false);
        assertEquals(1, newPrintTickets.size());
        assertEquals(BaseTicket.STATUS_INVALID, newPrintTickets.get(0).getStatus());
        assertFalse(newPrintTickets.get(0).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(2480000.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(2800000.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("RA-1", payouts.get(0).getGameId());
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
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from RA_TE_TICKET where id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1. make payout
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
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
        expectedTrans.setGameId("RA-1");
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
        List<RaffleTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                ticket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(0).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(2480000.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(2800000.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("RA-1", payouts.get(0).getGameId());
        assertEquals("GII-111", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_REVERSED, payouts.get(0).getStatus());
        assertTrue(payouts.get(0).isValid());
    }

    @Test
    public void testPayout_TaxOnWinnerAnalysis_Refund_MultiDraw_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("update RA_OPERATION_PARAMETERS set PAYOUT_MODEL=0");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1.Issue payout request.
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
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
        expectedTrans.setGameId("RA-1");
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
        List<RaffleTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(1).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertTrue(hostTickets.get(1).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(2, payouts.size());
        Payout normalPayout = this.findPayoutByGameInstance("GII-111", payouts);
        assertEquals(2480000.0, normalPayout.getTotalAmount().doubleValue(), 0);
        assertEquals(2800000.0, normalPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("RA-1", normalPayout.getGameId());
        assertEquals("GII-111", normalPayout.getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, normalPayout.getType());
        assertEquals(Payout.STATUS_REVERSED, normalPayout.getStatus());
        assertTrue(normalPayout.isValid());
    }

    @Test
    public void testPayout_TaxOnWinnerAnalysis_Refund_SingleDraw_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("update RA_OPERATION_PARAMETERS set PAYOUT_MODEL=0");
        this.jdbcTemplate.update("delete from RA_TE_TICKET where id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // 1.Issue payout request.
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
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
        expectedTrans.setGameId("RA-1");
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
        List<RaffleTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(0).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(2480000.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(2800000.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("RA-1", payouts.get(0).getGameId());
        assertEquals("GII-111", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_REVERSED, payouts.get(0).getStatus());
        assertTrue(payouts.get(0).isValid());
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
