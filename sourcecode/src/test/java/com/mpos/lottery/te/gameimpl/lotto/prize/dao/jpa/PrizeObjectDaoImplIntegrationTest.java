package com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.gameimpl.lotto.prize.dao.PrizeObjectDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.LuckyWinningItem;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.PrizeObject;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import javax.annotation.Resource;

public class PrizeObjectDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "prizeObjectDao")
    private PrizeObjectDao prizeObjectDao;

    @Test
    public void testFindByPrizeLogicAndLevel() throws Exception {
        PrizeObject po = this.getPrizeObjectDao().findByPrizeLogicAndLevel("OPL-1", 2,
                LuckyWinningItem.WINNING_TYPE_NORMAL, 1);
        assertNotNull(po);
        assertEquals("BPO-2", po.getId());
        assertEquals("XBox360", po.getName());
        assertEquals(4500.0, po.getPrizeAmount().doubleValue(), 0);
        assertEquals(1000.0, po.getTax().doubleValue(), 0);

        po = this.getPrizeObjectDao().findByPrizeLogicAndLevel("OPL-1", 9, LuckyWinningItem.WINNING_TYPE_NORMAL, 1);
        assertNull(po);
    }

    public PrizeObjectDao getPrizeObjectDao() {
        return prizeObjectDao;
    }

    public void setPrizeObjectDao(PrizeObjectDao prizeObjectDao) {
        this.prizeObjectDao = prizeObjectDao;
    }

}
