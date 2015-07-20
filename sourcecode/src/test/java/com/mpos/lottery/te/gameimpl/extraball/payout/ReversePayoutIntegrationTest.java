package com.mpos.lottery.te.gameimpl.extraball.payout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class ReversePayoutIntegrationTest extends BaseServletIntegrationTest {
    // SPRINT DEPENDENCIES
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;
    private PayoutDao payoutDao;
    private NewPrintTicketDao newPrintTicketDao;
    private TransactionDao transactionDao;
    private MerchantDao merchantDao;

    @Test
    public void testPayout_NewPrint_OK() throws Exception {
        printMethod();

        // issue payout first
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");
        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();
        doPost(this.mockRequest(ctx));
        // issue reversal request
        Transaction trans = new Transaction();
        trans.setDeviceId(ctx.getTerminalId());
        trans.setTraceMessageId(ctx.getTraceMessageId());
        ctx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), trans);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        this.assertOutput(ctx, respCtx, ticket, startCreditLevel, BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET);
    }

    @Test
    public void testPayout_Refund_OK() throws Exception {
        printMethod();

        this.jdbcTemplate.execute("update eb_operation_parameters set payout_model="
                + BaseOperationParameter.PAYOUTMODE_REFUND);

        // issue payout first
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");
        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();
        doPost(this.mockRequest(ctx));
        // issue reversal request
        Transaction trans = new Transaction();
        trans.setDeviceId(ctx.getTerminalId());
        trans.setTraceMessageId(ctx.getTraceMessageId());
        ctx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), trans);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        this.assertOutput(ctx, respCtx, ticket, startCreditLevel, BaseOperationParameter.PAYOUTMODE_REFUND);
    }

    // ----------------------------------------------------------
    // HELP METHODS
    // ----------------------------------------------------------

    protected void assertOutput(Context reqCtx, Context respCtx, ExtraBallTicket ticket, BigDecimal startCreditLevel,
            int payoutMode) throws Exception {
        // -- assert ticket
        List<ExtraBallTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                ticket.getSerialNo(), true);
        assertEquals(3, dbTickets.size());
        // assert ticket in sold game instance
        for (int i = 0; i < dbTickets.size(); i++) {
            ExtraBallTicket boughtTicket = dbTickets.get(i);
            assertEquals(BaseTicket.STATUS_ACCEPTED, boughtTicket.getStatus());
            assertTrue(boughtTicket.isCountInPool());
        }

        if (payoutMode == BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET) {
            // -- assert new print log
            NewPrintTicket newPrintTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
            assertEquals(NewPrintTicket.STATUS_REVERSED, newPrintTicket.getStatus());
            // assert new generated ticket
            List<ExtraBallTicket> dbGeneratedTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                    newPrintTicket.getNewTicketSerialNo(), true);
            assertEquals(2, dbGeneratedTickets.size());
            for (int i = 1; i < dbGeneratedTickets.size(); i++) {
                ExtraBallTicket newGeneratedTicket = dbGeneratedTickets.get(i);
                assertEquals(BaseTicket.STATUS_INVALID, newGeneratedTicket.getStatus());
                assertEquals(false, newGeneratedTicket.isCountInPool());
            }
        }

        // -- assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(ticket.getSerialNo());
        for (int i = 0; i < dbPayouts.size(); i++) {
            Payout dbPayout = dbPayouts.get(i);
            assertEquals(Payout.STATUS_REVERSED, dbPayout.getStatus());
        }

        // -- assert credit level
        assertEquals(startCreditLevel.doubleValue(),
                this.getMerchantDao().findById(Merchant.class, reqCtx.getMerchant().getId()).getSaleCreditLevel()
                        .doubleValue(), 0);
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------------

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
