package com.mpos.lottery.te.trans.service.impl;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.dao.OperationParameterDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.merchant.domain.Device;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.dao.TransactionRetryLogDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionRetryLog;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.service.TransactionRetryLogServiceAsyn;

import java.util.Date;

public class TransactionRetryLogServiceAsynImpl implements TransactionRetryLogServiceAsyn {
    private TransactionRetryLogDao transactionRetryLogDao;
    private OperationParameterDao operationParameterDao;
    private BaseJpaDao baseJpaDao;
    private UUIDService uuidManager;

    public TransactionRetryLog add(String ticketSerialNo, int transType, long deviceId) throws ApplicationException {
        TransactionRetryLog retryLog = new TransactionRetryLog();
        retryLog.setVersion(TransactionRetryLog.VERSION_VALID);
        retryLog.setId(this.getUuidManager().getGeneralID());
        retryLog.setCreateTime(new Date());
        retryLog.setTicketSerialNo(ticketSerialNo);
        retryLog.setTransType(TransactionType.VALIDATE_INSTANT_TICKET.getRequestType());
        retryLog.setTotalRetry(1);
        retryLog.setDeviceId(deviceId);
        this.getTransactionRetryLogDao().insert(retryLog);
        return retryLog;
    }

    public TransactionRetryLog getBySerialNoAndTransTypeAndDevice(String ticketSerialNo, int transType, long deviceId)
            throws ApplicationException {
        return this.getTransactionRetryLogDao().getByTicketAndTransTypeAndDevice(ticketSerialNo, transType, deviceId);
    }

    public void update(TransactionRetryLog retryLog) throws ApplicationException {
        this.getTransactionRetryLogDao().update(retryLog);
    }

    public TransactionRetryLog checkMaxValidationTimes(Transaction trans, String ticketSerialNo, int transType,
            Game game) throws ApplicationException {
        int timeRange = 1; // hour

        IgOperationParameter param = (IgOperationParameter) this.getOperationParameterDao().findById(
                IgOperationParameter.class, game.getOperatorParameterId());
        if (param == null) {
            throw new SystemException("can NOT find IG operation parameter by id(" + game.getOperatorParameterId()
                    + ").");
        }
        // retrieve exist log
        TransactionRetryLog retryLog = this.getBySerialNoAndTransTypeAndDevice(ticketSerialNo, transType,
                trans.getDeviceId());
        if (retryLog == null) {
            retryLog = this.add(ticketSerialNo, transType, trans.getDeviceId());
        } else {
            int maxValidationTimes = param.getMaxValidateTimes();
            // check current time, if (current-createtime) is greater than 1
            // hour
            Date current = new Date();
            if (SimpleToolkit.compare(current, retryLog.getCreateTime()) > (timeRange * 3600 * 1000)) {
                // exceed timeRange hour, reset transaction retry log.
                retryLog.setCreateTime(current);
                retryLog.setUpdateTime(current);
                retryLog.setTotalRetry(1);
                this.update(retryLog);
            } else {
                // if exceed max allowed times per hour, the request will be
                // denied.
                // It means you can try validation many time as long as they
                // don't occur in 1 hour.
                if (retryLog.getTotalRetry() >= maxValidationTimes) {
                    retryLog.setTotalRetry(retryLog.getTotalRetry() + 1);
                    // use 'version' column to represent a invalid(soft removed)
                    // log.
                    retryLog.setVersion(TransactionRetryLog.VERSION_INVALID);
                    this.update(retryLog);

                    // lock the device, it can be unlocked from M.lottery
                    // admin.
                    Device device = this.getBaseJpaDao().findById(Device.class, trans.getDeviceId());
                    if (device == null) {
                        throw new ApplicationException(SystemException.CODE_NO_DEVICE, "can NOT find device with id="
                                + trans.getDeviceId());
                    }
                    device.setStatus(Device.STATUS_BLOCKED);
                    this.getBaseJpaDao().update(device);
                } else {
                    retryLog.setTotalRetry(retryLog.getTotalRetry() + 1);
                    retryLog.setUpdateTime(current);
                    this.update(retryLog);
                }
            }
        }
        retryLog.setAllowedRetry(param.getMaxValidateTimes());
        return retryLog;
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------------

    public TransactionRetryLogDao getTransactionRetryLogDao() {
        return transactionRetryLogDao;
    }

    public void setTransactionRetryLogDao(TransactionRetryLogDao transactionRetryLogDao) {
        this.transactionRetryLogDao = transactionRetryLogDao;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

    public OperationParameterDao getOperationParameterDao() {
        return operationParameterDao;
    }

    public void setOperationParameterDao(OperationParameterDao operationParameterDao) {
        this.operationParameterDao = operationParameterDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

}
