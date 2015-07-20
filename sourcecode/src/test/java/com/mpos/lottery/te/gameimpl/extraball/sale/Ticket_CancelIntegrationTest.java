package com.mpos.lottery.te.gameimpl.extraball.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorMerchantDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.OperatorMerchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class Ticket_CancelIntegrationTest extends BaseServletIntegrationTest {
    // SPRINT DEPENDENCIES
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;
    private TransactionDao transactionDao;
    private MerchantDao merchantDao;
    private OperatorMerchantDao operatorMerchantDao;
    private BaseGameInstanceDao baseGameInstanceDao;

    @Test
    public void testCancelByTicket_OK() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-1");

        Context ctx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();
        int startIncentiveCounter = this.getOperatorMerchantDao()
                .findByOperatorAndMerchant(ctx.getOperatorId(), ctx.getMerchant().getId()).getIncentiveCount();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        this.assertCancellation(ticket.getSerialNo(), ctx, respCtx, startCreditLevel, startIncentiveCounter, false);
    }

    @Test
    public void testCancelByTicket_Decline() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-1");

        Context ctx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        this.jdbcTemplate.execute("update eb_game_instance set stop_selling_time=sysdate-1/24,"
                + "game_freezing_time=sysdate-30/(24*60) where ID='GI-EB-1'");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();
        int startIncentiveCounter = this.getOperatorMerchantDao()
                .findByOperatorAndMerchant(ctx.getOperatorId(), ctx.getMerchant().getId()).getIncentiveCount();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(333, respCtx.getResponseCode());
        this.assertCancellation(ticket.getSerialNo(), ctx, respCtx, startCreditLevel, startIncentiveCounter, true);
    }

    @Test
    public void testCancelByTicket_NonExist() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-NonExist");

        Context ctx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();
        int startIncentiveCounter = this.getOperatorMerchantDao()
                .findByOperatorAndMerchant(ctx.getOperatorId(), ctx.getMerchant().getId()).getIncentiveCount();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(315, respCtx.getResponseCode());
    }

    @Test
    public void testCancelByTransaction_OK() throws Exception {
        printMethod();
        Transaction trans = new Transaction();
        trans.setTraceMessageId("TMI-EB-1");
        trans.setDeviceId(111);

        Context ctx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();
        int startIncentiveCounter = this.getOperatorMerchantDao()
                .findByOperatorAndMerchant(ctx.getOperatorId(), ctx.getMerchant().getId()).getIncentiveCount();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        this.assertCancellation(BaseTicket.encryptSerialNo("SN-EB-1"), ctx, respCtx, startCreditLevel,
                startIncentiveCounter, false);
    }

    @Test
    public void testCancelByTransaction_Decline_OK() throws Exception {
        printMethod();
        Transaction trans = new Transaction();
        trans.setTraceMessageId("TMI-EB-1");
        trans.setDeviceId(111);

        Context ctx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");

        this.jdbcTemplate.execute("update eb_game_instance set stop_selling_time=sysdate-1/24,"
                + "game_freezing_time=sysdate-30/(24*60) where ID='GI-EB-1'");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();
        int startIncentiveCounter = this.getOperatorMerchantDao()
                .findByOperatorAndMerchant(ctx.getOperatorId(), ctx.getMerchant().getId()).getIncentiveCount();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(333, respCtx.getResponseCode());
        this.assertCancellation(BaseTicket.encryptSerialNo("SN-EB-1"), ctx, respCtx, startCreditLevel,
                startIncentiveCounter, true);
    }

    @Test
    public void testCancelByTransaction_NonExist() throws Exception {
        printMethod();
        Transaction trans = new Transaction();
        trans.setTraceMessageId("TMI-EB-NonExist");
        trans.setDeviceId(111);

        Context ctx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(328, respCtx.getResponseCode());
    }

    // ----------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------

    protected void assertCancellation(String serialNo, Context reqCtx, Context respCtx, BigDecimal startCreditLevel,
            int startIncentiveCounter, boolean isCancelDecline) throws Exception {
        // 1st: assert ticket
        List<ExtraBallTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class, serialNo, true);
        assertEquals(3, dbTickets.size());
        // assert ticket in sold game instance...Cancel and sale must be issued
        // by a single same client.
        ExtraBallTicket boughtTicket = dbTickets.get(0);
        for (int i = 0; i < dbTickets.size(); i++) {
            ExtraBallTicket dbTicket = dbTickets.get(i);
            assertEquals(reqCtx.getBatchNumber(), dbTicket.getBatchNo());
            assertEquals(respCtx.getTransactionID(), dbTicket.getTransaction().getId());
            assertEquals(isCancelDecline ? BaseTicket.STATUS_CANCEL_DECLINED : BaseTicket.STATUS_CANCELED,
                    dbTicket.getStatus());
            assertEquals(isCancelDecline ? true : false, dbTicket.isCountInPool());
        }

        // 2nd: assert transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(reqCtx.getTerminalId(), dbTrans.getDeviceId());
        assertEquals(reqCtx.getBatchNumber(), dbTrans.getBatchNumber());
        assertEquals(reqCtx.getOperatorId(), dbTrans.getOperatorId());
        assertEquals(reqCtx.getMerchant().getId(), dbTrans.getMerchantId());
        assertEquals(reqCtx.getTransType(), dbTrans.getType());
        assertEquals(respCtx.getResponseCode(), dbTrans.getResponseCode());
        assertNotNull(dbTrans.getCreateTime());
        ExtraBallGameInstance gameInstance = this.getBaseGameInstanceDao().findById(ExtraBallGameInstance.class,
                boughtTicket.getGameInstance().getId());
        assertEquals(gameInstance.getGame().getId(), dbTrans.getGameId());
        BigDecimal totalAmount = boughtTicket.getTotalAmount().multiply(new BigDecimal(dbTickets.size()));
        assertEquals(totalAmount.doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
        assertEquals(serialNo, dbTrans.getTicketSerialNo());

        // 3rd: assert credit level
        // OperatorCommission operatorCommission =
        // this.getOperatorCommissionDao()
        // .getByOperatorAndMerchantAndGame(reqCtx.getOperatorId(),
        // reqCtx.getMerchantId(),
        // gameInstance.getGame().getId());
        // BigDecimal newCreditLevel =
        // startCreditLevel.add(totalAmount).subtract(
        // totalAmount.multiply(operatorCommission.getSaleRate()));
        // assertEquals(newCreditLevel.doubleValue(),
        // this.getMerchantDao().getById(reqCtx.getMerchantId())
        // .getCreditLevel().doubleValue(), 0);

        // 5th: assert incentive count
        // base amount of LG game is $100.0
        OperatorMerchant op = this.getOperatorMerchantDao().findByOperatorAndMerchant(reqCtx.getOperatorId(),
                reqCtx.getMerchant().getId());
        assertEquals(startIncentiveCounter - totalAmount.intValue() / 100, op.getIncentiveCount());
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

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public OperatorMerchantDao getOperatorMerchantDao() {
        return operatorMerchantDao;
    }

    public void setOperatorMerchantDao(OperatorMerchantDao operatorMerchantDao) {
        this.operatorMerchantDao = operatorMerchantDao;
    }

    public BaseGameInstanceDao getBaseGameInstanceDao() {
        return baseGameInstanceDao;
    }

    public void setBaseGameInstanceDao(BaseGameInstanceDao baseGameInstanceDao) {
        this.baseGameInstanceDao = baseGameInstanceDao;
    }

}
