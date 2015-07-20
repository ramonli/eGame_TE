package com.mpos.lottery.te.merchant.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.merchant.domain.logic.GameTypeActivityReportHandler;
import com.mpos.lottery.te.merchant.web.ActivityReport;
import com.mpos.lottery.te.port.Context;

public interface ActivityReportService {

    void registerHandler(GameType gameType, GameTypeActivityReportHandler handler);

    /**
     * The retailer may consult the activity reports on their terminal, pls refer to TE interface document '#4.7.8
     * Activity Report' for more information.
     * 
     * @param request
     *            The request object, fields 'beginTime' and 'endTime' must be set.
     * @return a activity report.
     * @throws ApplicationException
     *             when encounter any application exception.
     */
    ActivityReport query(Context context, ActivityReport request) throws ApplicationException;
}
