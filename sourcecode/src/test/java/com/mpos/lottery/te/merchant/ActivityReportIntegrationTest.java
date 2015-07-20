package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.merchant.web.ActivityReportHeader;
import com.mpos.lottery.te.merchant.web.ActivityReportItem;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ActivityReportIntegrationTest extends BaseServletIntegrationTest {

    /**
     * <ActivityReport endTime="20140420145322" operatorId="OPERATOR-111" startTime="20140415145322"> <StatActivity>
     * <ReportItem amount="23800.9" number="10" tax="0" type="200" commission="1"/> <ReportItem amount="1200" number="2"
     * tax="0" type="206" commission="0"/> <ReportItem amount="3000.2" number="4" tax="0" type="204" commission="0"/>
     * <ReportItem amount="4000.2" number="2" tax="0" type="201" commission="0"/> <ReportItem amount="11530" number="7"
     * tax="80" type="302" commission="2"/> <ReportItem type="447" number="1" amount="100" tax="0" commission="5"/>
     * <ReportItem type="453" number="1" amount="100" tax="0" commission="5"/> </StatActivity> <DailyActivity
     * date="20140415"> <ReportItem amount="5800" number="3" tax="0" type="200" commission="0"/> </DailyActivity>
     * <DailyActivity date="20140417"> <ReportItem amount="200" number="1" tax="0" type="200" commission="0"/>
     * </DailyActivity> <DailyActivity date="20140418"> <ReportItem amount="17800.9" number="6" tax="0" type="200"
     * commission="1"/> <ReportItem amount="1200" number="2" tax="0" type="206" commission="0"/> <ReportItem
     * amount="3000.2" number="4" tax="0" type="204" commission="0"/> <ReportItem amount="4000.2" number="2" tax="0"
     * type="201" commission="0"/> <ReportItem amount="11530" number="7" tax="80" type="302" commission="2"/>
     * <ReportItem type="447" number="1" amount="100" tax="0" commission="5"/> <ReportItem type="353" number="1"
     * amount="100" tax="0" commission="5"/> </DailyActivity> </ActivityReport>
     * 
     * @throws Exception
     */
    @Test
    public void testEnquiryActivityReport() throws Exception {
        printMethod();
        ActivityReport dto = mock();

        Context reqCtx = this.getDefaultContext(TransactionType.ACTIVITY_REPORT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        ActivityReport respDto = (ActivityReport) respCtx.getModel();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        assertEquals(3, respDto.getDailyActivityReports().size());

        // verify header
        ActivityReportHeader reportHeader = respDto.getStatReport();
        assertEquals(10, reportHeader.getReportItems().size());
        ActivityReportItem headerReportItem = reportHeader.getReportItemByTransType(
                TransactionType.SELL_TICKET.getRequestType(), false);
        assertEquals(23800.9, headerReportItem.getAmount().doubleValue(), 0);
        // assertEquals(26400.9, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(11, headerReportItem.getNumberOfTrans());
        // assertEquals(12, headerReportItem.getNumberOfTrans());
        assertEquals(1, headerReportItem.getCommission().doubleValue(), 0.0);
        headerReportItem = reportHeader.getReportItemByTransType(
                TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), false);
        assertEquals(1200, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(2, headerReportItem.getNumberOfTrans());
        assertEquals(0, headerReportItem.getCommission().doubleValue(), 0.0);
        headerReportItem = reportHeader.getReportItemByTransType(TransactionType.CANCEL_BY_TICKET.getRequestType(),
                false);
        assertEquals(4000.2, headerReportItem.getAmount().doubleValue(), 0);
        // assertEquals(6600.2, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(2, headerReportItem.getNumberOfTrans());
        // assertEquals(3, headerReportItem.getNumberOfTrans());
        assertEquals(0, headerReportItem.getCommission().doubleValue(), 0.0);
        headerReportItem = reportHeader.getReportItemByTransType(TransactionType.CANCEL_DECLINED.getRequestType(),
                false);
        assertEquals(3000.2, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(4, headerReportItem.getNumberOfTrans());
        assertEquals(0, headerReportItem.getCommission().doubleValue(), 0.0);
        headerReportItem = reportHeader.getReportItemByTransType(TransactionType.PAYOUT.getRequestType(), false);
        assertEquals(11530, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(80, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(7, headerReportItem.getNumberOfTrans());
        assertEquals(2, headerReportItem.getCommission().doubleValue(), 0.0);
        headerReportItem = reportHeader.getReportItemByTransType(
                TransactionType.CASH_OUT_OPERATOR_MANUAL.getRequestType(), false);
        assertEquals(100, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(1, headerReportItem.getNumberOfTrans());
        assertEquals(5, headerReportItem.getCommission().doubleValue(), 0.0);
        headerReportItem = reportHeader.getReportItemByTransType(
                TransactionType.OPERATOR_TOPUP_VOUCHER.getRequestType(), false);
        assertEquals(100, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(1, headerReportItem.getNumberOfTrans());
        assertEquals(5, headerReportItem.getCommission().doubleValue(), 0.0);

        // verify daily reports

        List<DailyActivityReport> dailyReports = respDto.getDailyActivityReports();
        this.sortDailyActivityReport(dailyReports);
        DailyActivityReport dailyReport = dailyReports.get(0);
        assertEquals(1, dailyReport.getReportItems().size());
        ActivityReportItem reportItem = dailyReport.getReportItemByTransType(
                TransactionType.SELL_TICKET.getRequestType(), false);
        assertEquals(5800, reportItem.getAmount().doubleValue(), 0);
        assertEquals(0, reportItem.getTax().doubleValue(), 0);
        assertEquals(4, reportItem.getNumberOfTrans());
        assertEquals(0, reportItem.getCommission().doubleValue(), 0);

        dailyReport = dailyReports.get(1);
        assertEquals(1, dailyReport.getReportItems().size());
        reportItem = dailyReport.getReportItemByTransType(TransactionType.SELL_TICKET.getRequestType(), false);
        assertEquals(200, reportItem.getAmount().doubleValue(), 0);
        assertEquals(0, reportItem.getTax().doubleValue(), 0);
        assertEquals(1, reportItem.getNumberOfTrans());
        assertEquals(0, reportItem.getCommission().doubleValue(), 0);

        dailyReport = dailyReports.get(2);
        assertEquals(10, dailyReport.getReportItems().size());
        reportItem = dailyReport.getReportItemByTransType(TransactionType.SELL_TICKET.getRequestType(), false);
        assertEquals(17800.9, reportItem.getAmount().doubleValue(), 0);
        // assertEquals(20400.9, reportItem.getAmount().doubleValue(), 0);
        assertEquals(0, reportItem.getTax().doubleValue(), 0);
        assertEquals(6, reportItem.getNumberOfTrans());
        // assertEquals(7, reportItem.getNumberOfTrans());
        assertEquals(1, reportItem.getCommission().doubleValue(), 0);
        reportItem = dailyReport
                .getReportItemByTransType(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), false);
        assertEquals(1200, reportItem.getAmount().doubleValue(), 0);
        assertEquals(0, reportItem.getTax().doubleValue(), 0);
        assertEquals(2, reportItem.getNumberOfTrans());
        assertEquals(0, reportItem.getCommission().doubleValue(), 0);
        reportItem = dailyReport.getReportItemByTransType(TransactionType.CANCEL_BY_TICKET.getRequestType(), false);
        assertEquals(4000.2, reportItem.getAmount().doubleValue(), 0);
        // assertEquals(6600.2, reportItem.getAmount().doubleValue(), 0);
        assertEquals(0, reportItem.getTax().doubleValue(), 0);
        assertEquals(2, reportItem.getNumberOfTrans());
        // assertEquals(3, reportItem.getNumberOfTrans());
        assertEquals(0, reportItem.getCommission().doubleValue(), 0);
        reportItem = dailyReport.getReportItemByTransType(TransactionType.CANCEL_DECLINED.getRequestType(), false);
        assertEquals(3000.2, reportItem.getAmount().doubleValue(), 0);
        assertEquals(0, reportItem.getTax().doubleValue(), 0);
        assertEquals(4, reportItem.getNumberOfTrans());
        assertEquals(0, reportItem.getCommission().doubleValue(), 0);
        reportItem = dailyReport.getReportItemByTransType(TransactionType.PAYOUT.getRequestType(), false);
        assertEquals(11530, reportItem.getAmount().doubleValue(), 0);
        assertEquals(80, reportItem.getTax().doubleValue(), 0);
        assertEquals(7, reportItem.getNumberOfTrans());
        assertEquals(2, reportItem.getCommission().doubleValue(), 0);
        reportItem = dailyReport.getReportItemByTransType(TransactionType.CASH_OUT_OPERATOR_MANUAL.getRequestType(),
                false);
        assertEquals(100, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(1, headerReportItem.getNumberOfTrans());
        assertEquals(5, headerReportItem.getCommission().doubleValue(), 0.0);
        reportItem = dailyReport.getReportItemByTransType(TransactionType.OPERATOR_TOPUP_VOUCHER.getRequestType(),
                false);
        assertEquals(100, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(1, headerReportItem.getNumberOfTrans());
        assertEquals(5, headerReportItem.getCommission().doubleValue(), 0.0);

        reportItem = dailyReport.getReportItemByTransType(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), false);
        assertEquals(100, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(1, headerReportItem.getNumberOfTrans());
        assertEquals(5, headerReportItem.getCommission().doubleValue(), 0.0);
        
        reportItem = dailyReport.getReportItemByTransType(1161, false);
        assertEquals(100, headerReportItem.getAmount().doubleValue(), 0);
        assertEquals(0, headerReportItem.getTax().doubleValue(), 0);
        assertEquals(1, headerReportItem.getNumberOfTrans());
        assertEquals(5, headerReportItem.getCommission().doubleValue(), 0.0);
    }

    public static ActivityReport mock() {
        Date begin = new Date(new Date().getTime() - 3 * 24 * 3600 * 1000);
        Date end = new Date(new Date().getTime() + 2 * 24 * 3600 * 1000);

        ActivityReport dto = new ActivityReport();
        dto.setStartTime(begin);
        dto.setEndTime(end);
        return dto;
    }

    private static class ActivityReportItemComparator implements Comparator<ActivityReportItem> {

        @Override
        public int compare(ActivityReportItem o1, ActivityReportItem o2) {
            return o1.getTransType() - o2.getTransType();
        }

    }
}
