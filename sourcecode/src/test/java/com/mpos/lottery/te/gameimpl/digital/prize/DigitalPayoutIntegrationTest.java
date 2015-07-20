package com.mpos.lottery.te.gameimpl.digital.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
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

public class DigitalPayoutIntegrationTest extends BaseServletIntegrationTest {
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
    public void testPayout_TaxOnWinnerAnalysis_NewPrint_MultiDraw_OK() throws Exception {
        printMethod();
        DigitalTicket ticket = new DigitalTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from ld_winning");

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // issue payout request
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));

        // assert response
        assertEquals(200, payoutRespCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) payoutRespCtx.getModel();
        assertEquals(22200.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(24400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(2200.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        DigitalTicket newPrintTicket = (DigitalTicket) prize.getNewPrintTicket();
        assertEquals(1400.0, newPrintTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(2, newPrintTicket.getMultipleDraws());
        assertEquals("11002", newPrintTicket.getGameInstance().getNumber());
        assertEquals("11003", newPrintTicket.getLastDrawNo());
        assertEquals(2, newPrintTicket.getEntries().size());
        assertEquals(6, newPrintTicket.getValidationCode().length());
        assertNotNull(newPrintTicket.getBarcode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.add(prize.getActualAmount()).doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(payoutRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("FD-1");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(payoutCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(payoutCtx.getTerminalId());
        expectedTrans.setTraceMessageId(payoutCtx.getTraceMessageId());
        expectedTrans.setType(payoutCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, payoutRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert old Tickets
        List<DigitalTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_PAID, hostTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(1).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTickets.get(2).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertTrue(hostTickets.get(1).isCountInPool());
        assertTrue(hostTickets.get(2).isCountInPool());
        // assert new printed ticket
        List<DigitalTicket> newPrintTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                prize.getNewPrintTicket().getSerialNo(), false);
        assertEquals(2, newPrintTickets.size());
        assertEquals(BaseTicket.STATUS_ACCEPTED, newPrintTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, newPrintTickets.get(1).getStatus());
        assertTrue(newPrintTickets.get(0).isCountInPool());
        assertTrue(newPrintTickets.get(1).isCountInPool());
        assertEquals(TransactionType.PAYOUT.getRequestType(), newPrintTickets.get(0).getTransType());
        assertEquals(TransactionType.PAYOUT.getRequestType(), newPrintTickets.get(1).getTransType());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(22200.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(24400.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("FD-1", payouts.get(0).getGameId());
        assertEquals(payoutCtx.getOperatorId(), payouts.get(0).getOperatorId());
        assertEquals(111, payouts.get(0).getMerchantId());
        assertEquals(payoutCtx.getTerminalId(), payouts.get(0).getDevId());
        assertEquals("GII-111", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());
        assertTrue(payouts.get(0).isValid());

        // assert Log of new printed tickets
        NewPrintTicket newPrintTicketLog = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
        assertEquals(newPrintTicket.getSerialNo(), newPrintTicketLog.getNewTicketSerialNo());
        assertEquals(NewPrintTicket.STATUS_WAITCONFIRM, newPrintTicketLog.getStatus());
    }

    @Test
    public void testPayout_TaxOnWinnerAnalysis_NewPrint_SingleDraw_OK() throws Exception {
        printMethod();
        DigitalTicket ticket = new DigitalTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        this.jdbcTemplate.update("delete from TE_FD_TICKET where id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");

        // issue payout request
        Context payoutCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));

        // assert response
        assertEquals(200, payoutRespCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) payoutRespCtx.getModel();
        assertEquals(22200.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(24400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(2200.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        assertNull(prize.getNewPrintTicket());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.add(prize.getActualAmount()).doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(payoutRespCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("FD-1");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(payoutCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(payoutCtx.getTerminalId());
        expectedTrans.setTraceMessageId(payoutCtx.getTraceMessageId());
        expectedTrans.setType(payoutCtx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, payoutRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert old Tickets
        List<DigitalTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                ticket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        assertEquals(BaseTicket.STATUS_PAID, hostTickets.get(0).getStatus());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(22200.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(24400.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("FD-1", payouts.get(0).getGameId());
        assertEquals("GII-111", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());
        assertTrue(payouts.get(0).isValid());

        // assert Log of new printed tickets
        assertNull(this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo()));
    }

    @Test
    public void testPayout_TaxOnWinnerAnalysis_Refund_MultiDraw_OK() throws Exception {
        printMethod();
        DigitalTicket ticket = new DigitalTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("update FD_OPERATION_PARAMETERS set PAYOUT_MODEL=0 where ID='FD-OP-1'");
        this.jdbcTemplate.update("delete from ld_winning");

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(23600.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(24400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(2200.0, prize.getTaxAmount().doubleValue(), 0);
        assertEquals(1400.0, prize.getReturnAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_REFUND, prize.getPayoutMode());
        assertEquals(0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        assertNull(prize.getNewPrintTicket());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.add(prize.getActualAmount()).doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("FD-1");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(respCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(respCtx.getTerminalId());
        expectedTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectedTrans.setType(ctx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert Tickets
        List<DigitalTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_PAID, hostTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_RETURNED, hostTickets.get(1).getStatus());
        assertEquals(BaseTicket.STATUS_RETURNED, hostTickets.get(2).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertFalse(hostTickets.get(1).isCountInPool());
        assertFalse(hostTickets.get(2).isCountInPool());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(3, payouts.size());
        Payout normalPayout = this.findPayoutByGameInstance("GII-111", payouts);
        assertEquals(22200.0, normalPayout.getTotalAmount().doubleValue(), 0);
        assertEquals(24400.0, normalPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("FD-1", normalPayout.getGameId());
        assertEquals("GII-111", normalPayout.getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, normalPayout.getType());
        assertEquals(Payout.STATUS_PAID, normalPayout.getStatus());
        assertTrue(normalPayout.isValid());
    }

    @Test
    public void testPayout_TaxOnWinnerAnalysis_Refund_SingleDraw_OK() throws Exception {
        printMethod();
        DigitalTicket ticket = new DigitalTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("update FD_OPERATION_PARAMETERS set PAYOUT_MODEL=0 where ID='FD-OP-1'");
        this.jdbcTemplate.update("delete from TE_FD_TICKET where id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(22200.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(24400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(2200.0, prize.getTaxAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getReturnAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_REFUND, prize.getPayoutMode());
        assertEquals(0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        assertNull(prize.getNewPrintTicket());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldCreditOperator.add(prize.getActualAmount()).doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(respCtx.getTransactionID());
        expectedTrans.setTotalAmount(prize.getActualAmount());
        expectedTrans.setGameId("FD-1");
        expectedTrans.setTicketSerialNo(ticket.getSerialNo());
        expectedTrans.setOperatorId(respCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setDeviceId(respCtx.getTerminalId());
        expectedTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectedTrans.setType(ctx.getTransType());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        this.assertTransaction(expectedTrans, dbTrans);

        // assert Tickets
        List<DigitalTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                ticket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        assertEquals(BaseTicket.STATUS_PAID, hostTickets.get(0).getStatus());

        // assert Payouts
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(1, payouts.size());
        assertEquals(22200.0, payouts.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(24400.0, payouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals("FD-1", payouts.get(0).getGameId());
        assertEquals("GII-111", payouts.get(0).getGameInstanceId());
        assertEquals(Payout.TYPE_WINNING, payouts.get(0).getType());
        assertEquals(Payout.STATUS_PAID, payouts.get(0).getStatus());
        assertTrue(payouts.get(0).isValid());
    }

    // ----------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------------------------

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
