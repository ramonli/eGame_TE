package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.dao.OperationParameterDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantGameDrawDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicketSerialNo;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.List;

public abstract class AbstractCriteriaActiveStrategy implements CriteriaActiveStrategy {
    protected Log logger = LogFactory.getLog(AbstractCriteriaActiveStrategy.class);
    private InstantTicketDao instantTicketDao;
    private InstantGameDrawDao instantGameDrawDao;
    private OperationParameterDao<IgOperationParameter> operationParameterDao;

    protected ActiveResult batchActive(String beginSerial, String endSerial, Transaction transaction) {
        ActiveResult result = new ActiveResult();
        List<InstantTicket> tickets = this.getInstantTicketDao().getByRangeSerialNo(beginSerial, endSerial);
        if (tickets.size() == 0) {
            logger.warn("No instant tickets found between " + beginSerial + " and " + endSerial);
            result.setErrorCode(SystemException.CODE_NO_TICKETSFOUND);
            result.setBatchSuccessful(false);
            return result;
        }

        if (tickets != null) {
            for (InstantTicket ticket : tickets) {
                try {
                    this.beforeActive(ticket);
                } catch (ApplicationException e) {
                    // If one ticket failed, the whole batch will fail
                    result.setFailure(ticket.getSerialNo());
                    result.setErrorCode(e.getErrorCode());
                    result.setBatchSuccessful(false);
                    // write warning message
                    logger.warn(e.getMessage(), e);
                    break;
                }
            }
            if (result.isBatchSuccessful()) {
                result.setCount(tickets.size());
                // update instant tickts
                for (InstantTicket ticket : tickets) {
                    ticket.setStatus(InstantTicket.STATUS_ACTIVE);

                    // added by Lee,2010-07-21,system optimization
                    ticket.setOperatorId(transaction.getOperatorId());
                    ticket.setMerchantId(transaction.getMerchantId() + "");
                    ticket.setDevId(transaction.getDeviceId() + "");
                    ticket.setSoldTime(new Date());
                    ticket.setSaleTransId(transaction.getId());

                    this.getInstantTicketDao().update(ticket);
                }
            }
        }
        return result;
    }

    protected IgOperationParameter getIgOperationParameter(String gameInstanceName) throws ApplicationException {
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

    /**
     * Generate a instant ticket serial from source InstantTicketSerialNo.
     */
    protected String assembleSerialNo(InstantTicketSerialNo serialNo, long index) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(serialNo.getGGG()).append(serialNo.getBBB());
        buffer.append(InstantTicketSerialNo.getStringIndex(index));
        return buffer.toString();
    }

    /**
     * Check if the ticket can be actived.
     */
    protected void beforeActive(InstantTicket hostTicket) throws ApplicationException {
        // check if the ticket is damaged
        if (InstantTicket.PHYSICAL_STATUS_DAMAGED == hostTicket.getPhysicalStatus()) {
            throw new ApplicationException(SystemException.CODE_ONE_DAMAGETICKET, "LottoTicket(serialNo="
                    + hostTicket.getSerialNo() + ") is damaged.");
        }
        // check if the ticket is sold to distributor
        if (InstantTicket.STATUS_SOLD != hostTicket.getStatus()
                && InstantTicket.PHYSICAL_STATUS_STOCKOUT != hostTicket.getPhysicalStatus()) {
            throw new ApplicationException(SystemException.CODE_ACTIVE_NOSOLDTICKET, "Instant ticket(serialNo="
                    + hostTicket.getSerialNo() + ") is neither sold to distributor nor in stock, can NOT active it.");
        }
        if (InstantTicket.STATUS_ACTIVE == hostTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_DULPLICATED_ACTIVE, "Instant ticket(serialNo="
                    + hostTicket.getSerialNo() + ") has been actived.");
        }
        // add by Lee 2010-07-16,add gameDraw's suspendActive valid condition
        // || hostTicket.isSuspendActivation()
        // is suspend activation function(1--YES,0--NO)
        if (hostTicket.getGameDraw().getIsSuspendActiveBlocked() == 1) {
            throw new ApplicationException(SystemException.CODE_TICKET_BLOCKPAYOUT, "Instant Game(DrawNo='"
                    + hostTicket.getGameDraw().getName() + "') has been 'activation blocked'.");
        }
        if (hostTicket.isInBlacklist()) {
            throw new ApplicationException(SystemException.CODE_TICKET_INBLACKLIST, "Instant ticket(serialNo="
                    + hostTicket.getSerialNo() + ") is in blacklist, it will be blocked.");
        }
        InstantGameDraw gameDraw = hostTicket.getGameDraw();
        if (LottoGameInstance.STATE_INACTIVE == gameDraw.getStatus()) {
            throw new ApplicationException(SystemException.CODE_NOT_ACTIVE_DRAW, "Instant game draw(id="
                    + gameDraw.getId() + ") is inactive, can not active " + "ticket(serialNo="
                    + hostTicket.getSerialNo() + ").");
        }
        Date current = new Date();
        // check start and stop activation time
        if (gameDraw.getStartActivationTime() != null && current.before(gameDraw.getStartActivationTime())) {
            throw new ApplicationException(SystemException.CODE_NOTACTIVATIONTIME, "The instant ticket(serialNo="
                    + hostTicket.getSerialNo() + ") can't be actived before" + gameDraw.getStartActivationTime());
        }
        if (gameDraw.getStopActivationTime() != null && current.after(gameDraw.getStopActivationTime())) {
            throw new ApplicationException(SystemException.CODE_NOTACTIVATIONTIME, "The instant ticket(serialNo="
                    + hostTicket.getSerialNo() + ") can't be actived after" + gameDraw.getStopActivationTime());
        }
    }

    public InstantTicketDao getInstantTicketDao() {
        return instantTicketDao;
    }

    public void setInstantTicketDao(InstantTicketDao instantTicketDao) {
        this.instantTicketDao = instantTicketDao;
    }

    public OperationParameterDao<IgOperationParameter> getOperationParameterDao() {
        return operationParameterDao;
    }

    public void setOperationParameterDao(OperationParameterDao<IgOperationParameter> operationParameterDao) {
        this.operationParameterDao = operationParameterDao;
    }

    public InstantGameDrawDao getInstantGameDrawDao() {
        return instantGameDrawDao;
    }

    public void setInstantGameDrawDao(InstantGameDrawDao instantGameDrawDao) {
        this.instantGameDrawDao = instantGameDrawDao;
    }

}
