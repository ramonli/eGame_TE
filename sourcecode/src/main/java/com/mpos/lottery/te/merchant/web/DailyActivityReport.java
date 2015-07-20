package com.mpos.lottery.te.merchant.web;

import com.google.gson.Gson;

import java.util.Date;

public class DailyActivityReport extends ActivityReportHeader {
    private String date;
    private Date startTime;
    private Date endTime;

    // private List<GameActivityReport> gameActivityReports = new ArrayList<GameActivityReport>();
    // private GeneralActivityReport generalActivityReport;
    //
    // public GameActivityReport getReportByGame(String gameId, boolean createIfNull) {
    // GameActivityReport report = null;
    // for (GameActivityReport item : this.gameActivityReports) {
    // if (gameId.equalsIgnoreCase(item.getGameId()))
    // report = item;
    // }
    // if (createIfNull && report == null) {
    // report = new GameActivityReport();
    // report.setGameId(gameId);
    // this.gameActivityReports.add(report);
    // return report;
    // }
    // return report;
    // }
    //
    // public void addGameActivityReport(GameActivityReport reportItem) {
    // for (GameActivityReport item : this.gameActivityReports) {
    // if (reportItem.getGameId().equals(item.getGameId()))
    // throw new SystemException("Can't add game-activity-report with same gameId:"
    // + reportItem.getGameId() + " into a activity report.");
    // }
    // this.gameActivityReports.add(reportItem);
    // }
    //
    // public void addGameActivityReports(List<GameActivityReport> reportItems) {
    // for (GameActivityReport item : reportItems)
    // this.addGameActivityReport(item);
    // }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    // public List<GameActivityReport> getGameActivityReports() {
    // return gameActivityReports;
    // }
    //
    // public void setGameActivityReports(List<GameActivityReport> gameActivityReports) {
    // this.gameActivityReports = gameActivityReports;
    // }
    //
    // public GeneralActivityReport getGeneralActivityReport() {
    // return generalActivityReport;
    // }
    //
    // public void setGeneralActivityReport(GeneralActivityReport generalActivityReport) {
    // this.generalActivityReport = generalActivityReport;
    // }
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
