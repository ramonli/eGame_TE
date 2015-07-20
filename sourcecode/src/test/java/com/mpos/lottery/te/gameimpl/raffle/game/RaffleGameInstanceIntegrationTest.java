package com.mpos.lottery.te.gameimpl.raffle.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.gameimpl.raffle.RaffleDomainMocker;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDtos;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class RaffleGameInstanceIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiryActiveByGameType_OK() throws Exception {
        this.printMethod();
        // make cancelByTicket request
        GameInstanceDto reqDto = new GameInstanceDto();
        reqDto.setGameType(Game.TYPE_RAFFLE);
        Context ctx = this.getDefaultContext(TransactionType.GAME_DRAW_ENQUIRY.getRequestType(), reqDto);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // this.setComplete();

        // assert outputs
        assertEquals(200, respCtx.getResponseCode());
        GameInstanceDtos dtos = (GameInstanceDtos) respCtx.getModel();
        assertTrue(dtos.getGameDtos().size() > 0);
    }

    @Test
    public void testEnquiryActiveByGame_OK() throws Exception {
        this.printMethod();
        // make cancelByTicket request
        GameInstanceDto reqDto = new GameInstanceDto();
        reqDto.setGameType(Game.TYPE_RAFFLE);
        reqDto.setGameId("RA-1");
        Context ctx = this.getDefaultContext(TransactionType.GAME_DRAW_ENQUIRY.getRequestType(), reqDto);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // this.setComplete();

        // assert outputs
        assertEquals(200, respCtx.getResponseCode());
        GameInstanceDtos dtos = (GameInstanceDtos) respCtx.getModel();
        assertEquals(1, dtos.getGameDtos().size());
        GameInstanceDto gameInstanceDto = dtos.getGameDtos().get(0).getGameInstanceDtos().get(0);
        assertEquals(BaseGameInstance.STATE_ACTIVE, gameInstanceDto.getState());
        assertEquals("11002", gameInstanceDto.getNumber());
    }

    @Test
    public void testEnquiryActiveByGameAndNumber_OK() throws Exception {
        this.printMethod();
        // make cancelByTicket request
        GameInstanceDto reqDto = new GameInstanceDto();
        reqDto.setGameType(Game.TYPE_RAFFLE);
        reqDto.setGameId("RA-1");
        reqDto.setNumber("11001");
        Context ctx = this.getDefaultContext(TransactionType.GAME_DRAW_ENQUIRY.getRequestType(), reqDto);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // this.setComplete();

        // assert outputs
        assertEquals(200, respCtx.getResponseCode());
        GameInstanceDtos dtos = (GameInstanceDtos) respCtx.getModel();
        assertEquals(1, dtos.getGameDtos().size());
        GameInstanceDto gameInstanceDto = dtos.getGameDtos().get(0).getGameInstanceDtos().get(0);
        assertEquals(BaseGameInstance.STATE_PAYOUT_STARTED, gameInstanceDto.getState());
        assertEquals("11001", gameInstanceDto.getNumber());
    }

    protected Context makeSale() throws Exception {
        RaffleTicket ticket = RaffleDomainMocker.ticket();
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("100"));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        return respCtx;
    }
}
