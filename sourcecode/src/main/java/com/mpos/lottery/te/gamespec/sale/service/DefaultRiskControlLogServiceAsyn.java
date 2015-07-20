package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.sale.RiskControlLog;
import com.mpos.lottery.te.gamespec.sale.dao.RiskControlLogDao;
import com.mpos.lottery.te.sequence.service.UUIDService;

public class DefaultRiskControlLogServiceAsyn implements RiskControlLogServiceAsyn {
    private UUIDService uuidService;
    private RiskControlLogDao riskControlLogDao;

    @Override
    public RiskControlLog createDefault(String gameInstanceId, String selectedNumber, int prizeLevelType)
            throws ApplicationException {
        RiskControlLog log = new RiskControlLog();
        log.setId(this.getUuidService().getGeneralID());
        log.setGameInstanceId(gameInstanceId);
        log.setSelectedNumber(selectedNumber);
        log.setPrizeLevelType(prizeLevelType);
        this.getRiskControlLogDao().insert(log);
        return log;
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public RiskControlLogDao getRiskControlLogDao() {
        return riskControlLogDao;
    }

    public void setRiskControlLogDao(RiskControlLogDao riskControlLogDao) {
        this.riskControlLogDao = riskControlLogDao;
    }

}
