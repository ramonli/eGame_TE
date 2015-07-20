package com.mpos.lottery.te.gameimpl.digital.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.DigitalDomainMocker;
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
        DigitalTicket ticket = DigitalDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        DigitalTicket respTicket = (DigitalTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel by ticket
        DigitalTicket cancelTicket = new DigitalTicket();
        cancelTicket.setRawSerialNo(respTicket.getRawSerialNo());
        Context reqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        reqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(reqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(cancelTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                cancelTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), hostTickets.get(0).getTransType());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(1).getStatus());
        assertFalse(hostTickets.get(1).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), hostTickets.get(1).getTransType());
    }

    @Test
    public void testCancelByTicket_CancelDelined() throws Exception {
        printMethod();
        DigitalTicket ticket = DigitalDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        DigitalTicket respTicket = (DigitalTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();
        this.triggerCancelDecline();

        // 2. cancel by ticket
        DigitalTicket cancelTicket = new DigitalTicket();
        cancelTicket.setRawSerialNo(respTicket.getRawSerialNo());
        Context reqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        reqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(reqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_FAILTO_CANCEL, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(cancelTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                cancelTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, hostTickets.get(0).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), hostTickets.get(0).getTransType());
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, hostTickets.get(1).getStatus());
        assertTrue(hostTickets.get(1).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), hostTickets.get(1).getTransType());
    }

    @Test
    public void testCancelByTicket_Manually_OK() throws Exception {
        printMethod();
        DigitalTicket ticket = DigitalDomainMocker.mockTicket();

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        DigitalTicket respTicket = (DigitalTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel by ticket
        DigitalTicket cancelTicket = new DigitalTicket();
        cancelTicket.setRawSerialNo(respTicket.getRawSerialNo());
        cancelTicket.setManualCancel(true);
        Context reqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        reqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(reqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTrans.getType());

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                cancelTicket.getSerialNo(), false);
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), hostTickets.get(0).getTransType());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), hostTickets.get(1).getTransType());
    }

    @Test
    public void testCancelByTrans_OK() throws Exception {
        printMethod();
        DigitalTicket ticket = DigitalDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        DigitalTicket respTicket = (DigitalTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel by transaction
        Transaction trans = new Transaction();
        trans.setDeviceId(saleReqCtx.getTerminalId());
        trans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context reqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context respCtx = doPost(this.mockRequest(reqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), hostTickets.get(0).getTransType());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(1).getStatus());
        assertFalse(hostTickets.get(1).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), hostTickets.get(1).getTransType());
    }

    @Test
    public void testCancelByTrans_CancelDelined() throws Exception {
        printMethod();
        DigitalTicket ticket = DigitalDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        DigitalTicket respTicket = (DigitalTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();
        this.triggerCancelDecline();

        // 2. cancel by transaction
        Transaction trans = new Transaction();
        trans.setDeviceId(saleReqCtx.getTerminalId());
        trans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context reqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context respCtx = doPost(this.mockRequest(reqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_FAILTO_CANCEL, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, hostTickets.get(0).getStatus());
        assertTrue(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), hostTickets.get(0).getTransType());
        assertEquals(BaseTicket.STATUS_CANCEL_DECLINED, hostTickets.get(1).getStatus());
        assertTrue(hostTickets.get(1).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), hostTickets.get(1).getTransType());
    }

    private void triggerCancelDecline() {
        this.jdbcTemplate
                .update("update fd_game_instance set game_freezing_time=sysdate-30/(24*60) where GAME_INSTANCE_ID='GII-112'");
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
