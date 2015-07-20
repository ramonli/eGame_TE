package com.mpos.lottery.te.gameimpl.union.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.union.UnionDomainMocker;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class UnionMannualCancellationIntegrationTest extends BaseServletIntegrationTest {
    // dependencies inject by type
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @PersistenceContext
    private EntityManager entityManager;

    @Rollback(true)
    @Test
    public void testCancel_OK() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // 2. cancel manuallys
        ticket.setRawSerialNo(((UnionTicket) saleRespCtx.getModel()).getRawSerialNo());
        ticket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // assert response
        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getTransactionDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction type of
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTrans.getType());

        // assert ticket
        List<UnionTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(UnionTicket.class, ticket.getSerialNo(),
                false);
        for (UnionTicket dbTicket : dbTickets)
            assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTicket.getTransType());
    }

    @Rollback(true)
    @Test
    public void testCancel_RemovedOperator() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // below SQL won't update persistence context, as persistence context
        // has no idea which entity should be updated.
        this.jdbcTemplate.update("update operator set status=" + Operator.STATUS_INACTIVE);

        // 2. cancel manuallys
        ticket.setRawSerialNo(((UnionTicket) saleRespCtx.getModel()).getRawSerialNo());
        ticket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // assert response
        assertEquals(SystemException.CODE_OPERATOR_INACTIVE, cancelRespCtx.getResponseCode());
    }

    /**
     * disable the allow-cancellation of a game
     */
    @Test
    public void testCancel_Game_Disable() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update UN_OPERATION_PARAMETERS set ALLOW_CANCELLATION=0");

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));

        // 2. cancel manuallys
        ticket.setRawSerialNo(((UnionTicket) saleRespCtx.getModel()).getRawSerialNo());
        ticket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // assert response
        assertEquals(SystemException.CODE_MANUAL_CANCEL_DISABLED, cancelRespCtx.getResponseCode());
    }

    /**
     * disable the allow-cancellation of a game instance
     */
    @Test
    public void testCancel_GameInstance_Disable() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update UN_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=1");

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));

        // 2. cancel manuallys
        ticket.setRawSerialNo(((UnionTicket) saleRespCtx.getModel()).getRawSerialNo());
        ticket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        cancelCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // assert response
        assertEquals(SystemException.CODE_MANUAL_CANCEL_DISABLED, cancelRespCtx.getResponseCode());
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

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
