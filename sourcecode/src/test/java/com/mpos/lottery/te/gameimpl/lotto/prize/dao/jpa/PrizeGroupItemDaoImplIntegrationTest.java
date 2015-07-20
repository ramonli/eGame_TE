package com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;
import com.mpos.lottery.te.gamespec.prize.dao.PrizeGroupItemDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class PrizeGroupItemDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "prizeGroupItemDao")
    private PrizeGroupItemDao prizeGroupItemDao;

    @Test
    public void testFindByGroup_Cash() {
        List<PrizeGroupItem> items = this.getPrizeGroupItemDao().findByGroupAndGameTypeAndGroupType("BPG-1",
                Game.TYPE_LOTT, PrizeGroupItem.GROUP_TYPE_NORMAL_DRAW);
        assertEquals(8, items.size());
        PrizeGroupItem item = items.get(0);
        assertEquals("LOTTO-15", item.getId());
        assertEquals("BPG-1", item.getPrizeGroupId());
        assertEquals(1, item.getPrizeLevel());
        assertEquals(Game.TYPE_LOTT, item.getGameType());
    }

    @Test
    public void testFindByGroup_Object() {
        List<PrizeGroupItem> items = this.getPrizeGroupItemDao().findByGroupAndGameTypeAndGroupType("BPG-1",
                Game.TYPE_INSTANT, PrizeGroupItem.GROUP_TYPE_NORMAL_DRAW);
        assertEquals(6, items.size());
        PrizeGroupItem item = items.get(0);
        assertEquals("IG-8", item.getId());
        assertEquals("BPG-1", item.getPrizeGroupId());
        assertEquals(1, item.getPrizeLevel());
        assertEquals(Game.TYPE_INSTANT, item.getGameType());
    }

    public PrizeGroupItemDao getPrizeGroupItemDao() {
        return prizeGroupItemDao;
    }

    public void setPrizeGroupItemDao(PrizeGroupItemDao prizeGroupItemDao) {
        this.prizeGroupItemDao = prizeGroupItemDao;
    }

}
