package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.merchant.web.ActivityReportItem;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.merchant.web.GameActivityReport;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

public class PayoutDaoImplTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "payoutDao")
    private ActivityReportDao payoutDao;

    @Test
    public void testFindActivityReport() {
        // 5 days before
        Date begin = new Date(new Date().getTime() - 3 * 24 * 3600 * 1000);
        Date end = new Date(new Date().getTime() + 2 * 24 * 3600 * 1000);
        List<DailyActivityReport> dailyReports = this.getPayoutDao().findActivityReport("OPERATOR-111", begin, end);
        assertEquals(1, dailyReports.size());

        // assert payout daily report
        DailyActivityReport dailyReport = dailyReports.get(0);
        ActivityReportItem reportItem = dailyReport.getReportItemByTransType(TransactionType.PAYOUT.getRequestType(),
                false);
        assertEquals(11530, reportItem.getAmount().doubleValue(), 0);
        assertEquals(80, reportItem.getTax().doubleValue(), 0);
        assertEquals(7, reportItem.getNumberOfTrans());
    }

    protected GameActivityReport getGameActivityReport(String gameId, List<GameActivityReport> gameReports) {
        for (GameActivityReport gameReport : gameReports) {
            if (gameId.equals(gameReport.getGameId())) {
                return gameReport;
            }
        }
        return null;
    }

    public ActivityReportDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(ActivityReportDao payoutDao) {
        this.payoutDao = payoutDao;
    }

}
