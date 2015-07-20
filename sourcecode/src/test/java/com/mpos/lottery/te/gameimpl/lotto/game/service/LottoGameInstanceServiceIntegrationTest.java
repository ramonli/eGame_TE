package com.mpos.lottery.te.gameimpl.lotto.game.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.workingkey.domain.Gpe;

import org.junit.Test;

import javax.annotation.Resource;

public class LottoGameInstanceServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "lottoGameInstanceService")
    private GameInstanceService gameInstanceService;

    @Test
    public void testEnquiryByGameAndDrawNO_Stopped_Channel() throws Exception {
        this.jdbcTemplate.update("update BD_CHANNEL_SETTING_ITEM set STOP_SELLING_TIME=1620 where ID='IGPE-1'");

        Context respContext = new Context();
        Merchant merchant = new Merchant();
        merchant.setId(111);
        respContext.setMerchant(merchant);
        Gpe gpe = new Gpe();
        gpe.setType(Gpe.TYPE_IGPE);
        respContext.setGpe(gpe);

        try {
            this.getGameInstanceService().enquirySaleReady(respContext, "GAME-111", "20090408", 1);
            fail("Should get exception code:" + SystemException.CODE_SALE_STOPPED_CHANNEL);
        } catch (ApplicationException e) {
            assertEquals(SystemException.CODE_SALE_STOPPED_CHANNEL, e.getErrorCode());
        }
    }

    @Test
    public void testEnquiryByGameAndDrawNO_SuspendGameInstance() throws Exception {
        this.jdbcTemplate.update("update GAME_INSTANCE set IS_SUSPEND_SALE=1 where GAME_INSTANCE_ID='GII-113'");

        Context respContext = new Context();
        Merchant merchant = new Merchant();
        merchant.setId(111);
        respContext.setMerchant(merchant);
        Gpe gpe = new Gpe();
        gpe.setType(Gpe.TYPE_IGPE);
        respContext.setGpe(gpe);

        try {
            this.getGameInstanceService().enquirySaleReady(respContext, "GAME-111", "20090408", 1);
            fail("Should get exception code:" + SystemException.CODE_SUSPENDED_GAME_INSTANCE);
        } catch (ApplicationException e) {
            assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, e.getErrorCode());
        }
    }

    @Test
    public void testEnquiryByGameAndDrawNO_InactiveGameInstance() throws Exception {
        this.jdbcTemplate.update("update GAME_INSTANCE set STATUS=" + BaseGameInstance.STATE_PAYOUT_STARTED
                + " where GAME_INSTANCE_ID='GII-113'");

        Context respContext = new Context();
        Merchant merchant = new Merchant();
        merchant.setId(111);
        respContext.setMerchant(merchant);
        Gpe gpe = new Gpe();
        gpe.setType(Gpe.TYPE_IGPE);
        respContext.setGpe(gpe);

        try {
            this.getGameInstanceService().enquirySaleReady(respContext, "GAME-111", "20090408", 1);
            fail("Should get exception code:" + SystemException.CODE_NOT_ACTIVE_DRAW);
        } catch (ApplicationException e) {
            assertEquals(SystemException.CODE_NOT_ACTIVE_DRAW, e.getErrorCode());
        }
    }

    @Test
    public void testEnquiryByGameAndDrawNO_InactiveGame() throws Exception {
        this.jdbcTemplate.update("update GAME set STATUS=" + Game.STATUS_INACTIVE);

        Context respContext = new Context();
        Merchant merchant = new Merchant();
        merchant.setId(111);
        respContext.setMerchant(merchant);
        Gpe gpe = new Gpe();
        gpe.setType(Gpe.TYPE_IGPE);
        respContext.setGpe(gpe);

        try {
            this.getGameInstanceService().enquirySaleReady(respContext, "GAME-111", "20090408", 1);
            fail("Should get exception code:" + SystemException.CODE_GAME_INACTIVE);
        } catch (ApplicationException e) {
            assertEquals(SystemException.CODE_GAME_INACTIVE, e.getErrorCode());
        }
    }

    public GameInstanceService getGameInstanceService() {
        return gameInstanceService;
    }

    public void setGameInstanceService(GameInstanceService gameInstanceService) {
        this.gameInstanceService = gameInstanceService;
    }

}
