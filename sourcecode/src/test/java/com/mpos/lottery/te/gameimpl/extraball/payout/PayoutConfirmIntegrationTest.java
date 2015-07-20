package com.mpos.lottery.te.gameimpl.extraball.payout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.List;

public class PayoutConfirmIntegrationTest extends BaseServletIntegrationTest {
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
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
        // issue payout first
        Context respCtx = doPost(this.mockRequest(ctx));
        // issue payout confirm request
        ctx.setTraceMessageId(System.currentTimeMillis() + "");
        ctx.setTransType(TransactionType.CONFIRM_PAYOUT.getRequestType());
        respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());

        this.assertOutput(ctx, respCtx, ticket, BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET);
    }

    @Test
    public void testPayout_Refund_OK() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");

        this.jdbcTemplate.execute("update eb_operation_parameters set payout_model="
                + BaseOperationParameter.PAYOUTMODE_REFUND);

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
        // issue payout first
        Context respCtx = doPost(this.mockRequest(ctx));
        // issue payout confirm request
        ctx.setTraceMessageId(System.currentTimeMillis() + "");
        ctx.setTransType(TransactionType.CONFIRM_PAYOUT.getRequestType());
        respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());

        this.assertOutput(ctx, respCtx, ticket, BaseOperationParameter.PAYOUTMODE_REFUND);
    }

    // ----------------------------------------------------------
    // HELP METHODS
    // ----------------------------------------------------------

    protected void assertOutput(Context reqCtx, Context respCtx, ExtraBallTicket ticket, int payoutMode)
            throws Exception {
        // -- assert transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(reqCtx.getTerminalId(), dbTrans.getDeviceId());
        assertEquals(reqCtx.getBatchNumber(), dbTrans.getBatchNumber());
        assertEquals(reqCtx.getOperatorId(), dbTrans.getOperatorId());
        assertEquals(reqCtx.getMerchant().getId(), dbTrans.getMerchantId());
        assertEquals(reqCtx.getTransType(), dbTrans.getType());
        assertEquals(respCtx.getResponseCode(), dbTrans.getResponseCode());
        assertNotNull(dbTrans.getCreateTime());
        assertEquals("GAME-EB", dbTrans.getGameId());
        // assertEquals(prize.getPrizeAmount().doubleValue(),
        // dbTrans.getTotalAmount().doubleValue(),
        // 0);
        assertEquals(ticket.getSerialNo(), dbTrans.getTicketSerialNo());

        // -- assert ticket
        List<ExtraBallTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                ticket.getSerialNo(), true);
        assertEquals(3, dbTickets.size());
        // assert ticket in sold game instance
        ExtraBallTicket boughtTicket = dbTickets.get(0);
        assertEquals(BaseTicket.STATUS_PAID, boughtTicket.getStatus());
        assertTrue(boughtTicket.isCountInPool());
        // assert the other 2 tickets
        for (int i = 1; i < dbTickets.size(); i++) {
            boughtTicket = dbTickets.get(i);
            if (payoutMode == BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET) {
                assertEquals(BaseTicket.STATUS_INVALID, boughtTicket.getStatus());
                assertEquals(false, boughtTicket.isCountInPool());
            } else {
                assertEquals(BaseTicket.STATUS_RETURNED, boughtTicket.getStatus());
                assertEquals(false, boughtTicket.isCountInPool());
            }
        }

        if (payoutMode == BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET) {
            // -- assert new print log
            NewPrintTicket newPrintTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
            assertEquals(NewPrintTicket.STATUS_CONFIRMED, newPrintTicket.getStatus());

            // assert new generated ticket
            List<ExtraBallTicket> dbGeneratedTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                    newPrintTicket.getNewTicketSerialNo(), true);
            assertEquals(2, dbGeneratedTickets.size());
            for (int i = 1; i < dbGeneratedTickets.size(); i++) {
                ExtraBallTicket newGeneratedTicket = dbGeneratedTickets.get(i);
                assertEquals(BaseTicket.STATUS_ACCEPTED, newGeneratedTicket.getStatus());
                assertTrue(newGeneratedTicket.isCountInPool());
            }
        }
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
