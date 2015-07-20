package com.mpos.lottery.te.gameimpl.extraball.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.gameimpl.extraball.ExtraBallDomainMocker;
import com.mpos.lottery.te.gamespec.game.Game;
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
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.util.List;

public class Ticket_Sale_IntegrationTest extends BaseServletIntegrationTest {
    // SPRINT DEPENDENCIES
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;
    private TransactionDao transactionDao;
    private MerchantDao merchantDao;
    private OperatorMerchantDao operatorMerchantDao;

    @Test
    public void testSell_MultiDraw_OK() throws Exception {
        printMethod();
        ExtraBallTicket ticket = ExtraBallDomainMocker.mockTicket();

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
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
        ExtraBallTicket respTicket = (ExtraBallTicket) respCtx.getModel();
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("20120711", respTicket.getLastDrawNo());
        assertNotNull(respTicket.getRawSerialNo());

        // 1st: assert transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(ctx.getTerminalId(), dbTrans.getDeviceId());
        assertEquals(ctx.getBatchNumber(), dbTrans.getBatchNumber());
        assertEquals(ctx.getOperatorId(), dbTrans.getOperatorId());
        assertEquals(ctx.getMerchant().getId(), dbTrans.getMerchantId());
        assertEquals(ctx.getTransType(), dbTrans.getType());
        assertEquals(respCtx.getResponseCode(), dbTrans.getResponseCode());
        assertNotNull(dbTrans.getCreateTime());
        assertEquals(ticket.getGameInstance().getGame().getId(), dbTrans.getGameId());
        assertEquals(ticket.getTotalAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
        assertEquals(respTicket.getSerialNo(), dbTrans.getTicketSerialNo());

        // 2nd: assert ticket
        List<ExtraBallTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                respTicket.getSerialNo(), true);
        assertEquals(3, dbTickets.size());
        // assert ticket in sold game instance
        ExtraBallTicket boughtTicket = dbTickets.get(0);
        assertEquals(ctx.getTerminalId(), boughtTicket.getDevId());
        assertEquals(ctx.getBatchNumber(), boughtTicket.getBatchNo());
        assertEquals(ctx.getOperatorId(), boughtTicket.getOperatorId());
        assertEquals(ctx.getMerchant().getId(), boughtTicket.getMerchantId());
        assertEquals(ticket.getTotalAmount().divide(new BigDecimal(ticket.getMultipleDraws())).doubleValue(),
                boughtTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(respCtx.getTransactionID(), boughtTicket.getTransaction().getId());
        assertEquals(ticket.getMultipleDraws(), boughtTicket.getMultipleDraws());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, boughtTicket.getTicketType());
        assertEquals(BaseTicket.STATUS_ACCEPTED, boughtTicket.getStatus());
        assertTrue(boughtTicket.isCountInPool());
        assertFalse(boughtTicket.isWinning());
        assertEquals("GI-EB-1", boughtTicket.getGameInstance().getId());
        assertFalse(boughtTicket.isPayoutBlocked());
        assertNotNull(boughtTicket.getCreateTime());
        // assert the other 2 tickets
        for (int i = 1; i < dbTickets.size(); i++) {
            boughtTicket = dbTickets.get(i);
            assertEquals(ctx.getTerminalId(), boughtTicket.getDevId());
            assertEquals(ctx.getBatchNumber(), boughtTicket.getBatchNo());
            assertEquals(ctx.getOperatorId(), boughtTicket.getOperatorId());
            assertEquals(ctx.getMerchant().getId(), boughtTicket.getMerchantId());
            assertEquals(ticket.getTotalAmount().divide(new BigDecimal(ticket.getMultipleDraws())).doubleValue(),
                    boughtTicket.getTotalAmount().doubleValue(), 0);
            assertEquals(respCtx.getTransactionID(), boughtTicket.getTransaction().getId());
            assertEquals(0, boughtTicket.getMultipleDraws());
            assertEquals(BaseTicket.TICKET_TYPE_NORMAL, boughtTicket.getTicketType());
            assertEquals(BaseTicket.STATUS_ACCEPTED, boughtTicket.getStatus());
            assertTrue(boughtTicket.isCountInPool());
            assertFalse(boughtTicket.isWinning());
            if (i == 1)
                assertEquals("GI-EB-2", boughtTicket.getGameInstance().getId());
            if (i == 2)
                assertEquals("GI-EB-3", boughtTicket.getGameInstance().getId());
            assertFalse(boughtTicket.isPayoutBlocked());
            assertNotNull(boughtTicket.getCreateTime());
        }

        // 3rd: assert entry
        List<ExtraBallEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(ExtraBallEntry.class,
                respTicket.getSerialNo(), true);
        assertEquals(ticket.getEntries().size(), dbEntries.size());
        for (int i = 0; i < dbEntries.size(); i++) {
            ExtraBallEntry entry = (ExtraBallEntry) ticket.getEntries().get(i);
            ExtraBallEntry dbEntry = dbEntries.get(i);
            assertEquals(entry.getBetOption(), dbEntry.getBetOption());
            assertEquals(i + 1 + "", dbEntry.getEntryNo());
            assertEquals(entry.getInputChannel(), dbEntry.getInputChannel());
            assertEquals(entry.getSelectNumber(), dbEntry.getSelectNumber());
            assertEquals(1, dbEntry.getTotalBets());
            assertEquals(entry.getEntryAmount().doubleValue(), dbEntry.getEntryAmount().doubleValue(), 0);
        }

        // 5th: assert incentive count
        // base amount of LG game is $100.0
        OperatorMerchant op = this.getOperatorMerchantDao().findByOperatorAndMerchant(ctx.getOperatorId(),
                ctx.getMerchant().getId());
        assertEquals(startIncentiveCounter + ticket.getTotalAmount().intValue() / 100, op.getIncentiveCount());
    }

    /**
     * Buy a stopped game instance.
     */
    @Test
    public void testSell_MultiDraw_StoppedGameInstance() throws Exception {
        printMethod();
        ExtraBallTicket ticket = ExtraBallDomainMocker.mockTicket();
        ticket.getGameInstance().setNumber("20120708");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(304, respCtx.getResponseCode());
    }

    /**
     * Entry amount isn't in allowed range
     */
    @Test
    public void testSell_MultiDraw_UnallowedAmount() throws Exception {
        printMethod();
        ExtraBallTicket ticket = ExtraBallDomainMocker.mockTicket();
        ((ExtraBallEntry) ticket.getEntries().get(0)).setEntryAmount(new BigDecimal(9000000.0));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(307, respCtx.getResponseCode());
    }

    /**
     * The multiple draw isn't in allowed range.
     */
    @Test
    public void testSell_UnallowedMultiDraw() throws Exception {
        printMethod();
        ExtraBallTicket ticket = ExtraBallDomainMocker.mockTicket();
        ticket.setMultipleDraws(22);

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(280, respCtx.getResponseCode());
    }

    /**
     * The format of selected number is illegal
     */
    @Test
    public void testSell_IllegalSelectedNumber() throws Exception {
        printMethod();
        ExtraBallTicket ticket = ExtraBallDomainMocker.mockTicket();
        ((ExtraBallEntry) ticket.getEntries().get(0)).setSelectNumber("88");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(305, respCtx.getResponseCode());
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------------

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(@Qualifier("baseTicketDao") BaseTicketDao baseTicketDao) {
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

}
