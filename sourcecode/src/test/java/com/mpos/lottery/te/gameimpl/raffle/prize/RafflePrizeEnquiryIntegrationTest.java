package com.mpos.lottery.te.gameimpl.raffle.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
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

public class RafflePrizeEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_NewPrint_MultiDraw_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS
                + " where GAME_TYPE_ID=14");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(2480000.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(2800000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(8000, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(1400.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(1, prize.getPrizeItems().size());
        PrizeItemDto normalPrizeItem = prize.getPrizeItems().get(0);
        assertEquals(2, normalPrizeItem.getPrizeLevelItems().size());
        BaseGameInstance gameInstance = normalPrizeItem.getGameInstance();
        assertEquals("RA-1", gameInstance.getGameId());
        assertEquals("11001", gameInstance.getNumber());

        PrizeLevelItemDto levelItem1 = normalPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000.0, levelItem1.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000.0, levelItem1.getActualAmount().doubleValue(), 0);
        assertEquals(80000.0, levelItem1.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItem1.getNumberOfPrizeLevel());
        assertEquals("1", levelItem1.getPrizeLevel());

        PrizeLevelItemDto levelItem2 = normalPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(1000000.0, levelItem2.getPrizeAmount().doubleValue(), 0);
        assertEquals(920000.0, levelItem2.getActualAmount().doubleValue(), 0);
        assertEquals(80000.0, levelItem2.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItem2.getNumberOfPrizeLevel());
        assertEquals("4", levelItem2.getPrizeLevel());
        // assert object
        PrizeLevelObjectItemDto object = levelItem2.getPrizeLevelObjectItems().get(0);
        assertEquals(2, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);
    }

    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_NewPrint_MultiDraw_LuckyDraw_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(4960000.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(5600000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(640000.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(16000, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(1400.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(2, prize.getPrizeItems().size());
        PrizeItemDto normalPrizeItem = prize.getPrizeItems().get(0);
        assertEquals("RA-1", normalPrizeItem.getGameInstance().getGameId());
        assertEquals("11001", normalPrizeItem.getGameInstance().getNumber());
        PrizeLevelItemDto levelItemDto = normalPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = normalPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(1000000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(920000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("4", levelItemDto.getPrizeLevel());
        // assert object
        PrizeLevelObjectItemDto object = levelItemDto.getPrizeLevelObjectItems().get(0);
        assertEquals(2, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);

        // assert lucky prize items
        PrizeItemDto luckyPrizeItem = prize.getPrizeItems().get(1);
        assertEquals("LD-1", luckyPrizeItem.getGameInstance().getGameId());
        assertEquals("001", luckyPrizeItem.getGameInstance().getNumber());
        levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(1000000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(920000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("4", levelItemDto.getPrizeLevel());
        // assert object
        object = levelItemDto.getPrizeLevelObjectItems().get(0);
        assertEquals(2, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);
    }

    @Test
    public void testEnquiry_TaxWhenPayout_BasedPerTicket_NewPrint_MultiDraw_LuckyDraw_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + ",TAX_CALCULATION_BASED=" + Game.TAXMETHOD_BASE_TICKET);

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(4666666.66, prize.getActualAmount().doubleValue(), 0);
        assertEquals(5600000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(933333.34, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(16000, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(1400.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(2, prize.getPrizeItems().size());
        PrizeItemDto normalPrizeItem = prize.getPrizeItems().get(0);
        assertEquals("RA-1", normalPrizeItem.getGameInstance().getGameId());
        assertEquals("11001", normalPrizeItem.getGameInstance().getNumber());
        PrizeLevelItemDto levelItemDto = normalPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(0, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(0, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = normalPrizeItem.getPrizeLevelItems().get(1);
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

        // assert lucky prize items
        PrizeItemDto luckyPrizeItem = prize.getPrizeItems().get(1);
        assertEquals("LD-1", luckyPrizeItem.getGameInstance().getGameId());
        assertEquals("001", luckyPrizeItem.getGameInstance().getNumber());
        levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(0);
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
        object = levelItemDto.getPrizeLevelObjectItems().get(0);
        assertEquals(2, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);
    }

    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_NewPrint_SingleDraw_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("delete from RA_TE_TICKET where id in ('2','3')");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(2480000.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(2800000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(8000, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(700.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(1, prize.getPrizeItems().size());
        PrizeItemDto normalPrizeItem = prize.getPrizeItems().get(0);
        assertEquals("RA-1", normalPrizeItem.getGameInstance().getGameId());
        assertEquals("11001", normalPrizeItem.getGameInstance().getNumber());
        PrizeLevelItemDto levelItemDto = normalPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = normalPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(1000000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(920000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("4", levelItemDto.getPrizeLevel());
        // assert object
        PrizeLevelObjectItemDto object = levelItemDto.getPrizeLevelObjectItems().get(0);
        assertEquals(2, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);
    }

    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_Refund_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        // update payotu mode to refund
        this.jdbcTemplate.update("update RA_OPERATION_PARAMETERS set PAYOUT_MODEL=0");
        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(2480700.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(2800000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_REFUND, prize.getPayoutMode());
        assertEquals(8000, prize.getLuckyPrizeAmount().doubleValue(), 0);
        assertEquals(700.0, prize.getReturnAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(1400.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(1, prize.getPrizeItems().size());
        PrizeItemDto normalPrizeItem = prize.getPrizeItems().get(0);
        assertEquals("RA-1", normalPrizeItem.getGameInstance().getGameId());
        assertEquals("11001", normalPrizeItem.getGameInstance().getNumber());
        PrizeLevelItemDto levelItemDto = normalPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = normalPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(1000000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(920000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("4", levelItemDto.getPrizeLevel());
        // assert object
        PrizeLevelObjectItemDto object = levelItemDto.getPrizeLevelObjectItems().get(0);
        assertEquals(2, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);
    }

    /**
     * Wins only lucky draw(level#1-cash, and level#4-cash+object)
     */
    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_NewPrint_MultiDraw_OnlyLuckyDraw_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from RA_WINNING_OBJECT");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(2480000.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(2800000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(8000.0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(1400.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(1, prize.getPrizeItems().size());

        // assert lucky prize items
        PrizeItemDto luckyPrizeItem = prize.getPrizeItems().get(0);
        assertEquals("LD-1", luckyPrizeItem.getGameInstance().getGameId());
        assertEquals("001", luckyPrizeItem.getGameInstance().getNumber());
        this.sortPrizeLevelItemDto(luckyPrizeItem.getPrizeLevelItems());
        PrizeLevelItemDto levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(1000000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(920000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("4", levelItemDto.getPrizeLevel());
        // assert object
        PrizeLevelObjectItemDto object = levelItemDto.getPrizeLevelObjectItems().get(0);
        assertEquals(2, object.getNumberOfObject());
        assertEquals(500, object.getTaxAmount().doubleValue(), 0);
        assertEquals(2000, object.getPrice().doubleValue(), 0);
    }

    /**
     * Wins only lucky draw(level#1-cash, and level#4-object)
     */
    @Test
    public void testEnquiry_TaxOnWinnerAnalysis_NewPrint_MultiDraw_OnlyLuckyDraw_2_OK() throws Exception {
        printMethod();
        RaffleTicket ticket = new RaffleTicket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_ANALYSIS);
        this.jdbcTemplate.update("delete from RA_WINNING_OBJECT");
        this.jdbcTemplate.update("delete from BD_PRIZE_LEVEL_ITEM where id='PLI-4'");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(640000.0, prize.getActualAmount().doubleValue(), 0);
        assertEquals(800000.0, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(160000.0, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET, prize.getPayoutMode());
        assertEquals(8000.0, prize.getLuckyPrizeAmount().doubleValue(), 0);
        // assert ticket
        BaseTicket winningTicket = prize.getWinningTicket();
        assertEquals(1400.0, winningTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getRawSerialNo(), winningTicket.getRawSerialNo());

        // assert prize item one by one
        assertEquals(1, prize.getPrizeItems().size());

        // assert lucky prize items
        PrizeItemDto luckyPrizeItem = prize.getPrizeItems().get(0);
        assertEquals("LD-1", luckyPrizeItem.getGameInstance().getGameId());
        assertEquals("001", luckyPrizeItem.getGameInstance().getNumber());
        this.sortPrizeLevelItemDto(luckyPrizeItem.getPrizeLevelItems());
        PrizeLevelItemDto levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(0);
        assertEquals(400000, levelItemDto.getPrizeAmount().doubleValue(), 0);
        assertEquals(320000, levelItemDto.getActualAmount().doubleValue(), 0);
        assertEquals(80000, levelItemDto.getTaxAmount().doubleValue(), 0);
        assertEquals(2, levelItemDto.getNumberOfPrizeLevel());
        assertEquals("1", levelItemDto.getPrizeLevel());
        levelItemDto = luckyPrizeItem.getPrizeLevelItems().get(1);
        assertEquals(0, levelItemDto.getPrizeAmount().doubleValue(), 0);
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
}
