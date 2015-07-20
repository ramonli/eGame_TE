package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.lotto.prize.domain.WinningItem;
import com.mpos.lottery.te.gamespec.prize.dao.BaseWinningItemDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

/**
 * Reload etc/db/oracle_testdata.sql before running test.
 */
public class WinningItemDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "baseWinningItemDao")
    private BaseWinningItemDao winningItemDao;

    @Test
    public void testGetByTicketAndGameAndVersion_Lotto() {
        String serialNo = BaseTicket.encryptSerialNo("S-123456");
        List<WinningItem> items = this.getWinningItemDao().findByGameInstanceAndSerialNoAndVersion(WinningItem.class,
                "GII-111", serialNo, 1);
        assertEquals(3, items.size());
        WinningItem item = items.get(0);
        System.out.println(item);
    }

    public BaseWinningItemDao getWinningItemDao() {
        return winningItemDao;
    }

    public void setWinningItemDao(BaseWinningItemDao winningItemDao) {
        this.winningItemDao = winningItemDao;
    }

}
