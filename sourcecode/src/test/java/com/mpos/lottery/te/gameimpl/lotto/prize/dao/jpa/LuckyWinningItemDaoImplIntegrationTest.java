package com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.gameimpl.lotto.prize.dao.LuckyWinningItemDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.LuckyWinningItem;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class LuckyWinningItemDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "luckyWinningItemDao")
    private LuckyWinningItemDao luckyWinningItemDao;

    @Test
    public void testFindByTicket() throws Exception {
        List<LuckyWinningItem> items = this.getLuckyWinningItemDao().findByTicketAndGameDraw(
                BaseTicket.encryptSerialNo("S-123456"), "GII-111", 1);
        assertEquals(2, items.size());
        LuckyWinningItem item = items.get(0);
        assertEquals("WO-1", item.getId());
        assertEquals(1, item.getVersion());
        assertEquals("GII-111", item.getGameInstanceId());
        assertEquals(1, item.getPrizeLevel());
        assertEquals(1, item.getNumberOfLevel());
        assertNull(item.getPrizeObject());
    }

    public LuckyWinningItemDao getLuckyWinningItemDao() {
        return luckyWinningItemDao;
    }

    public void setLuckyWinningItemDao(LuckyWinningItemDao luckyWinningItemDao) {
        this.luckyWinningItemDao = luckyWinningItemDao;
    }

}
