package com.mpos.lottery.te.gameimpl.magic100.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.Magic100DomainMocker;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import javax.annotation.Resource;

public class Magic100MannualCancelByTicketIntegrationTest extends BaseServletIntegrationTest {
    // dependencies inject by type
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao ticketDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    @Rollback(true)
    @Test
    public void testCancel_OK() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        // 1. make sale first
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancel by ticket first
        Magic100Ticket cancelTicket = (Magic100Ticket) saleRespCtx.getModel();
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getTransactionDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction type of
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTrans.getType());

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getTicketDao().findBySerialNo(Magic100Ticket.class,
                cancelTicket.getSerialNo(), false);
        for (Magic100Ticket dbTicket : dbTickets)
            assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTicket.getTransType());
    }

    /**
     * disable the allow-cancellation of a game
     */
    @Test
    public void testCancel_Game_Disable() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        this.jdbcTemplate.update("update LK_OPERATION_PARAMETERS set ALLOW_CANCELLATION=0");

        // 1. make sale first
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancel by ticket first
        Magic100Ticket cancelTicket = (Magic100Ticket) saleRespCtx.getModel();
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_MANUAL_CANCEL_DISABLED, cancelRespCtx.getResponseCode());
    }

    /**
     * disable the allow-cancellation of a game instance
     */
    @Test
    public void testCancel_GameInstance_Disable() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        this.jdbcTemplate.update("update LK_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=1");

        // 1. make sale first
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancel by ticket first
        Magic100Ticket cancelTicket = (Magic100Ticket) saleRespCtx.getModel();
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_MANUAL_CANCEL_DISABLED, cancelRespCtx.getResponseCode());
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public BaseTicketDao getTicketDao() {
        return ticketDao;
    }

    public void setTicketDao(BaseTicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

}
