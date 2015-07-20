package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.gamespec.prize.PrizeLevel;
import com.mpos.lottery.te.gamespec.prize.PrizeLevelItem;
import com.mpos.lottery.te.gamespec.prize.dao.PrizeLevelDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class PrizeLevelDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "prizeLevelDao")
    private PrizeLevelDao prizeLevelDao;

    @Test
    public void testFindByPrizeLogicAndLevel() {
        PrizeLevel prizeLevel = this.getPrizeLevelDao().findByPrizeLogicAndLevel("OPL-1", 4);
        assertEquals(2, prizeLevel.getLevelItems().size());
        List<PrizeLevelItem> items = prizeLevel.getItemByPrizeType(PrizeLevel.PRIZE_TYPE_CASH);
        assertEquals(1, items.size());
        PrizeLevelItem item = items.get(0);
        assertEquals(1000000.0, item.getPrizeAmount().doubleValue(), 0);
        assertEquals(80000.0, item.getTaxAmount().doubleValue(), 0);
        assertEquals(920000.0, item.getActualAmount().doubleValue(), 0);
        assertNull(item.getObjectId());
        assertNull(item.getObjectName());

        item = prizeLevel.getItemByPrizeType(PrizeLevel.PRIZE_TYPE_OBJECT).get(0);
        assertEquals(2000.0, item.getPrizeAmount().doubleValue(), 0);
        assertEquals(500.0, item.getTaxAmount().doubleValue(), 0);
        assertEquals(1500.0, item.getActualAmount().doubleValue(), 0);
        assertEquals("BPO-3", item.getObjectId());
        assertEquals("Sony Camera", item.getObjectName());
    }

    public PrizeLevelDao getPrizeLevelDao() {
        return prizeLevelDao;
    }

    public void setPrizeLevelDao(PrizeLevelDao prizeLevelDao) {
        this.prizeLevelDao = prizeLevelDao;
    }

}
