package com.mpos.lottery.te.merchant.service.impl;

import com.mpos.lottery.te.config.dao.SysConfigurationDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.merchant.dao.ActivityReportDao;
import com.mpos.lottery.te.merchant.domain.logic.GameTypeActivityReportHandler;
import com.mpos.lottery.te.merchant.service.ActivityReportService;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultActivityReportService implements ActivityReportService {
    private Log logger = LogFactory.getLog(DefaultActivityReportService.class);
    private Map<GameType, GameTypeActivityReportHandler> gameTypeActivityReportHandlerMap = new HashMap<GameType, GameTypeActivityReportHandler>();
    private SysConfigurationDao sysConfigurationDao;
    private ActivityReportDao payoutDao;
    private ActivityReportDao commissionDao;
    private ActivityReportDao destinationActivityReportDao;
    private ActivityReportDao generalActivityReportDao;
    private ActivityReportDao transferActivityReportDao;

    @Override
    public void registerHandler(GameType gameType, GameTypeActivityReportHandler handler) {
        if (this.gameTypeActivityReportHandlerMap.get(gameType) != null) {
            throw new SystemException("A handler(" + this.gameTypeActivityReportHandlerMap.get(gameType)
                    + ") has registered with game type(" + gameType + "), however another handler(" + handler
                    + ") try to register with same game type.");
        }
        this.gameTypeActivityReportHandlerMap.put(gameType, handler);
    }

    @Override
    public ActivityReport query(@SuppressWarnings("rawtypes") Context context, ActivityReport request)
            throws ApplicationException {
        this.verifyTimeCriteria(context, request);
        // initial report
        ActivityReport report = request;

        // handle sale activity report
        Set<GameType> gameTypes = this.gameTypeActivityReportHandlerMap.keySet();
        for (GameType gameType : gameTypes) {
            // handle the sale one by one
            List<DailyActivityReport> dailyActivityReports = this.gameTypeActivityReportHandlerMap.get(gameType)
                    .enquiry(context, request.getOperatorId(), request.getStartTime(), request.getEndTime());
            report.mergeDailyActivityReports(dailyActivityReports);
            for (DailyActivityReport dailyReport : dailyActivityReports) {
                if (logger.isDebugEnabled()) {
                    logger.debug("DailyActivityReport of " + gameType + ":" + dailyReport);
                }
            }
        }

        // handle payout activity report
        List<DailyActivityReport> payoutReports = this.getPayoutDao().findActivityReport(context.getOperatorId(),
                request.getStartTime(), request.getEndTime());
        report.mergeDailyActivityReports(payoutReports);
        for (DailyActivityReport dailyReport : payoutReports) {
            if (logger.isDebugEnabled()) {
                logger.debug("DailyActivityReport of Payout:" + dailyReport);
            }
        }

        // such as Operator Cash Out By Pass,Operator Cash Out Manually,Cashout of customer,cashout
        // withdraw etc
        List<DailyActivityReport> destinationActivityReports = destinationActivityReportDao.findActivityReport(
                context.getOperatorId(), request.getStartTime(), request.getEndTime());
        report.mergeDailyActivityReports(destinationActivityReports);
        for (DailyActivityReport dailyReport : payoutReports) {
            if (logger.isDebugEnabled()) {
                logger.debug("DailyActivityReport of destination Operator Cash Out By Pass,Operator Cash Out "
                        + "Manually,Cashout of customer,cashout withdraw:" + dailyReport);
            }
        }

        // such as Operator Cash Out By Pass,Operator Cash Out Manually,Income Balance Transfer, Top up
        // by
        // voucher,cashout withdraw,Cashout of customer etc
        List<DailyActivityReport> generalActivityReports = generalActivityReportDao.findActivityReport(
                context.getOperatorId(), request.getStartTime(), request.getEndTime());
        report.mergeDailyActivityReports(generalActivityReports);
        for (DailyActivityReport dailyReport : payoutReports) {
            if (logger.isDebugEnabled()) {
                logger.debug("DailyActivityReport of Cash Out By Pass,Operator Cash Out Manually,Income "
                        + "Balance Transfer,Top up by voucher,cashout withdraw,Cashout of customer:" + dailyReport);
            }
        }

        // handle generate transactions which has no relationship with game,
        // such as Operator Cash Out By Pass,Operator Cash Out Manually,Income Balance Transfer etc
        List<DailyActivityReport> commissionReports = this.getCommissionDao().findActivityReport(
                context.getOperatorId(), request.getStartTime(), request.getEndTime());
        report.mergeDailyActivityReports(commissionReports);
        for (DailyActivityReport dailyReport : commissionReports) {
            if (logger.isDebugEnabled()) {
                logger.debug("DailyActivityReport of Commission:" + dailyReport);
            }
        }


        //Need to invoke transferActivityReportDao to handle balance transfer transaction
        List<DailyActivityReport> transferReports = this.getTransferActivityReportDao().findActivityReport(
                context.getOperatorId(), request.getStartTime(), request.getEndTime());
        report.mergeDailyActivityReports(transferReports);
        for (DailyActivityReport dailyReport : transferReports) {
            if (logger.isDebugEnabled()) {
                logger.debug("DailyActivityReport of Commission:" + dailyReport);
            }
        }
       
        

        return report;
    }

    // --------------------------------------------------------
    // PROTECTED METHODS
    // --------------------------------------------------------

    /**
     * Check the query criteria, includes:
     * <ul>
     * <li>startTime must be earlier than endTime.</li>
     * <li>startTime can't be earlier than max allowed querying days.</li>
     * </ul>
     */
    protected void verifyTimeCriteria(Context context, ActivityReport request) throws ApplicationException {
        if (request.getOperatorId() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No operator provided in request body, the X-Operator-Id(" + context.getOperatorId()
                        + ") will be used.");
            }
            request.setOperatorId(context.getOperatorId());
        }
        // endTime must be later than startTime
        if (!request.getEndTime().after(request.getStartTime())) {
            throw new ApplicationException(SystemException.CODE_STARTTIME_LATER_THAN_ENDTIME, "The start time("
                    + request.getStartTime() + ") can't be later than end time(" + request.getEndTime() + ").");
        }
        long allowedQueeryDays = this.getSysConfigurationDao().getSysConfiguration()
                .getMaxAllowedDaysOfActivityReport();
        if (request.getEndTime().getTime() - request.getStartTime().getTime() > allowedQueeryDays * 24 * 3600 * 1000) {
            throw new ApplicationException(SystemException.CODE_EXCEED_MAX_ALLOWED_QUERY_DAYS, "Can only query max "
                    + allowedQueeryDays + " days' report.");
        }
    }

    // --------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------------

    public SysConfigurationDao getSysConfigurationDao() {
        return sysConfigurationDao;
    }

    public void setSysConfigurationDao(SysConfigurationDao sysConfigurationDao) {
        this.sysConfigurationDao = sysConfigurationDao;
    }

    public ActivityReportDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(ActivityReportDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    /**
     * @return commissionDao
     */
    public ActivityReportDao getCommissionDao() {
        return commissionDao;
    }

    /**
     * @param commissionDao
     */
    public void setCommissionDao(ActivityReportDao commissionDao) {
        this.commissionDao = commissionDao;
    }

    /**
     * @return destinationActivityReportDao
     */
    public ActivityReportDao getDestinationActivityReportDao() {
        return destinationActivityReportDao;
    }

    /**
     * @param destinationActivityReportDao
     */
    public void setDestinationActivityReportDao(ActivityReportDao destinationActivityReportDao) {
        this.destinationActivityReportDao = destinationActivityReportDao;
    }

    /**
     * @return generalActivityReportDao
     */
    public ActivityReportDao getGeneralActivityReportDao() {
        return generalActivityReportDao;
    }

    /**
     * @param generalActivityReportDao
     */
    public void setGeneralActivityReportDao(ActivityReportDao generalActivityReportDao) {
        this.generalActivityReportDao = generalActivityReportDao;
    }

    public ActivityReportDao getTransferActivityReportDao() {
        return transferActivityReportDao;
    }

    public void setTransferActivityReportDao(ActivityReportDao transferActivityReportDao) {
        this.transferActivityReportDao = transferActivityReportDao;
    }
}
