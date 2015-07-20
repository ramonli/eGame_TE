package com.mpos.lottery.te.gameimpl.union.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.union.sale.UnionTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class UnionPrizeEnquiryIntegrationTest extends BaseServletIntegrationTest {

    /**
     * Calculate tax when winner analysis
     */
    @Test
    public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4887973.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when winner analysis
     */
    @Test
    public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket_ByBarcode() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setBarcode(false, "01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4887973.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when winner analysis...win lucky draw
     */
    @Test
    public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket_LuckyDraw() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(3, dto.getPrizeItems().size());

        assertEquals(10032000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1374040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(7367973.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout...win both normal and lucky draw
     */
    @Test
    public void testEnquiry_WIN_TaxWhenPayout_BasePerTIcket_PrintNewTicket_LuckyDraw() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(3, dto.getPrizeItems().size());

        assertEquals(10032000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1672000.01, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(8359999.99, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout...win only lucky draw
     */
    @Test
    public void testEnquiry_WIN_TaxWhenPayout_BasePerTIcket_OnlyLuckyDraw() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
        this.jdbcTemplate.update("delete from un_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1, dto.getPrizeItems().size());

        assertEquals(2800000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(466666.67, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(2333333.33, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout, based on per bet
     */
    @Test
    public void testEnquiry_WIN_TaxWhenPayout_PrintNewTicket() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1205333.33, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(6026666.67, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout, and based on per ticket
     */
    @Test
    public void testEnquiry_WIN_TaxWhenPayout_BasedPerTicket_PrintNewTicket() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1205333.34, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(6026666.66, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * NO winning
     */
    @Test
    public void testEnquiry_NoWIN_PrintNewTicket() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from un_winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0, dto.getPrizeItems().size());

        assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * NO winning, calculate tax when payout
     */
    @Test
    public void testEnquiry_NoWIN_PrintNewTicket_LuckyDraw() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from un_winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1, dto.getPrizeItems().size());

        assertEquals(2800000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(466666.68, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(2333333.32, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when winner analysis
     */
    @Test
    public void testEnquiry_WIN_TaxWhenAnalysis_Return() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4890473.1, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout
     */
    @Test
    public void testEnquiry_WIN_TaxWhenPayout_Return() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1205333.33, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(6029166.77, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * NO winning
     */
    @Test
    public void testEnquiry_NoWIN_Return() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from un_winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0, dto.getPrizeItems().size());

        assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
    }

    @Test
    public void testEnquiry_NoExistTicket() throws Exception {
        printMethod();
        UnionTicket ticket = new UnionTicket();
        ticket.setRawSerialNo("S-123456-NO");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from un_winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader(Context.HEADER_PROTOCAL_VERSION, reqCtx.getProtocalVersion());
        req.addHeader(Context.HEADER_GPE_ID, reqCtx.getGpe().getId());
        req.addHeader(Context.HEADER_TRANSACTION_TYPE, reqCtx.getTransType() + "");
        req.addHeader(Context.HEADER_TIMESTAMP, reqCtx.getStrTimestamp());
        req.addHeader(Context.HEADER_BATCHNUMBER, reqCtx.getBatchNumber());
        req.addHeader(Context.HEADER_TRACE_MESSAGE_ID, reqCtx.getTraceMessageId());
        req.addHeader(Context.HEADER_OPERATOR_ID, reqCtx.getOperatorId());
        req.addHeader(Context.HEADER_TERMINAL_ID, reqCtx.getTerminalId() + "");
        req.addHeader(Context.HEADER_GAME_TYPE_ID, "66");
        this.assembleReqBody(reqCtx, req);
        Context respCtx = this.doPost(req);

        assertNotNull(respCtx);
        assertEquals(SystemException.CODE_UNSUPPORTED_TRANSTYPE, respCtx.getResponseCode());
    }

    private void switchPayoutMode(int payoutMode) {
        // the default payout mode: print new ticket
        this.jdbcTemplate.update("update UN_OPERATION_PARAMETERS set PAYOUT_MODEL=" + payoutMode);
    }

}
