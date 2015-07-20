package com.mpos.lottery.te.gameimpl.raffle.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.raffle.RaffleDomainMocker;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import javax.annotation.Resource;

public class RaffleSaleCancellationIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;

    @Rollback(true)
    @Test
    public void testCancelByTicket_OK() throws Exception {
        this.printMethod();
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        // 1. make sale fist
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();

        // fluch entity state to underlying database.
        this.entityManager.flush();
        // will detach all entities from EM, then any operations on entity will
        // be built from DB again.
        this.entityManager.clear();

        // 2. make cancelByTicket request
        ticket.setRawSerialNo(respTicket.getRawSerialNo());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert outputs
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(ticket.getSerialNo());
        expectTrans.setOperatorId(cancelReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelReqCtx.getTraceMessageId());
        expectTrans.setType(cancelReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<RaffleTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(1, dbTickets.size());

        RaffleTicket dbTicket1 = dbTickets.get(0);
        assertEquals(BaseTicket.STATUS_CANCELED, dbTicket1.getStatus());
        assertEquals(ticket.getTotalAmount().doubleValue(), dbTicket1.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getMultipleDraws(), dbTicket1.getMultipleDraws());
        assertEquals(ticket.getUser().getMobile(), dbTicket1.getMobile());
        assertNull(dbTicket1.getCreditCardSN());
        assertFalse(dbTicket1.isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), dbTicket1.getTransType());
    }

    @Test
    public void testCancelByTicket_Declined() throws Exception {
        this.printMethod();
        // 1. make sale fist
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();

        // fluch entity state to underlying database.
        this.entityManager.flush();
        // will detach all entities from EM, then any operations on entity will
        // be built from DB again.
        this.entityManager.clear();
        this.triggerCancelDecline();

        // 2. make cancelByTicket request
        ticket.setRawSerialNo(respTicket.getRawSerialNo());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert outputs
        assertEquals(SystemException.CODE_FAILTO_CANCEL, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(ticket.getSerialNo());
        expectTrans.setOperatorId(cancelReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelReqCtx.getTraceMessageId());
        expectTrans.setType(cancelReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<RaffleTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(1, dbTickets.size());

        RaffleTicket dbTicket1 = dbTickets.get(0);
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, dbTicket1.getStatus());
        assertTrue(dbTicket1.isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), dbTicket1.getTransType());
    }

    @Test
    public void testCancelByTicket_Manually_OK() throws Exception {
        this.printMethod();
        // 1. make sale fist
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();

        // fluch entity state to underlying database.
        this.entityManager.flush();
        // will detach all entities from EM, then any operations on entity will
        // be built from DB again.
        this.entityManager.clear();

        // 2. make cancelByTicket request
        ticket.setRawSerialNo(respTicket.getRawSerialNo());
        ticket.setManualCancel(true);
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert outputs
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTrans.getType());

        // assert ticket
        List<RaffleTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTickets.get(0).getTransType());
    }

    @Rollback(true)
    @Test
    public void testCancelByTransaction_OK() throws Exception {
        this.printMethod();
        // 1. make sale fist
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();
        ticket.setRawSerialNo(respTicket.getRawSerialNo());

        // fluch entity state to underlying database.
        this.entityManager.flush();
        // will detach all entities from EM, then any operations on entity will
        // be built from DB again.
        this.entityManager.clear();

        // 2. make cancelByTicket request
        Transaction transaction = new Transaction();
        transaction.setDeviceId(saleReqCtx.getTerminalId());
        transaction.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(),
                transaction);
        cancelReqCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert outputs
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(ticket.getSerialNo());
        expectTrans.setOperatorId(cancelReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelReqCtx.getTraceMessageId());
        expectTrans.setType(cancelReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<RaffleTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(1, dbTickets.size());

        RaffleTicket dbTicket1 = dbTickets.get(0);
        assertEquals(BaseTicket.STATUS_CANCELED, dbTicket1.getStatus());
        assertFalse(dbTicket1.isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), dbTicket1.getTransType());
    }

    @Test
    public void testCancelByTransaction_Declined() throws Exception {
        this.printMethod();
        // 1. make sale fist
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();
        ticket.setRawSerialNo(respTicket.getRawSerialNo());

        // fluch entity state to underlying database.
        this.entityManager.flush();
        // will detach all entities from EM, then any operations on entity will
        // be built from DB again.
        this.entityManager.clear();
        this.triggerCancelDecline();

        // 2. make cancelByTicket request
        Transaction transaction = new Transaction();
        transaction.setDeviceId(saleReqCtx.getTerminalId());
        transaction.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(),
                transaction);
        cancelReqCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert outputs
        assertEquals(SystemException.CODE_FAILTO_CANCEL, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(ticket.getSerialNo());
        expectTrans.setOperatorId(cancelReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelReqCtx.getTraceMessageId());
        expectTrans.setType(cancelReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<RaffleTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class, ticket.getSerialNo(),
                false);
        assertEquals(1, dbTickets.size());

        RaffleTicket dbTicket1 = dbTickets.get(0);
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, dbTicket1.getStatus());
        assertTrue(dbTicket1.isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), dbTicket1.getTransType());
    }

    private void triggerCancelDecline() {
        this.jdbcTemplate
                .update("update RA_GAME_INSTANCE set game_freezing_time=sysdate-30/(24*60) where ID='GII-112'");
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCY INJECTION
    // ----------------------------------------------------------

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
}
