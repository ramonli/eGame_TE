package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.sale.RiskControlLog;

public interface RiskControlLogServiceAsyn {

    RiskControlLog createDefault(String gameInstanceId, String selectedNumber, int prizeLevelType)
            throws ApplicationException;
}
