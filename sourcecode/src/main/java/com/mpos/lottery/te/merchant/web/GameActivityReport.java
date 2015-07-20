package com.mpos.lottery.te.merchant.web;

import com.mpos.lottery.te.config.exception.SystemException;

import java.util.LinkedList;
import java.util.List;

public class GameActivityReport {
    private String gameId;
    private int gameType;
    private List<ActivityReportItem> reportItems = new LinkedList<ActivityReportItem>();

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public List<ActivityReportItem> getReportItems() {
        return reportItems;
    }

    public void setReportItems(List<ActivityReportItem> reportItems) {
        this.reportItems = reportItems;
    }

    public ActivityReportItem getReportItemByTransType(int transType) {
        return this.getReportItemByTransType(transType, false);
    }

    public ActivityReportItem getReportItemByTransType(int transType, boolean createIfNull) {
        ActivityReportItem reportItem = null;
        for (ActivityReportItem item : this.reportItems) {
            if (transType == item.getTransType()) {
                reportItem = item;
            }
        }
        if (createIfNull && reportItem == null) {
            reportItem = new ActivityReportItem();
            reportItem.setTransType(transType);
            this.reportItems.add(reportItem);
        }

        return reportItem;
    }

    public void addReportItem(ActivityReportItem reportItem) {
        for (ActivityReportItem item : this.reportItems) {
            if (reportItem.getTransType() == item.getTransType()) {
                throw new SystemException("You can't add report item with same trans type:" + item.getTransType()
                        + " into a activity report of game(id=" + this.gameId + ").");
            }
        }
        this.reportItems.add(reportItem);
    }

    public void addReportItems(List<ActivityReportItem> reportItems) {
        for (ActivityReportItem item : reportItems) {
            this.addReportItem(item);
        }
    }
}
