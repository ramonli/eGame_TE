package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.merchant.web.DailyActivityReport;

import java.util.Date;
import java.util.List;

public interface ActivityReportDao {

    /**
     * Query activity report of special game type.
     */
    List<DailyActivityReport> findActivityReport(String operatorId, Date startTime, Date endTime);
}
