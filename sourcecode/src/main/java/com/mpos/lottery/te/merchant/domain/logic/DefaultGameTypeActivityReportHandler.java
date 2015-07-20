package com.mpos.lottery.te.merchant.domain.logic;

import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.GameTypeAware;
import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.merchant.service.ActivityReportService;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;
import java.util.List;

public class DefaultGameTypeActivityReportHandler
        implements
            GameTypeActivityReportHandler,
            InitializingBean,
            GameTypeAware {
    private Log logger = LogFactory.getLog(DefaultGameTypeActivityReportHandler.class);
    private GameType supportedGameType;
    private ActivityReportDao activityReportDao;
    private ActivityReportService activityReportService;

    /**
     * Register a <code>GameTypeActivityReportHandler</code> to <code>DefaultActivityReportService</code>.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.getActivityReportService().registerHandler(this.supportedGameType(), this);
    }

    @Override
    public GameType supportedGameType() {
        return this.supportedGameType;
    }

    @Override
    public List<DailyActivityReport> enquiry(Context respCtx, String operatorId, Date startTime, Date endTime) {
        List<DailyActivityReport> dailyReports = this.getActivityReportDao().findActivityReport(operatorId, startTime,
                endTime);

        return dailyReports;
    }

    public ActivityReportService getActivityReportService() {
        return activityReportService;
    }

    public void setActivityReportService(ActivityReportService activityReportService) {
        this.activityReportService = activityReportService;
    }

    /**
     * Support SPRING injection.
     */
    public void setSupportedGameType(GameType supportedGameType) {
        this.supportedGameType = supportedGameType;
    }

    public ActivityReportDao getActivityReportDao() {
        return activityReportDao;
    }

    public void setActivityReportDao(ActivityReportDao activityReportDao) {
        this.activityReportDao = activityReportDao;
    }

}
