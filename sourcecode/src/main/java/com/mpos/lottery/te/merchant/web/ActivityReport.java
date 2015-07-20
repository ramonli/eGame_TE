package com.mpos.lottery.te.merchant.web;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityReport {
    private String operatorId;
    // private String barcode;
    private Date startTime;
    private Date endTime;
    private ActivityReportHeader statReport = new ActivityReportHeader();
    private List<DailyActivityReport> dailyActivityReports = new ArrayList<DailyActivityReport>();

    public DailyActivityReport getReportByDate(String date, boolean createIfNull) {
        DailyActivityReport report = null;
        for (DailyActivityReport item : this.dailyActivityReports) {
            if (date.equalsIgnoreCase(item.getDate())) {
                report = item;
            }
        }
        if (createIfNull && report == null) {
            report = new DailyActivityReport();
            report.setDate(date);
            this.dailyActivityReports.add(report);
            return report;
        }
        return report;
    }

    /**
     * Merge the provided <code>DailyActivityReport</code> into the activity report.
     */
    public void mergeDailyActivityReport(DailyActivityReport dailyReport) {
        Assert.notNull(dailyReport);
        DailyActivityReport existDailyReport = this.getReportByDate(dailyReport.getDate(), false);
        if (existDailyReport == null) {
            existDailyReport = dailyReport;
            this.getDailyActivityReports().add(existDailyReport);
        } else {
            this.doMergeDailyActivityReport(dailyReport, existDailyReport);
        }

        // update activity report header as well.
        this.doMergeDailyActivityReport(dailyReport, this.getStatReport());
    }

    public void mergeDailyActivityReports(List<DailyActivityReport> dailyReports) {
        for (DailyActivityReport dailyReport : dailyReports) {
            this.mergeDailyActivityReport(dailyReport);
        }
    }

    protected void doMergeDailyActivityReport(DailyActivityReport newDailyReport, ActivityReportHeader existDailyReport) {
        for (ActivityReportItem reportItem : newDailyReport.getReportItems()) {
            ActivityReportItem existReportItem = existDailyReport.getReportItemByTransType(reportItem.getTransType(),
                    false);
            if (existReportItem == null) {
                existReportItem = new ActivityReportItem(reportItem);
                existDailyReport.getReportItems().add(existReportItem);
            } else {
                existReportItem.setAmount(existReportItem.getAmount().add(reportItem.getAmount()));
                existReportItem.setTax(existReportItem.getTax().add(reportItem.getTax()));
                existReportItem.setNumberOfTrans(existReportItem.getNumberOfTrans() + reportItem.getNumberOfTrans());
                existReportItem.setCommission(existReportItem.getCommission().add(reportItem.getCommission()));
            }
        }
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public ActivityReportHeader getStatReport() {
        return statReport;
    }

    public void setStatReport(ActivityReportHeader statReport) {
        this.statReport = statReport;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<DailyActivityReport> getDailyActivityReports() {
        return dailyActivityReports;
    }

    public void setDailyActivityReports(List<DailyActivityReport> dailyActivityReports) {
        this.dailyActivityReports = dailyActivityReports;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

}
