package com.mpos.lottery.te.gamespec.game.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class JpaBaseGameInstanceDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "baseGameInstanceDao")
    private BaseGameInstanceDao baseGameInstanceDao;

    @Test
    public void testLookupActiveByGame() {
        this.printMethod();
        List<LottoGameInstance> gameInstances = this.getBaseGameInstanceDao().lookupActiveByGame(111,
                LottoGameInstance.class, "GAME-111");
        assertEquals(1, gameInstances.size());
        assertEquals("GII-113", gameInstances.get(0).getId());
    }

    @Test
    public void testLookupActiveByGameType() {
        this.printMethod();
        List<LottoGameInstance> gameInstances = this.getBaseGameInstanceDao().lookupActiveByGameType(111,
                LottoGameInstance.class);
        assertEquals(1, gameInstances.size());
        assertEquals("GII-113", gameInstances.get(0).getId());
    }

    @Test
    public void testLookupActiveByGameType_NoGameAllocated() {
        this.printMethod();
        List<LottoGameInstance> gameInstances = this.getBaseGameInstanceDao().lookupActiveByGameType(112,
                LottoGameInstance.class);
        assertEquals(0, gameInstances.size());
    }

    @Test
    public void testLookupByGameAndNumber() {
        this.printMethod();
        LottoGameInstance gameInstance = this.getBaseGameInstanceDao().lookupByGameAndNumber(111,
                LottoGameInstance.class, "GAME-111", "20090408");
        assertNotNull(gameInstance);
        assertEquals("GII-113", gameInstance.getId());
    }

    @Test
    public void testLookupByGameAndNumber_NoGameAllocated() {
        this.printMethod();
        LottoGameInstance gameInstance = this.getBaseGameInstanceDao().lookupByGameAndNumber(112,
                LottoGameInstance.class, "GAME-111", "20090408");
        assertNull(gameInstance);
    }

    @Test
    public void testLookupFutureByGameAndNumber() throws Exception {
        this.printMethod();
        List<LottoGameInstance> gameInstances = this.getBaseGameInstanceDao().lookupFutureByGameAndNumber(111,
                LottoGameInstance.class, "GAME-111", "20090408", 2);
        assertEquals(2, gameInstances.size());
        assertEquals("GII-113", gameInstances.get(0).getId());
    }

    public BaseGameInstanceDao getBaseGameInstanceDao() {
        return baseGameInstanceDao;
    }

    public void setBaseGameInstanceDao(BaseGameInstanceDao baseGameInstanceDao) {
        this.baseGameInstanceDao = baseGameInstanceDao;
    }

}
