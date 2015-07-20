package com.mpos.lottery.te.merchant.domain.logic;

import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.port.Context;

import java.util.Date;
import java.util.List;

public interface GameTypeActivityReportHandler {

    /**
     * Enquiry the activity report of sale related transaction of a given game type.
     * 
     */
    List<DailyActivityReport> enquiry(Context respCtx, String operatorId, Date startTime, Date endTime);
}
