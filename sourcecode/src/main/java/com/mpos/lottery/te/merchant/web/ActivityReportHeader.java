package com.mpos.lottery.te.merchant.web;

import java.util.LinkedList;
import java.util.List;

public class ActivityReportHeader {
    private List<ActivityReportItem> reportItems = new LinkedList<ActivityReportItem>();

    public ActivityReportItem getReportItemByTransType(int transType, boolean createIfNull) {
        ActivityReportItem report = null;
        for (ActivityReportItem item : this.reportItems) {
            if (transType == item.getTransType()) {
                report = item;
            }
        }
        if (createIfNull && report == null) {
            report = new ActivityReportItem();
            report.setTransType(transType);
            this.reportItems.add(report);
            return report;
        }
        return report;
    }

    public List<ActivityReportItem> getReportItems() {
        return reportItems;
    }

    public void setReportItems(List<ActivityReportItem> reportItems) {
        this.reportItems = reportItems;
    }

}
