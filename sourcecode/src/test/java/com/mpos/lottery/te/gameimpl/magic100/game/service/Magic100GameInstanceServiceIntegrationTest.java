package com.mpos.lottery.te.gameimpl.magic100.game.service;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.workingkey.domain.Gpe;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class Magic100GameInstanceServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "magic100GameInstanceService")
    private GameInstanceService gameInstanceService;

    @Test
    public void testEnquiryActive() throws Exception {
        Context ctx = new Context();
        Gpe gpe = new Gpe();
        gpe.setType(Gpe.TYPE_IGPE);
        ctx.setGpe(gpe);
        Merchant merchant = new Merchant();
        merchant.setId(111);
        ctx.setMerchant(merchant);

        List<? extends BaseGameInstance> gameInstances = this.getGameInstanceService().enquirySaleReady(ctx, "LK-1");
        Magic100GameInstance activeGameInstance = (Magic100GameInstance) gameInstances.get(0);
        assertEquals("GII-111", activeGameInstance.getId());
        assertEquals(BaseGameInstance.STATE_ACTIVE, activeGameInstance.getState());
    }

    public GameInstanceService getGameInstanceService() {
        return gameInstanceService;
    }

    public void setGameInstanceService(GameInstanceService gameInstanceService) {
        this.gameInstanceService = gameInstanceService;
    }

}
