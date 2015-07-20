package com.mpos.lottery.te.gameimpl.extraball.payout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallEntry;
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
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.util.List;

public class PayoutIntegrationTest extends BaseServletIntegrationTest {
    // SPRINT DEPENDENCIES
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;
    private PayoutDao payoutDao;
    private NewPrintTicketDao newPrintTicketDao;
    private TransactionDao transactionDao;
    private MerchantDao merchantDao;

    @Test
    public void testPayout_NewPrint_TaxWhenDraw_OK() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());

        Prize expectedPrize = new Prize();
        expectedPrize.setPrizeAmount(new BigDecimal("22500.0"));
        expectedPrize.setTaxAmount(new BigDecimal("4000.0"));
        expectedPrize.setActualAmount(new BigDecimal("18500.0"));
        expectedPrize.setReturnAmount(new BigDecimal("0"));
        Game game = new Game();
        game.setId("GAME-EB");
        expectedPrize.setGame(game);
        this.assertOutput(ctx, respCtx, expectedPrize, ticket, startCreditLevel,
                BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET);
    }

    @Test
    public void testPayout_NewPrint_TaxWhenPayout_OK() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        this.jdbcTemplate.execute("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + " where game_id='GAME-EB'");
        this.jdbcTemplate.execute("update WINNER_TAX_THRESHOLDS set CALCULATION_METHOD=0");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());

        Prize expectedPrize = new Prize();
        expectedPrize.setPrizeAmount(new BigDecimal("22500.0"));
        expectedPrize.setTaxAmount(expectedPrize.getPrizeAmount().multiply(new BigDecimal("0.2")));
        expectedPrize.setActualAmount(expectedPrize.getPrizeAmount().subtract(expectedPrize.getTaxAmount()));
        expectedPrize.setReturnAmount(new BigDecimal("0"));
        Game game = new Game();
        game.setId("GAME-EB");
        expectedPrize.setGame(game);
        this.assertOutput(ctx, respCtx, expectedPrize, ticket, startCreditLevel,
                BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET);
    }

    @Test
    public void testPayout_Refund_TaxWhenDraw_OK() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        this.jdbcTemplate.execute("update eb_operation_parameters set payout_model="
                + BaseOperationParameter.PAYOUTMODE_REFUND);

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());

        Prize expectedPrize = new Prize();
        expectedPrize.setPrizeAmount(new BigDecimal("22500.0"));
        expectedPrize.setTaxAmount(new BigDecimal("4000.0"));
        // actualAmount = prizeAmount -taxAmount + returnAmount
        expectedPrize.setActualAmount(new BigDecimal("21500.0"));
        expectedPrize.setReturnAmount(new BigDecimal("3000.0"));
        Game game = new Game();
        game.setId("GAME-EB");
        expectedPrize.setGame(game);
        this.assertOutput(ctx, respCtx, expectedPrize, ticket, startCreditLevel,
                BaseOperationParameter.PAYOUTMODE_REFUND);
    }

    @Test
    public void testPayout_Refund_TaxWhenPayout_OK() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        this.jdbcTemplate.execute("update eb_operation_parameters set payout_model="
                + BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.execute("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + " where game_id='GAME-EB'");
        this.jdbcTemplate.execute("update WINNER_TAX_THRESHOLDS set CALCULATION_METHOD=0");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(200, respCtx.getResponseCode());

        Prize expectedPrize = new Prize();
        expectedPrize.setPrizeAmount(new BigDecimal("22500.0"));
        expectedPrize.setTaxAmount(expectedPrize.getPrizeAmount().multiply(new BigDecimal("0.2")));
        expectedPrize.setReturnAmount(new BigDecimal("3000.0"));
        // actualAmount = prizeAmount -taxAmount + returnAmount
        expectedPrize.setActualAmount(expectedPrize.getPrizeAmount().subtract(expectedPrize.getTaxAmount())
                .add(expectedPrize.getReturnAmount()));
        Game game = new Game();
        game.setId("GAME-EB");
        expectedPrize.setGame(game);

        this.assertOutput(ctx, respCtx, expectedPrize, ticket, startCreditLevel,
                BaseOperationParameter.PAYOUTMODE_REFUND);
    }

    @Test
    public void testPayout_Exceed_MaxAllowedAmount() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        this.jdbcTemplate.execute("update eb_winning set prize_amount=10000999,actual_payout=10000111 where id='1'");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(341, respCtx.getResponseCode());
    }

    @Test
    public void testPayout_In_Progress_Of_WinnerAnalysis() throws Exception {
        printMethod();
        ExtraBallTicket ticket = new ExtraBallTicket();
        ticket.setRawSerialNo("SN-EB-2");

        Context ctx = this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        this.jdbcTemplate.execute("update eb_game_instance set STOP_SELLING_TIME=sysdate-1/24 where id='GI-EB-1'");

        // retrieve the original data before issuing request.
        BigDecimal startCreditLevel = this.getMerchantDao().findById(Merchant.class, ctx.getMerchant().getId())
                .getSaleCreditLevel();

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));
        // this.setComplete();

        // assert response
        assertEquals(339, respCtx.getResponseCode());
    }

    // ----------------------------------------------------------
    // HELP METHODS
    // ----------------------------------------------------------

    protected void assertOutput(Context reqCtx, Context respCtx, Prize expectedPrize, ExtraBallTicket ticket,
            BigDecimal startCreditLevel, int payoutMode) throws Exception {
        // assert response
        Prize prize = (Prize) respCtx.getModel();
        assertEquals(expectedPrize.getPrizeAmount().doubleValue(), prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(expectedPrize.getTaxAmount().doubleValue(), prize.getTaxAmount().doubleValue(), 0);
        assertEquals(expectedPrize.getActualAmount().doubleValue(), prize.getActualAmount().doubleValue(), 0);
        assertEquals(expectedPrize.getReturnAmount().doubleValue(), prize.getReturnAmount().doubleValue(), 0);
        assertEquals(expectedPrize.getGame().getId(), prize.getGame().getId());
        ExtraBallTicket generatedTicket = null;
        if (payoutMode == BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET) {
            assertEquals(1, prize.getGeneratedTickets().size());
            generatedTicket = (ExtraBallTicket) prize.getGeneratedTickets().get(0);
            assertEquals(2, generatedTicket.getMultipleDraws());
            assertEquals(3000.0, generatedTicket.getTotalAmount().doubleValue(), 0);
            assertEquals(BaseTicket.TICKET_TYPE_NORMAL, generatedTicket.getTicketType());
            assertEquals("20120710", generatedTicket.getLastDrawNo());
            assertEquals(3, generatedTicket.getEntries().size());
            // assert content of entries.
        } else if (payoutMode == BaseOperationParameter.PAYOUTMODE_REFUND) {
            assertEquals(0, prize.getGeneratedTickets().size());
        }

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
            assertEquals(BaseTicket.STATUS_ACCEPTED, boughtTicket.getStatus());
            assertTrue(boughtTicket.isCountInPool());
        }

        if (payoutMode == BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET) {
            // assert new generated ticket
            List<ExtraBallTicket> dbGeneratedTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                    generatedTicket.getSerialNo(), true);
            assertEquals(2, dbGeneratedTickets.size());
            for (int i = 1; i < dbGeneratedTickets.size(); i++) {
                ExtraBallTicket newGeneratedTicket = dbGeneratedTickets.get(i);
                if (i == 0)
                    assertEquals("GI-EB-1", newGeneratedTicket.getGameInstance().getId());
                else if (i == 1)
                    assertEquals("GI-EB-2", newGeneratedTicket.getGameInstance().getId());
                assertEquals(BaseTicket.STATUS_ACCEPTED, newGeneratedTicket.getStatus());
                assertTrue(newGeneratedTicket.isCountInPool());
                assertEquals(respCtx.getTransactionID(), newGeneratedTicket.getTransaction().getId());
                assertNull(newGeneratedTicket.getBatchNo());
            }
            List<ExtraBallEntry> dbGeneratedEntries = this.getBaseEntryDao().findByTicketSerialNo(ExtraBallEntry.class,
                    dbGeneratedTickets.get(0).getSerialNo(), true);
            assertEquals(3, dbGeneratedEntries.size());

            // -- assert new print log
            NewPrintTicket newPrintTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
            assertEquals(dbGeneratedTickets.get(0).getSerialNo(), newPrintTicket.getNewTicketSerialNo());
            assertEquals(NewPrintTicket.STATUS_WAITCONFIRM, newPrintTicket.getStatus());
        }

        // -- assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNoAndStatus(ticket.getSerialNo(),
                Payout.STATUS_PAID);
        if (payoutMode == BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET) {
            assertEquals(1, dbPayouts.size());
        } else if (payoutMode == BaseOperationParameter.PAYOUTMODE_REFUND) {
            assertEquals(3, dbPayouts.size());
        }
        for (int i = 0; i < dbPayouts.size(); i++) {
            Payout dbPayout = dbPayouts.get(i);
            assertNotNull(dbPayout.getCreateTime());
            assertEquals(respCtx.getTransactionID(), dbPayout.getTransaction().getId());
            assertEquals(ticket.getSerialNo(), dbPayout.getTicketSerialNo());
            if (i == 0) {
                // only the 1st ticket will be paid, other 2 draws will be
                // printed or returned.
                assertEquals(prize.getActualAmount().subtract(prize.getReturnAmount()).doubleValue(), dbPayout
                        .getTotalAmount().doubleValue(), 0);
                assertEquals(prize.getPrizeAmount().doubleValue(), dbPayout.getBeforeTaxTotalAmount().doubleValue(), 0);
                assertEquals(Payout.TYPE_WINNING, dbPayout.getType());
            } else {
                assertEquals(dbTickets.get(0).getTotalAmount().doubleValue(), dbPayout.getTotalAmount().doubleValue(),
                        0);
                assertEquals(dbTickets.get(0).getTotalAmount().doubleValue(), dbPayout.getBeforeTaxTotalAmount()
                        .doubleValue(), 0);
                assertEquals(Payout.TYPE_RETURN, dbPayout.getType());
            }
            assertEquals(true, dbPayout.isValid());
            assertEquals(reqCtx.getTerminalId(), dbPayout.getDevId());
            assertEquals(reqCtx.getOperatorId(), dbPayout.getOperatorId());
            assertEquals(reqCtx.getMerchant().getId(), dbPayout.getMerchantId());
            assertNotNull(dbPayout.getCreateTime());
            assertEquals("GAME-EB", dbPayout.getGameId());
        }

        // -- assert credit level
        // OperatorCommission operatorCommission =
        // this.getOperatorCommissionDao()
        // .getByOperatorAndMerchantAndGame(reqCtx.getOperatorId(),
        // reqCtx.getMerchantId(), "GAME-EB");
        // BigDecimal newCreditLevel =
        // startCreditLevel.add(prize.getActualAmount()).add(
        // prize.getActualAmount().multiply(operatorCommission.getPayoutRate()));
        // assertEquals(newCreditLevel.doubleValue(),
        // this.getMerchantDao().getById(reqCtx.getMerchantId())
        // .getCreditLevel().doubleValue(), 0);
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
