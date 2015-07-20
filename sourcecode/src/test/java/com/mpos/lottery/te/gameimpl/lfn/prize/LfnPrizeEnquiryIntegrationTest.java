package com.mpos.lottery.te.gameimpl.lfn.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.mpos.lottery.te.gameimpl.lfn.sale.LfnTicket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelObjectItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class LfnPrizeEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_NewPrint_MultiDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from ld_winning");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(22200.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(24400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(2200.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(2100.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(1, prize.getPrizeItems().size());
        PrizeItemDto prizeItem = prize.getPrizeItems().get(0);
        assertEquals(3, prizeItem.getPrizeLevelItems().size());
        BaseGameInstance gameInstance = prizeItem.getGameInstance();
        assertEquals("LFN-1", gameInstance.getGameId());
        assertEquals("11001", gameInstance.getNumber());

        // sort PrizeLeveItemDto by prize level first
        this.sortPrizeLevelItemDto(prizeItem.getPrizeLevelItems());
        PrizeLevelItemDto levelItem1 = prizeItem.getPrizeLevelItems().get(0);
        assertEquals(20000.0, levelItem1.getPrizeAmount().doubleValue(), 0);
        assertEquals(18000.0, levelItem1.getActualAmount().doubleValue(), 0);
        assertEquals(2000.0, levelItem1.getTaxAmount().doubleValue(), 0);
        assertEquals(1, levelItem1.getNumberOfPrizeLevel());
        assertEquals("2", levelItem1.getPrizeLevel());

        PrizeLevelItemDto levelItem2 = prizeItem.getPrizeLevelItems().get(1);
        assertEquals(2000.0, levelItem2.getPrizeAmount().doubleValue(), 0);
        assertEquals(1900.0, levelItem2.getActualAmount().doubleValue(), 0);
        assertEquals(100.0, levelItem2.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItem2.getNumberOfPrizeLevel());
        assertEquals("5", levelItem2.getPrizeLevel());

        PrizeLevelItemDto levelItem3 = prizeItem.getPrizeLevelItems().get(2);
        assertEquals(100.0, levelItem3.getPrizeAmount().doubleValue(), 0);
        assertEquals(100.0, levelItem3.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem3.getTaxAmount().doubleValue(), 0);
        assertEquals(4, levelItem3.getNumberOfPrizeLevel());
        assertEquals("7", levelItem3.getPrizeLevel());
    }

    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_NewPrint_MultiDraw_LuckyDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update bd_prize_level_item set PRIZE_LEVEL_NUM=1 where id='PLI-41'");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(2355533.32, prize.getActualAmount().doubleValue(), 0);
        assertEquals(2824400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(468866.68, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(4000, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(2100.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(2, prize.getPrizeItems().size());
        PrizeItemDto prizeItem = prize.getPrizeItems().get(0);
        assertEquals(3, prizeItem.getPrizeLevelItems().size());
        BaseGameInstance gameInstance = prizeItem.getGameInstance();
        assertEquals("LFN-1", gameInstance.getGameId());
        assertEquals("11001", gameInstance.getNumber());

        // sort PrizeLeveItemDto by prize level first
        this.sortPrizeLevelItemDto(prizeItem.getPrizeLevelItems());
        PrizeLevelItemDto levelItem1 = prizeItem.getPrizeLevelItems().get(0);
        assertEquals(20000.0, levelItem1.getPrizeAmount().doubleValue(), 0);
        assertEquals(18000.0, levelItem1.getActualAmount().doubleValue(), 0);
        assertEquals(2000.0, levelItem1.getTaxAmount().doubleValue(), 0);
        assertEquals(1, levelItem1.getNumberOfPrizeLevel());
        assertEquals("2", levelItem1.getPrizeLevel());

        PrizeLevelItemDto levelItem2 = prizeItem.getPrizeLevelItems().get(1);
        assertEquals(2000.0, levelItem2.getPrizeAmount().doubleValue(), 0);
        assertEquals(1900.0, levelItem2.getActualAmount().doubleValue(), 0);
        assertEquals(100.0, levelItem2.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItem2.getNumberOfPrizeLevel());
        assertEquals("5", levelItem2.getPrizeLevel());

        PrizeLevelItemDto levelItem3 = prizeItem.getPrizeLevelItems().get(2);
        assertEquals(100.0, levelItem3.getPrizeAmount().doubleValue(), 0);
        assertEquals(100.0, levelItem3.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem3.getTaxAmount().doubleValue(), 0);
        assertEquals(4, levelItem3.getNumberOfPrizeLevel());
        assertEquals("7", levelItem3.getPrizeLevel());

        // assert lucky prize items
        PrizeItemDto luckyPrizeItem = prize.getPrizeItems().get(1);
        assertEquals("LD-1", luckyPrizeItem.getGameInstance().getGameId());
        assertEquals("001", luckyPrizeItem.getGameInstance().getNumber());
        PrizeLevelItemDto levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(333333.33, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(66666.67, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(1000000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(833333.33, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(166666.67, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("4", levelItemDto.getPrizeLevel());
        // assert object
        PrizeLevelObjectItemDto object = levelItemDto.getPrizeLevelObjectItems().get(0);
        assertEquals(1, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);
    }

    @Test
    public void testEnquiry_TaxOnPayout_BasedPerTicket_NewPrint_MultiDraw_LuckyDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(2353666.66, prize.getActualAmount().doubleValue(), 0);
        assertEquals(2824400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(470733.34, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(8000, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(2100.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(2, prize.getPrizeItems().size());
        PrizeItemDto prizeItem = prize.getPrizeItems().get(0);
        assertEquals(3, prizeItem.getPrizeLevelItems().size());
        BaseGameInstance gameInstance = prizeItem.getGameInstance();
        assertEquals("LFN-1", gameInstance.getGameId());
        assertEquals("11001", gameInstance.getNumber());

        // sort PrizeLeveItemDto by prize level first
        this.sortPrizeLevelItemDto(prizeItem.getPrizeLevelItems());
        PrizeLevelItemDto levelItem1 = prizeItem.getPrizeLevelItems().get(0);
        assertEquals(20000.0, levelItem1.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem1.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem1.getTaxAmount().doubleValue(), 0);
        assertEquals(1, levelItem1.getNumberOfPrizeLevel());
        assertEquals("2", levelItem1.getPrizeLevel());

        PrizeLevelItemDto levelItem2 = prizeItem.getPrizeLevelItems().get(1);
        assertEquals(2000.0, levelItem2.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem2.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem2.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItem2.getNumberOfPrizeLevel());
        assertEquals("5", levelItem2.getPrizeLevel());

        PrizeLevelItemDto levelItem3 = prizeItem.getPrizeLevelItems().get(2);
        assertEquals(100.0, levelItem3.getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem3.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem3.getTaxAmount().doubleValue(), 0);
        assertEquals(4, levelItem3.getNumberOfPrizeLevel());
        assertEquals("7", levelItem3.getPrizeLevel());

        // assert lucky prize items
        PrizeItemDto luckyPrizeItem = prize.getPrizeItems().get(1);
        assertEquals("LD-1", luckyPrizeItem.getGameInstance().getGameId());
        assertEquals("001", luckyPrizeItem.getGameInstance().getNumber());
        PrizeLevelItemDto levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(0, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(1000000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(0, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("4", levelItemDto.getPrizeLevel());
        // assert object
        PrizeLevelObjectItemDto object = levelItemDto.getPrizeLevelObjectItems().get(0);
        assertEquals(2, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);
    }

    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_NewPrint_SingleDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("delete from LFN_TE_TICKET where id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(22200.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(24400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(2200.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(700.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(1, prize.getPrizeItems().size());
        PrizeItemDto prizeItem = prize.getPrizeItems().get(0);
        assertEquals(3, prizeItem.getPrizeLevelItems().size());
        BaseGameInstance gameInstance = prizeItem.getGameInstance();
        assertEquals("LFN-1", gameInstance.getGameId());
        assertEquals("11001", gameInstance.getNumber());

        // sort PrizeLeveItemDto by prize level first
        this.sortPrizeLevelItemDto(prizeItem.getPrizeLevelItems());
        PrizeLevelItemDto levelItem1 = prizeItem.getPrizeLevelItems().get(0);
        assertEquals(20000.0, levelItem1.getPrizeAmount().doubleValue(), 0);
        assertEquals(18000.0, levelItem1.getActualAmount().doubleValue(), 0);
        assertEquals(2000.0, levelItem1.getTaxAmount().doubleValue(), 0);
        assertEquals(1, levelItem1.getNumberOfPrizeLevel());
        assertEquals("2", levelItem1.getPrizeLevel());

        PrizeLevelItemDto levelItem2 = prizeItem.getPrizeLevelItems().get(1);
        assertEquals(2000.0, levelItem2.getPrizeAmount().doubleValue(), 0);
        assertEquals(1900.0, levelItem2.getActualAmount().doubleValue(), 0);
        assertEquals(100.0, levelItem2.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItem2.getNumberOfPrizeLevel());
        assertEquals("5", levelItem2.getPrizeLevel());

        PrizeLevelItemDto levelItem3 = prizeItem.getPrizeLevelItems().get(2);
        assertEquals(100.0, levelItem3.getPrizeAmount().doubleValue(), 0);
        assertEquals(100.0, levelItem3.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem3.getTaxAmount().doubleValue(), 0);
        assertEquals(4, levelItem3.getNumberOfPrizeLevel());
        assertEquals("7", levelItem3.getPrizeLevel());
    }

    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_Refund_OK() throws Exception {
        printMethod();
        LfnTicket ticket = new LfnTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("update LFN_OPERATION_PARAMETERS set PAYOUT_MODEL=0 where ID='LFN-OP-1'");
        this.jdbcTemplate.update("delete from ld_winning");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(23600.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(24400.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(2200.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_REFUND, prize.getPayoutMode());
        assertEquals(0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(1400.0, prize.getReturnAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(2100.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(1, prize.getPrizeItems().size());
        PrizeItemDto prizeItem = prize.getPrizeItems().get(0);
        assertEquals(3, prizeItem.getPrizeLevelItems().size());
        BaseGameInstance gameInstance = prizeItem.getGameInstance();
        assertEquals("LFN-1", gameInstance.getGameId());
        assertEquals("11001", gameInstance.getNumber());

        // sort PrizeLeveItemDto by prize level first
        this.sortPrizeLevelItemDto(prizeItem.getPrizeLevelItems());
        PrizeLevelItemDto levelItem1 = prizeItem.getPrizeLevelItems().get(0);
        assertEquals(20000.0, levelItem1.getPrizeAmount().doubleValue(), 0);
        assertEquals(18000.0, levelItem1.getActualAmount().doubleValue(), 0);
        assertEquals(2000.0, levelItem1.getTaxAmount().doubleValue(), 0);
        assertEquals(1, levelItem1.getNumberOfPrizeLevel());
        assertEquals("2", levelItem1.getPrizeLevel());

        PrizeLevelItemDto levelItem2 = prizeItem.getPrizeLevelItems().get(1);
        assertEquals(2000.0, levelItem2.getPrizeAmount().doubleValue(), 0);
        assertEquals(1900.0, levelItem2.getActualAmount().doubleValue(), 0);
        assertEquals(100.0, levelItem2.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItem2.getNumberOfPrizeLevel());
        assertEquals("5", levelItem2.getPrizeLevel());

        PrizeLevelItemDto levelItem3 = prizeItem.getPrizeLevelItems().get(2);
        assertEquals(100.0, levelItem3.getPrizeAmount().doubleValue(), 0);
        assertEquals(100.0, levelItem3.getActualAmount().doubleValue(), 0);
        assertEquals(0.0, levelItem3.getTaxAmount().doubleValue(), 0);
        assertEquals(4, levelItem3.getNumberOfPrizeLevel());
        assertEquals("7", levelItem3.getPrizeLevel());
    }
}
