package com.mpos.lottery.te.gameimpl.magic100.game;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.web.GameDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDtos;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class Magic100GameInstanceIntegrationTest extends BaseServletIntegrationTest {

    // @Rollback(false)
    @Test
    public void testEnquiryByGameTpye() throws Exception {
        printMethod();

        this.jdbcTemplate.update("delete from LK_GAME_INSTANCE where ID='GII-112'");

        GameInstanceDto reqDto = new GameInstanceDto();
        reqDto.setGameType(Game.TYPE_LUCKYNUMBER);
        Context ctx = this.getDefaultContext(TransactionType.GAME_DRAW_ENQUIRY.getRequestType(), reqDto);
        Context respCtx = doPost(this.mockRequest(ctx));
        GameInstanceDtos gameInstanceDtos = (GameInstanceDtos) respCtx.getModel();

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        assertEquals(1, gameInstanceDtos.getGameDtos().size());
        GameDto gameDto = gameInstanceDtos.getGameDtos().get(0);
        assertEquals(100.0, gameDto.getBaseAmount().doubleValue(), 0);
        assertEquals("LK-1", gameDto.getId());
        assertEquals(Game.TYPE_LUCKYNUMBER, gameDto.getGameType().intValue());

        assertEquals(1, gameDto.getGameInstanceDtos().size());
        GameInstanceDto returnedGameInstanceDto = gameDto.getGameInstanceDtos().get(0);
        assertEquals(BaseGameInstance.STATE_ACTIVE, returnedGameInstanceDto.getState());
        assertEquals("001", returnedGameInstanceDto.getNumber());
    }

    @Test
    public void testEnquiryByGame() throws Exception {
        printMethod();

        GameInstanceDto reqDto = new GameInstanceDto();
        reqDto.setGameType(Game.TYPE_LUCKYNUMBER);
        reqDto.setGameId("LK-1");
        Context ctx = this.getDefaultContext(TransactionType.GAME_DRAW_ENQUIRY.getRequestType(), reqDto);
        Context respCtx = doPost(this.mockRequest(ctx));
        GameInstanceDtos gameInstanceDtos = (GameInstanceDtos) respCtx.getModel();

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        assertEquals(1, gameInstanceDtos.getGameDtos().size());
        GameDto gameDto = gameInstanceDtos.getGameDtos().get(0);
        assertEquals(100.0, gameDto.getBaseAmount().doubleValue(), 0);
        assertEquals("LK-1", gameDto.getId());
        assertEquals(Game.TYPE_LUCKYNUMBER, gameDto.getGameType().intValue());

        assertEquals(1, gameDto.getGameInstanceDtos().size());
        GameInstanceDto returnedGameInstanceDto = gameDto.getGameInstanceDtos().get(0);
        assertEquals(BaseGameInstance.STATE_ACTIVE, returnedGameInstanceDto.getState());
        assertEquals("001", returnedGameInstanceDto.getNumber());
    }

    @Test
    public void testEnquiryByGame_NoMerchantSupport() throws Exception {
        printMethod();

        // remove game merchant relationship
        this.jdbcTemplate.update("delete from game_merchant");

        GameInstanceDto reqDto = new GameInstanceDto();
        reqDto.setGameType(Game.TYPE_LUCKYNUMBER);
        reqDto.setGameId("LK-1");
        Context ctx = this.getDefaultContext(TransactionType.GAME_DRAW_ENQUIRY.getRequestType(), reqDto);
        Context respCtx = doPost(this.mockRequest(ctx));
        GameInstanceDtos gameInstanceDtos = (GameInstanceDtos) respCtx.getModel();

        // assert response
        assertEquals(SystemException.CODE_NO_GAMEDRAW, respCtx.getResponseCode());
    }

    @Test
    public void testEnquiryByNumber() throws Exception {
        printMethod();

        GameInstanceDto reqDto = new GameInstanceDto();
        reqDto.setGameType(Game.TYPE_LUCKYNUMBER);
        reqDto.setGameId("LK-1");
        reqDto.setNumber("001");
        Context ctx = this.getDefaultContext(TransactionType.GAME_DRAW_ENQUIRY.getRequestType(), reqDto);
        Context respCtx = doPost(this.mockRequest(ctx));
        GameInstanceDtos gameInstanceDtos = (GameInstanceDtos) respCtx.getModel();

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        assertEquals(1, gameInstanceDtos.getGameDtos().size());
        GameDto gameDto = gameInstanceDtos.getGameDtos().get(0);
        assertEquals(100.0, gameDto.getBaseAmount().doubleValue(), 0);
        assertEquals("LK-1", gameDto.getId());
        assertEquals(Game.TYPE_LUCKYNUMBER, gameDto.getGameType().intValue());

        assertEquals(1, gameDto.getGameInstanceDtos().size());
        GameInstanceDto returnedGameInstanceDto = gameDto.getGameInstanceDtos().get(0);
        assertEquals(BaseGameInstance.STATE_ACTIVE, returnedGameInstanceDto.getState());
        assertEquals("001", returnedGameInstanceDto.getNumber());
    }
}
