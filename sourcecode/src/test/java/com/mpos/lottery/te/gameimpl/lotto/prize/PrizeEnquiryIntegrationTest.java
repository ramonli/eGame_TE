package com.mpos.lottery.te.gameimpl.lotto.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.Constants;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelObjectItemDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.List;

public class PrizeEnquiryIntegrationTest extends BaseServletIntegrationTest {

    // /**
    // * Calculate tax when winner analysis
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(2, dto.getPrizeItems().size());
    //
    // assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(4887973.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * Calculate tax when winner analysis
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket_ByBarcode() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setBarcode(false, "01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(2, dto.getPrizeItems().size());
    //
    // assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(4887973.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * Calculate tax when winner analysis...win lucky draw
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket_LuckyDraw() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(3, dto.getPrizeItems().size());
    //
    // assertEquals(10032000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1374040.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(7367973.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * Calculate tax when payout...win both normal and lucky draw
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenPayout_BasePerTIcket_PrintNewTicket_LuckyDraw() throws
    // Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
    // + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(3, dto.getPrizeItems().size());
    //
    // assertEquals(10032000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1672000.01, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(8359999.99, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * Calculate tax when payout...win only lucky draw
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenPayout_BasePerTIcket_OnlyLuckyDraw() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
    // + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
    // this.jdbcTemplate.update("delete from WINNING");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(1, dto.getPrizeItems().size());
    //
    // assertEquals(2800000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(466666.67, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(2333333.33, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * Calculate tax when payout, based on per bet
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenPayout_PrintNewTicket() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(2, dto.getPrizeItems().size());
    //
    // assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1205333.33, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(6026666.67, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * Calculate tax when payout, and based on per ticket
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenPayout_BasedPerTicket_PrintNewTicket() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
    // + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(2, dto.getPrizeItems().size());
    //
    // assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1205333.34, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(6026666.66, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * NO winning
    // */
    // @Test
    // public void testEnquiry_NoWIN_PrintNewTicket() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("delete from winning w where w.ticket_serialno='" +
    // ticket.getSerialNo()
    // + "'");
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(0, dto.getPrizeItems().size());
    //
    // assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * NO winning, calculate tax when payout
    // */
    // @Test
    // public void testEnquiry_NoWIN_PrintNewTicket_LuckyDraw() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("delete from winning w where w.ticket_serialno='" +
    // ticket.getSerialNo()
    // + "'");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(1, dto.getPrizeItems().size());
    //
    // assertEquals(2800000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(466666.68, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(2333333.32, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * Calculate tax when winner analysis
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenAnalysis_Return() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
    // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(2, dto.getPrizeItems().size());
    //
    // assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1054040.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(4890473.1, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * Calculate tax when payout
    // */
    // @Test
    // public void testEnquiry_WIN_TaxWhenPayout_Return() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
    // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(2, dto.getPrizeItems().size());
    //
    // assertEquals(7232000.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(1205333.33, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(6029166.77, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // /**
    // * NO winning
    // */
    // @Test
    // public void testEnquiry_NoWIN_Return() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("delete from winning w where w.ticket_serialno='" +
    // ticket.getSerialNo()
    // + "'");
    // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertNotNull(respCtx);
    // assertEquals(200, respCtx.getResponseCode());
    // assertNotNull(respCtx.getModel());
    // PrizeDto dto = (PrizeDto) respCtx.getModel();
    // assertEquals(0, dto.getPrizeItems().size());
    //
    // assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
    // assertEquals(2500.1, dto.getActualAmount().doubleValue(), 0);
    // assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
    // assertEquals(2500.1, dto.getReturnAmount().doubleValue(), 0);
    // }
    //
    // @Test
    // public void testEnquiry_NoExistTicket() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("S-123456-NO");
    // ticket.setValidationCode("111111");
    //
    // this.jdbcTemplate.update("delete from winning w where w.ticket_serialno='" +
    // ticket.getSerialNo()
    // + "'");
    // this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
    // this.jdbcTemplate.update("delete from ld_winning");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
    //
    // MockHttpServletRequest req = new MockHttpServletRequest();
    // req.addHeader(Context.HEADER_PROTOCAL_VERSION, reqCtx.getProtocalVersion());
    // req.addHeader(Context.HEADER_GPE_ID, reqCtx.getGpe().getId());
    // req.addHeader(Context.HEADER_TRANSACTION_TYPE, reqCtx.getTransType() + "");
    // req.addHeader(Context.HEADER_TIMESTAMP, reqCtx.getStrTimestamp());
    // req.addHeader(Context.HEADER_BATCHNUMBER, reqCtx.getBatchNumber());
    // req.addHeader(Context.HEADER_TRACE_MESSAGE_ID, reqCtx.getTraceMessageId());
    // req.addHeader(Context.HEADER_OPERATOR_ID, reqCtx.getOperatorId());
    // req.addHeader(Context.HEADER_TERMINAL_ID, reqCtx.getTerminalId() + "");
    // req.addHeader(Context.HEADER_GAME_TYPE_ID, "66");
    // this.assembleReqBody(reqCtx, req);
    // Context respCtx = this.doPost(req);
    //
    // assertNotNull(respCtx);
    // assertEquals(SystemException.CODE_UNSUPPORTED_TRANSTYPE, respCtx.getResponseCode());
    // }

    @Test
    public void testEnquiry_Return_TaxWhenPayout_BasedPerTicket_WinOnlyLuckyDraw_WinOnlyObject() throws Exception {
        printMethod();
        LottoTicket ticket = new LottoTicket();
        ticket.setRawSerialNo("S-123456"); // print new ticket
        ticket.setValidationCode("111111");
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from winning w where w.ticket_serialno='" + ticket.getSerialNo() + "'");
        // remove return amount
        this.jdbcTemplate.update("delete from TE_TICKET where ID='TICKET-113'");
        // remove all cash prize
        this.jdbcTemplate.update("delete from LD_WINNING where prize_level=1");
        this.jdbcTemplate.update("delete from BD_PRIZE_LEVEL_ITEM where ID='PLI-4'");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_LOTTO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));
        PrizeDto prize = (PrizeDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(8000.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0, dto.getReturnAmount().doubleValue(), 0);
        // asert new print ticket
        assertEquals(true, dto.getNewPrintTicket() == null);

        PrizeLevelItemDto levelItem = dto.getPrizeItems().get(0).getPrizeLevelItems().get(0);
        assertEquals(2, levelItem.getNumberOfPrizeLevel());
        assertEquals(0, levelItem.getPrizeAmount().doubleValue(), 0);
        assertEquals(0, levelItem.getActualAmount().doubleValue(), 0);
        assertEquals(0, levelItem.getTaxAmount().doubleValue(), 0);

        // assert object prize
        List<PrizeLevelObjectItemDto> objectItems = dto.getPrizeItems().get(0).getPrizeLevelItems().get(0)
                .getPrizeLevelObjectItems();
        assertEquals(1, objectItems.size());
        assertEquals("Sony Camera", objectItems.get(0).getObjectName());
        assertEquals(2000.0, objectItems.get(0).getPrice().doubleValue(), 0);
        assertEquals(500.0, objectItems.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(2, objectItems.get(0).getNumberOfObject());
    }

    private void switchPayoutMode(int payoutMode) {
        // the default payout mode: print new ticket
        this.jdbcTemplate.update("update LOTTO_OPERATION_PARAMETERS set PAYOUT_MODEL=" + payoutMode);
    }

}
