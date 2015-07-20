package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import com.mpos.lottery.te.config.dao.OperationParameterDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantGameDrawDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantVIRNPrizeDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gamespec.prize.dao.PrizeLevelDao;
import com.mpos.lottery.te.trans.service.TransactionRetryLogServiceAsyn;

/**
 * A proxy for a variety of validation strategy implementations.
 */
public class ValidationStrategyProxy implements ValidationStrategy {
    private InstantVIRNPrizeDao instantVIRNPrizeDao;
    private TransactionRetryLogServiceAsyn transactionRetryLogService;
    private PrizeLevelDao prizeLevelDao;
    private InstantGameDrawDao instantGameDrawDao;
    private OperationParameterDao<IgOperationParameter> operationParameterDao;

    public PrizeLevelDto validate(InstantTicket ticket, String virn, boolean isEnquiry) throws ApplicationException {
        ValidationStrategy strategy = null;
        int validationType = ticket.getGameDraw().getValidationType();
        if (validationType == InstantGameDraw.VALIDATION_TYPE_EGAME) {
            strategy = new EGameValidationStrategy();
        } else if (validationType == InstantGameDraw.VALIDATION_TYPE_VIRN) {
            strategy = new VIRNValidationStrategy();
        } else {
            throw new SystemException("Unsupportted validation type:" + validationType);
        }

        ((AbstractValidationStrategy) strategy).setPrizeLevelDao(this.getPrizeLevelDao());
        ((AbstractValidationStrategy) strategy).setTransactionRetryLogService(this.getTransactionRetryLogService());
        ((AbstractValidationStrategy) strategy).setInstantGameDrawDao(this.getInstantGameDrawDao());
        ((AbstractValidationStrategy) strategy).setOperationParameterDao(this.getOperationParameterDao());
        ((AbstractValidationStrategy) strategy).setInstantVIRNPrizeDao(this.getInstantVIRNPrizeDao());
        return strategy.validate(ticket, virn, isEnquiry);
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

    public PrizeLevelDao getPrizeLevelDao() {
        return prizeLevelDao;
    }

    public void setPrizeLevelDao(PrizeLevelDao prizeLevelDao) {
        this.prizeLevelDao = prizeLevelDao;
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

}
