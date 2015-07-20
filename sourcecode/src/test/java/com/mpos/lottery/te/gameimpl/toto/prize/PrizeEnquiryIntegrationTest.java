package com.mpos.lottery.te.gameimpl.toto.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class PrizeEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiryPrize_TaxWhenAnalysis() throws Exception {
        printMethod();
        ToToTicket ticket = new ToToTicket();
        ticket.setRawSerialNo("T-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from ld_winning");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertTrue(prize.isVerifyPIN());
        assertEquals(22000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(2200.0, prize.getTaxAmount().doubleValue(), 0);
        assertEquals(19806.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getReturnAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getLuckyPrizeAmount().doubleValue(), 0);
    }

    @Test
    public void testEnquiryPrize_TaxWhenAnalysis_LuckyDraw() throws Exception {
        printMethod();
        ToToTicket ticket = new ToToTicket();
        ticket.setRawSerialNo("T-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        // this.jdbcTemplate.update("delete from ld_winning");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertTrue(prize.isVerifyPIN());
        assertEquals(2822000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(322200.0, prize.getTaxAmount().doubleValue(), 0);
        assertEquals(2499806.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getReturnAmount().doubleValue(), 0);
        assertEquals(8000.0, prize.getLuckyPrizeAmount().doubleValue(), 0);
    }

    @Test
    public void testEnquiryPrize_TaxWhenPayout() throws Exception {
        printMethod();
        ToToTicket ticket = new ToToTicket();
        ticket.setRawSerialNo("T-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
        this.jdbcTemplate.update("delete from ld_winning");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertTrue(prize.isVerifyPIN());
        assertEquals(22000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(3666.68, prize.getTaxAmount().doubleValue(), 0);
        assertEquals(18333.32, prize.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getReturnAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getLuckyPrizeAmount().doubleValue(), 0);
    }

    @Test
    public void testEnquiryPrize_NoWin() throws Exception {
        printMethod();
        ToToTicket ticket = new ToToTicket();
        ticket.setRawSerialNo("T-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from TT_WINNING w where w.TICKET_SERIALNO='" + ticket.getSerialNo() + "'");
        this.jdbcTemplate.update("delete from ld_winning");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertTrue(prize.isVerifyPIN());
        assertEquals(0.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getTaxAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getReturnAmount().doubleValue(), 0);
        assertEquals(0.0, prize.getLuckyPrizeAmount().doubleValue(), 0);
    }
}
