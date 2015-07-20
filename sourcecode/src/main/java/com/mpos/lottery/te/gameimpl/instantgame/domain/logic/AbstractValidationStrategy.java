package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import com.mpos.lottery.te.config.dao.OperationParameterDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantGameDrawDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantVIRNPrizeDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicketSerialNo;
import com.mpos.lottery.te.gamespec.prize.dao.PrizeLevelDao;
import com.mpos.lottery.te.trans.domain.TransactionRetryLog;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.service.TransactionRetryLogServiceAsyn;

public abstract class AbstractValidationStrategy implements ValidationStrategy {
    private InstantVIRNPrizeDao instantVIRNPrizeDao;
    private TransactionRetryLogServiceAsyn transactionRetryLogService;
    private PrizeLevelDao prizeLevelDao;
    private InstantGameDrawDao instantGameDrawDao;
    private OperationParameterDao<IgOperationParameter> operationParameterDao;

    protected IgOperationParameter getIgOperationParameter(String ticketSerialNo) throws ApplicationException {
        InstantTicketSerialNo no = new InstantTicketSerialNo(ticketSerialNo);
        String gameInstanceName = no.getGGG();
        InstantGameDraw draw = this.getInstantGameDrawDao().getByName(gameInstanceName);
        if (draw == null) {
            throw new ApplicationException(SystemException.CODE_NO_GAMEDRAW,
                    "can NOT find IG game instance by instanceName=" + gameInstanceName);
        }
        String opParamId = draw.getGame().getOperatorParameterId();
        IgOperationParameter param = this.getOperationParameterDao().findById(IgOperationParameter.class, opParamId);
        if (param == null) {
            throw new SystemException("can NOT find default IG operation parameter(id=" + opParamId + ").");
        }
        return param;
    }

    protected void checkValidationRetry(InstantTicket hostTicket) throws ApplicationException {
        TransactionRetryLog retryLog = this.getTransactionRetryLogService().checkMaxValidationTimes(
                hostTicket.getTransaction(), hostTicket.getSerialNo(),
                TransactionType.VALIDATE_INSTANT_TICKET.getRequestType(), hostTicket.getGameDraw().getGame());
        if (retryLog.isExceedMaxValidationTimes()) {
            throw new ApplicationException(SystemException.CODE_EXCEED_MAX_VALIDATION_TIMES,
                    "exceed max allowed validation times(serialNo=" + hostTicket.getSerialNo()
                            + ", maxAllowedValidationCount=" + retryLog.getAllowedRetry() + ").");
        }
        if (retryLog.getAllowedRetry() == retryLog.getTotalRetry()) {
            // warn: only one retry time left
            throw new ApplicationException(SystemException.CODE_ONE_VALIDATIONCHANGE,
                    "Only one validation chance left(allowed=" + retryLog.getAllowedRetry() + ", current:"
                            + retryLog.getTotalRetry() + ").");
        }
    }

    public InstantVIRNPrizeDao getInstantVIRNPrizeDao() {
        return instantVIRNPrizeDao;
    }

    public void setInstantVIRNPrizeDao(InstantVIRNPrizeDao instantVIRNPrizeDao) {
        this.instantVIRNPrizeDao = instantVIRNPrizeDao;
    }

    public TransactionRetryLogServiceAsyn getTransactionRetryLogService() {
        return transactionRetryLogService;
    }

    public void setTransactionRetryLogService(TransactionRetryLogServiceAsyn transactionRetryLogService) {
        this.transactionRetryLogService = transactionRetryLogService;
    }

    public InstantGameDrawDao getInstantGameDrawDao() {
        return instantGameDrawDao;
    }

    public void setInstantGameDrawDao(InstantGameDrawDao instantGameDrawDao) {
        this.instantGameDrawDao = instantGameDrawDao;
    }

    public OperationParameterDao<IgOperationParameter> getOperationParameterDao() {
        return operationParameterDao;
    }

    public void setOperationParameterDao(OperationParameterDao<IgOperationParameter> operationParameterDao) {
        this.operationParameterDao = operationParameterDao;
    }

    public PrizeLevelDao getPrizeLevelDao() {
        return prizeLevelDao;
    }

    public void setPrizeLevelDao(PrizeLevelDao prizeLevelDao) {
        this.prizeLevelDao = prizeLevelDao;
    }

}
