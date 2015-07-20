package com.mpos.lottery.te.gameimpl.bingo.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.Constants;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

public class BingoPrizeEnquiryIntegrationTest extends BaseServletIntegrationTest {

    /**
     * Calculate tax when winner analysis
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from TE_BG_TICKET t where t.id in ('2','3')");
        this.jdbcTemplate.update("delete from bg_winning_lucky");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1, dto.getPrizeItems().size());

        assertEquals(0, dto.getGeneratedTickets().size());
        assertEquals(5032000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1004040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4027967.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when winner analysis
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket_ByBarcode() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setBarcode(false, "01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from TE_BG_TICKET t where t.id in ('2','3')");
        this.jdbcTemplate.update("delete from bg_winning_lucky");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1, dto.getPrizeItems().size());

        assertEquals(0, dto.getGeneratedTickets().size());
        assertEquals(5032000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1004040.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4027967.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when winner analysis...win lucky draw
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenAnalysis_PrintNewTicket_SecondPrize() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1, dto.getPrizeItems().size());
        assertEquals(0, dto.getGeneratedTickets().size());
        assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1004075.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4027982.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
        List<PrizeItemDto> list = dto.getPrizeItems();
        for (PrizeItemDto prizeItemDto : list) {
            if (prizeItemDto.getType() == 1) {
                assertEquals(5, prizeItemDto.getPrizeLevelItems().size());
            }
        }
    }

    /**
     * Calculate tax when payout...win both normal and lucky draw
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenPayout_BasePerTIcket_PrintNewTicket_SecondPrize() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(838671.67, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4193378.33, dto.getActualAmount().doubleValue(), 0);
        assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout...win only lucky draw
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenPayout_BasePerTIcket_OnlySecondPrize() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
        this.jdbcTemplate.update("delete from BG_WINNING");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1, dto.getPrizeItems().size());

        assertEquals(50.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(5.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(45.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout, based on per bet
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenPayout_PrintNewTicket() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("delete from bg_winning_lucky");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1, dto.getPrizeItems().size());

        assertEquals(5032000.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(838666.67, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4193333.33, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout, and based on per ticket
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenPayout_BasedPerTicket_PrintNewTicket() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(838671.67, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4193378.33, dto.getActualAmount().doubleValue(), 0);
        assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);

    }

    /**
     * NO winning
     */
    // @Test
    public void testEnquiry_NoWIN_PrintNewTicket() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from BG_winning w where w.SERIAL_NO='" + ticket.getSerialNo() + "'");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("delete from bg_winning_lucky");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
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
    // @Test
    public void testEnquiry_NoWIN_PrintNewTicket_SecondPrize() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from BG_winning w where w.SERIAL_NO='" + ticket.getSerialNo() + "'");
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(1, dto.getPrizeItems().size());

        assertEquals(50.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(35.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(15, dto.getActualAmount().doubleValue(), 0);
        assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when winner analysis
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenAnalysis_Return() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");

        // this.jdbcTemplate.update("delete from bg_winning_lucky");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(5032050.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(1004075.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4029382.0, dto.getActualAmount().doubleValue(), 0);
        assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(1400.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * Calculate tax when payout
     */
    // @Test
    public void testEnquiry_WIN_TaxWhenPayout_Return() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(2, dto.getPrizeItems().size());

        assertEquals(5032050, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(838676.67, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(4193373.33, dto.getActualAmount().doubleValue(), 0);
        assertEquals(20.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(1400.0, dto.getReturnAmount().doubleValue(), 0);
    }

    /**
     * NO winning
     */
    // @Test
    public void testEnquiry_NoWIN_Return() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from BG_winning w where w.SERIAL_NO='" + ticket.getSerialNo() + "'");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("delete from bg_winning_lucky");
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);
        this.jdbcTemplate.update("delete from ld_winning");

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        PrizeDto dto = (PrizeDto) respCtx.getModel();
        assertEquals(0, dto.getPrizeItems().size());

        assertEquals(0.0, dto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getTaxAmount().doubleValue(), 0);
        assertEquals(1400, dto.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, dto.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(1400, dto.getReturnAmount().doubleValue(), 0);
    }

    // @Test
    public void testEnquiry_NoExistTicket() throws Exception {
        printMethod();
        BingoTicket ticket = new BingoTicket();
        ticket.setRawSerialNo("S-123456-NO");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from BG_winning w where w.SERIAL_NO='" + ticket.getSerialNo() + "'");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("delete from bg_winning_lucky");
        this.switchPayoutMode(BaseOperationParameter.PAYOUTMODE_REFUND);

        Context reqCtx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Constants.GAME_TYPE_BINGO + "");

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
        this.jdbcTemplate.update("update BG_OPERATION_PARAMETERS set PAYOUT_MODEL=" + payoutMode);
    }

    private void accertWinningLevel(PrizeDto dto) {
        List<PrizeItemDto> list = dto.getPrizeItems();
        for (PrizeItemDto prizeItemDto : list) {
            if (prizeItemDto.getType() == 1) {
                assertEquals(3, prizeItemDto.getPrizeLevelItems().size());
            } else {
                assertEquals(3, prizeItemDto.getPrizeLevelItems().size());
            }
        }
    }

}
