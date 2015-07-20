package com.mpos.lottery.te.gameimpl.extraball.payout;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class EnquiryPrizeIntegrationTest extends BaseServletIntegrationTest {
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

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");

        // Issue request
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());

        Prize expectedPrize = new Prize();
        expectedPrize.setPrizeAmount(new BigDecimal("22500.0"));
        expectedPrize.setTaxAmount(new BigDecimal("4000.0"));
        expectedPrize.setActualAmount(new BigDecimal("18500.0"));
        expectedPrize.setReturnAmount(new BigDecimal("0"));
        this.assertOutput(ctx, respCtx, expectedPrize, ticket, BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET);
    }

    // @Test
    // public void testPayout_NewPrint_TaxWhenPayout_OK() throws Exception {
    // printMethod();
    // ExtraBallTicket ticket = new ExtraBallTicket();
    // ticket.setRawSerialNo("SN-EB-2");
    //
    // Context ctx =
    // this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
    //
    // this.getJdbcTemplate().execute(
    // "update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
    // + " where game_id='GAME-EB'");
    // this.getJdbcTemplate().execute("update WINNER_TAX_THRESHOLDS set CALCULATION_METHOD=0");
    //
    // // Issue request
    // Context respCtx = doPost(this.mockRequest(ctx));
    // // this.setComplete();
    //
    // // assert response
    // assertEquals(200, respCtx.getResponseCode());
    //
    // PrizeDto expectedPrize = new PrizeDto();
    // expectedPrize.setPrizeAmount(new BigDecimal("22500.0"));
    // expectedPrize.setTaxAmount(expectedPrize.getPrizeAmount().multiply(new
    // BigDecimal("0.2")));
    // expectedPrize.setActualAmount(expectedPrize.getPrizeAmount().subtract(
    // expectedPrize.getTaxAmount()));
    // expectedPrize.setReturnAmount(new BigDecimal("0"));
    // this.assertOutput(ctx, respCtx, expectedPrize, ticket,
    // BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET);
    // }
    //
    // @Test
    // public void testPayout_Refund_TaxWhenDraw_OK() throws Exception {
    // printMethod();
    // ExtraBallTicket ticket = new ExtraBallTicket();
    // ticket.setRawSerialNo("SN-EB-2");
    //
    // Context ctx =
    // this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
    //
    // this.getJdbcTemplate().execute(
    // "update eb_operation_parameters set payout_model="
    // + BaseOperationParameter.PAYOUTMODE_REFUND);
    //
    // // Issue request
    // Context respCtx = doPost(this.mockRequest(ctx));
    // // this.setComplete();
    //
    // // assert response
    // assertEquals(200, respCtx.getResponseCode());
    //
    // PrizeDto expectedPrize = new PrizeDto();
    // expectedPrize.setPrizeAmount(new BigDecimal("22500.0"));
    // expectedPrize.setTaxAmount(new BigDecimal("4000.0"));
    // // actualAmount = prizeAmount -taxAmount + returnAmount
    // expectedPrize.setActualAmount(new BigDecimal("21500.0"));
    // expectedPrize.setReturnAmount(new BigDecimal("3000.0"));
    // this.assertOutput(ctx, respCtx, expectedPrize, ticket,
    // BaseOperationParameter.PAYOUTMODE_REFUND);
    // }
    //
    // @Test
    // public void testPayout_Refund_TaxWhenPayout_OK() throws Exception {
    // printMethod();
    // ExtraBallTicket ticket = new ExtraBallTicket();
    // ticket.setRawSerialNo("SN-EB-2");
    //
    // Context ctx =
    // this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
    //
    // this.getJdbcTemplate().execute(
    // "update eb_operation_parameters set payout_model="
    // + BaseOperationParameter.PAYOUTMODE_REFUND);
    // this.getJdbcTemplate().execute(
    // "update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
    // + " where game_id='GAME-EB'");
    // this.getJdbcTemplate().execute("update WINNER_TAX_THRESHOLDS set CALCULATION_METHOD=0");
    //
    // // Issue request
    // Context respCtx = doPost(this.mockRequest(ctx));
    // // this.setComplete();
    //
    // // assert response
    // assertEquals(200, respCtx.getResponseCode());
    //
    // PrizeDto expectedPrize = new PrizeDto();
    // expectedPrize.setPrizeAmount(new BigDecimal("22500.0"));
    // expectedPrize.setTaxAmount(expectedPrize.getPrizeAmount().multiply(new
    // BigDecimal("0.2")));
    // expectedPrize.setReturnAmount(new BigDecimal("3000.0"));
    // // actualAmount = prizeAmount -taxAmount + returnAmount
    // expectedPrize.setActualAmount(expectedPrize.getPrizeAmount()
    // .subtract(expectedPrize.getTaxAmount()).add(expectedPrize.getReturnAmount()));
    //
    // this.assertOutput(ctx, respCtx, expectedPrize, ticket,
    // BaseOperationParameter.PAYOUTMODE_REFUND);
    // }
    //
    // @Test
    // public void testPayout_In_Progress_Of_WinnerAnalysis() throws Exception {
    // printMethod();
    // ExtraBallTicket ticket = new ExtraBallTicket();
    // ticket.setRawSerialNo("SN-EB-2");
    //
    // Context ctx =
    // this.getDefaultContext(TransactionType.PAYOUT.getRequestType(), ticket);
    // ctx.setGameTypeId(Game.TYPE_EXTRABALL + "");
    //
    // this.getJdbcTemplate().execute(
    // "update eb_game_instance set STOP_SELLING_TIME=sysdate-1/24 where id='GI-EB-1'");
    //
    // // retrieve the original data before issuing request.
    // BigDecimal startCreditLevel =
    // this.getMerchantDao().getById(ctx.getMerchantId())
    // .getCreditLevel();
    //
    // // Issue request
    // Context respCtx = doPost(this.mockRequest(ctx));
    // // this.setComplete();
    //
    // // assert response
    // assertEquals(339, respCtx.getResponseCode());
    // }

    // ----------------------------------------------------------
    // HELP METHODS
    // ----------------------------------------------------------

    protected void assertOutput(Context reqCtx, Context respCtx, Prize expectedPrize, ExtraBallTicket ticket,
            int payoutMode) {
        // assert response
        Prize prize = (Prize) respCtx.getModel();
        assertEquals(expectedPrize.getPrizeAmount().doubleValue(), prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(expectedPrize.getTaxAmount().doubleValue(), prize.getTaxAmount().doubleValue(), 0);
        assertEquals(expectedPrize.getActualAmount().doubleValue(), prize.getActualAmount().doubleValue(), 0);
        assertEquals(expectedPrize.getReturnAmount().doubleValue(), prize.getReturnAmount().doubleValue(), 0);
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
