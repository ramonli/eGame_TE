package com.mpos.lottery.te.gameimpl.toto.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.toto.TotoDomainMocker;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class SaleCancellationIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;

    @Test
    public void testCancelByTicket_OK() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();
        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        ticket.setRawSerialNo(respTicket.getRawSerialNo());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelCtx.setGameTypeId(Game.TYPE_TOTO + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert cancel transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(cancelCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectTrans.setType(cancelCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(ToToTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), hostTickets.get(0).getTransType());
    }

    @Test
    public void testCancelByTicket_Manually_OK() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));

        // 1. make sale
        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        ticket.setRawSerialNo(respTicket.getRawSerialNo());
        ticket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelCtx.setGameTypeId(Game.TYPE_TOTO + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTrans.getType());

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(ToToTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), hostTickets.get(0).getTransType());
    }

    @Test
    public void testCancelByTransaction_OK() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(ctx.getTerminalId());
        trans.setTraceMessageId(ctx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(cancelCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectTrans.setType(cancelCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(ToToTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), hostTickets.get(0).getTransType());
    }

    @Test
    public void testCancelByTicket_CancelDecline() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();
        this.triggerCancelDecline();

        // 2. make cancellation
        ticket.setRawSerialNo(respTicket.getRawSerialNo());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelCtx.setGameTypeId(Game.TYPE_TOTO + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_FAILTO_CANCEL, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(cancelCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectTrans.setType(cancelCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(ToToTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, hostTickets.get(0).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), hostTickets.get(0).getTransType());
    }

    @Test
    public void testCancelByTransaction_CancelDecline() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();
        this.triggerCancelDecline();

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(ctx.getTerminalId());
        trans.setTraceMessageId(ctx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_FAILTO_CANCEL, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(cancelCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectTrans.setType(cancelCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(ToToTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, hostTickets.get(0).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), hostTickets.get(0).getTransType());
    }

    private void triggerCancelDecline() {
        this.jdbcTemplate
                .update("update TOTO_GAME_INSTANCE set game_freezing_time=sysdate-30/(24*60) where GAME_INSTANCE_ID='GII-112'");
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }
}
