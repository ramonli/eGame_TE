package com.mpos.lottery.te.gameimpl.union.prize;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.union.sale.UnionTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import javax.annotation.Resource;

public class UnionPayoutReversalIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseTicketDao")
    private BaseTicketDao ticketDao;
    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;
    @Resource(name = "payoutDetailDao")
    private PayoutDetailDao payoutDetailDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao entryDao;
    @Resource(name = "newPrintTicketDao")
    private NewPrintTicketDao newPrintTicketDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    @Rollback(true)
    @Test
    public void testPayout_PrintNewTicket_Reversal() throws Exception {
        this.printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        // 1. make payout
        Context payoutReqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutReqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(payoutReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancel by transaction
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutReqCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutReqCtx.getTraceMessageId());
        Context reversalReqCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), payoutTrans);
        Context reversalRespCtx = this.doPost(this.mockRequest(reversalReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, reversalRespCtx.getResponseCode());

        // assert payout transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert ticket
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(1).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(3, dbPayouts.size());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(0).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(1).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(2).getStatus());

        // assert new printed ticket
        NewPrintTicket dbNewPrintTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
        assertEquals(NewPrintTicket.STATUS_REVERSED, dbNewPrintTicket.getStatus());
    }

    @Rollback(true)
    @Test
    public void testPayout_PrintNewTicket_Reversal_NoAllocatedOperator() throws Exception {
        this.printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        // 1. make payout
        Context payoutReqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutReqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(payoutReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        this.jdbcTemplate.update("delete from OPERATOR_MERCHANT where OPERATOR_ID='OPERATOR-111'");

        // 2. make cancel by transaction
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutReqCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutReqCtx.getTraceMessageId());
        Context reversalReqCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), payoutTrans);
        Context reversalRespCtx = this.doPost(this.mockRequest(reversalReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, reversalRespCtx.getResponseCode());

        // assert payout transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert ticket
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(1).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(3, dbPayouts.size());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(0).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(1).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(2).getStatus());

        // assert new printed ticket
        NewPrintTicket dbNewPrintTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
        assertEquals(NewPrintTicket.STATUS_REVERSED, dbNewPrintTicket.getStatus());
    }

    @Test
    public void testPayout_PrintNewTicket_DulplicatedReversal() throws Exception {
        this.printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        // 1. make payout
        Context payoutReqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutReqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(payoutReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancel by transaction
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutReqCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutReqCtx.getTraceMessageId());
        Context reversalReqCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), payoutTrans);
        Context reversalRespCtx = this.doPost(this.mockRequest(reversalReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 3. make cancel by transaction again
        reversalReqCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), payoutTrans);
        reversalRespCtx = this.doPost(this.mockRequest(reversalReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_CANCELLED_TRANS, reversalRespCtx.getResponseCode());

        // assert payout transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert ticket
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(1).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(3, dbPayouts.size());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(0).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(1).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(2).getStatus());

        // assert new printed ticket
        NewPrintTicket dbNewPrintTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
        assertEquals(NewPrintTicket.STATUS_REVERSED, dbNewPrintTicket.getStatus());
    }

    @Test
    public void testPayout_Return_Reversal() throws Exception {
        this.printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);

        // 1. make payout
        Context payoutReqCtx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        payoutReqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(payoutReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancel by transaction
        Transaction payoutTrans = new Transaction();
        payoutTrans.setDeviceId(payoutReqCtx.getTerminalId());
        payoutTrans.setTraceMessageId(payoutReqCtx.getTraceMessageId());
        Context reversalReqCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), payoutTrans);
        Context reversalRespCtx = this.doPost(this.mockRequest(reversalReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, reversalRespCtx.getResponseCode());

        // assert payout transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert ticket
        List<UnionTicket> dbTickets = this.getTicketDao()
                .findBySerialNo(UnionTicket.class, ticket.getSerialNo(), false);
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(0).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(1).getStatus());
        assertEquals(BaseTicket.STATUS_ACCEPTED, dbTickets.get(2).getStatus());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        assertEquals(4, dbPayouts.size());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(0).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(1).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(2).getStatus());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(3).getStatus());
    }

    private void switchPayoutMode(int payoutMode) {
        // the default payout mode: print new ticket
        this.jdbcTemplate.update("update UN_OPERATION_PARAMETERS set PAYOUT_MODEL=" + payoutMode);
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

    public BaseTicketDao getTicketDao() {
        return ticketDao;
    }

    public void setTicketDao(BaseTicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    public BaseEntryDao getEntryDao() {
        return entryDao;
    }

    public void setEntryDao(BaseEntryDao entryDao) {
        this.entryDao = entryDao;
    }

    public NewPrintTicketDao getNewPrintTicketDao() {
        return newPrintTicketDao;
    }

    public void setNewPrintTicketDao(NewPrintTicketDao newPrintTicketDao) {
        this.newPrintTicketDao = newPrintTicketDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

}
