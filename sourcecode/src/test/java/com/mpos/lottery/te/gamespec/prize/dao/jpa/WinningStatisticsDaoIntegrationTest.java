package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.lotto.prize.domain.WinningStatistics;
import com.mpos.lottery.te.gamespec.prize.dao.BaseWinningStatisticsDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import javax.annotation.Resource;

/**
 * Reload etc/db/oracle_testdata.sql before running test.
 */
public class WinningStatisticsDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "baseWinningStatisticsDao")
    private BaseWinningStatisticsDao winningStatDao;

    @Test
    public void testGetByGameDraw_Lotto() {
        WinningStatistics stats = this.getWinningStatDao().getByGameDrawAndPrizeLevelAndVersion(
                WinningStatistics.class, "GII-111", 5, 1);
        assertEquals(22, stats.getNumberOfPrize());
        assertEquals(10000.0, stats.getPrizeAmount().doubleValue(), 0);
    }

    public BaseWinningStatisticsDao getWinningStatDao() {
        return winningStatDao;
    }

    public void setWinningStatDao(BaseWinningStatisticsDao winningStatDao) {
        this.winningStatDao = winningStatDao;
    }

}
