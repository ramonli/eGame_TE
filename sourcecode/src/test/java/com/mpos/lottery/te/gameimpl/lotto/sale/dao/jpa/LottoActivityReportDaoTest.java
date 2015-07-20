package com.mpos.lottery.te.gameimpl.lotto.sale.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.merchant.web.ActivityReportItem;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

public class LottoActivityReportDaoTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "lottoActivityReportDao")
    private ActivityReportDao activityReportDao;

    @Test
    public void testEnquiryActivityReport() {
        Date end = new Date();
        // 5 days before
        Date begin = new Date(end.getTime() - 5 * 24 * 3600 * 1000);
        end = new Date(end.getTime() + 1 * 24 * 3600 * 1000);

        List<DailyActivityReport> gameReports = this.getActivityReportDao().findActivityReport("OPERATOR-111", begin,
                end);
        assertEquals(2, gameReports.size());
        this.sortDailyActivityReport(gameReports);

        // assert statistics of yesterday
        DailyActivityReport dailyReport = gameReports.get(0);
        assertEquals(1, dailyReport.getReportItems().size());
        // sale
        ActivityReportItem reportItem = dailyReport.getReportItemByTransType(
                TransactionType.SELL_TICKET.getRequestType(), false);
        assertEquals(1, reportItem.getNumberOfTrans());
        assertEquals(200.0, reportItem.getAmount().doubleValue(), 0);

        // assert today statistics
        dailyReport = gameReports.get(1);
        assertEquals(4, dailyReport.getReportItems().size());
        // sale
        reportItem = dailyReport.getReportItemByTransType(TransactionType.SELL_TICKET.getRequestType(), false);
        assertEquals(3, reportItem.getNumberOfTrans());
        assertEquals(10100.5, reportItem.getAmount().doubleValue(), 0);
        // cancel by transaction
        reportItem = dailyReport
                .getReportItemByTransType(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), false);
        assertEquals(1, reportItem.getNumberOfTrans());
        assertEquals(600, reportItem.getAmount().doubleValue(), 0);
        // cancel by transaction
        reportItem = dailyReport.getReportItemByTransType(TransactionType.CANCEL_BY_TICKET.getRequestType(), false);
        assertEquals(1, reportItem.getNumberOfTrans());
        assertEquals(2000.1, reportItem.getAmount().doubleValue(), 0);
        // cancel decline
        reportItem = dailyReport.getReportItemByTransType(TransactionType.CANCEL_DECLINED.getRequestType(), false);
        assertEquals(2, reportItem.getNumberOfTrans());
        assertEquals(1500.1, reportItem.getAmount().doubleValue(), 0);
    }

    @Test
    public void testEnquiryActivityReport_Today() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateString = sdf.format(new Date());

        sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date begin = sdf.parse(dateString + "000000");
        Date end = sdf.parse(dateString + "235959");

        List<DailyActivityReport> gameReports = this.getActivityReportDao().findActivityReport("OPERATOR-111", begin,
                end);
        assertEquals(1, gameReports.size());
        DailyActivityReport dailyReport = gameReports.get(0);
        assertEquals(4, dailyReport.getReportItems().size());
        // sale
        ActivityReportItem reportItem = dailyReport.getReportItemByTransType(
                TransactionType.SELL_TICKET.getRequestType(), false);
        assertEquals(3, reportItem.getNumberOfTrans());
        assertEquals(10100.5, reportItem.getAmount().doubleValue(), 0);
        // cancel by transaction
        reportItem = dailyReport
                .getReportItemByTransType(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), false);
        assertEquals(1, reportItem.getNumberOfTrans());
        assertEquals(600, reportItem.getAmount().doubleValue(), 0);
        // cancel by transaction
        reportItem = dailyReport.getReportItemByTransType(TransactionType.CANCEL_BY_TICKET.getRequestType(), false);
        assertEquals(1, reportItem.getNumberOfTrans());
        assertEquals(2000.1, reportItem.getAmount().doubleValue(), 0);
        // cancel decline
        reportItem = dailyReport.getReportItemByTransType(TransactionType.CANCEL_DECLINED.getRequestType(), false);
        assertEquals(2, reportItem.getNumberOfTrans());
        assertEquals(1500.1, reportItem.getAmount().doubleValue(), 0);
    }

    public ActivityReportDao getActivityReportDao() {
        return activityReportDao;
    }

    public void setActivityReportDao(ActivityReportDao activityReportDao) {
        this.activityReportDao = activityReportDao;
    }

}
