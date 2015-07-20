package com.mpos.lottery.te.trans.service.impl;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.SysConfiguration;
import com.mpos.lottery.te.config.dao.SysConfigurationDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.dao.GameDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.DummyTicket;
import com.mpos.lottery.te.gamespec.sale.service.TicketEnquiryService;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.thirdpartyservice.amqp.AmqpMessageUtils;
import com.mpos.lottery.te.thirdpartyservice.amqp.MessagePack;
import com.mpos.lottery.te.thirdpartyservice.amqp.TeTransactionMessage;
import com.mpos.lottery.te.trans.dao.PendingTransactionDao;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.dao.TransactionMessageDao;
import com.mpos.lottery.te.trans.domain.PendingTransaction;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionMessage;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.ReversalOrCancelStrategy;
import com.mpos.lottery.te.trans.domain.logic.ReversalOrCancelStrategyFactory;
import com.mpos.lottery.te.trans.domain.transactionhandle.TransactionHandle;
import com.mpos.lottery.te.trans.domain.transactionhandle.TransactionHandleFactory;
import com.mpos.lottery.te.trans.service.TransactionService;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;
import com.mpos.lottery.te.valueaddservice.airtime.dao.AirtimeDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Transaction record manager.
 */
public class TransactionServiceImpl implements TransactionService {
    private Log logger = LogFactory.getLog(TransactionServiceImpl.class);
    // the key of map should be game type.
    private Map<GameType, TicketEnquiryService> ticketEnquiryMap = new HashMap<GameType, TicketEnquiryService>();
    // ------------------------------------------------------
    // SPRING DEPENDENCIES
    // ------------------------------------------------------
    private UUIDService uuidManager;
    private TransactionDao transactionDao;
    private TransactionMessageDao transMessageDao;
    // private PayoutDao payoutDao;
    private BaseJpaDao baseJpaDao;
    private ReversalOrCancelStrategyFactory reversalStrategyFactory;
    private GameDao gameDao;
    private PendingTransactionDao pendingTransactionDao;
    private SysConfigurationDao sysConfigurationDao;
    @PersistenceContext
    private EntityManager entityManager;
    @Resource(name = "transactionHandleFactory")
    private TransactionHandleFactory transactionHandleFactory;

    @Override
    public void registerTicketEnquiry(GameType gameType, TicketEnquiryService ticketService) {
        TicketEnquiryService handler = this.ticketEnquiryMap.get(gameType);
        if (handler != null) {
            throw new SystemException("A ticket enquiry handler(" + handler + ") has been registered with game type("
                    + gameType + ").");
        }
        this.ticketEnquiryMap.put(gameType, ticketService);
    }

    /**
     * @see TransactionService#enquiry(Context, long, String).
     */
    public Transaction enquiry(Context respCtx, long terminalId, String traceMessageId) throws ApplicationException {
        assert traceMessageId != null : "argument 'traceMessageId' can NOT be null.";
        Transaction targetTrans = this.getTransactionDao().getByDeviceAndTraceMessage(terminalId, traceMessageId);
        if (targetTrans == null) {
            throw new ApplicationException(SystemException.CODE_NO_TRANSACTION, "can NOT find transaction(deviceId="
                    + terminalId + ",traceMessageId=" + traceMessageId + ").");
        }
        int gameTypeId = this.lookupGameType(respCtx, targetTrans);
        // Game type id >=1000,represents VAS transaction
        if (gameTypeId < 1000) {
            if (targetTrans.getTicketSerialNo() != null) {
                targetTrans.setTicket(getTransactionTicketByGameInfo(respCtx, targetTrans));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Transaction(deviceId=" + terminalId + ",traceMessageID=" + traceMessageId
                            + ") doesn't associate with a ticket.");
                }
            }
        } else {
            TransactionHandle transactionHandle = transactionHandleFactory.lookupHandle(gameTypeId);
            if (transactionHandle == null) {
                throw new SystemException("No transaction handle p" + "rovider found by given game type id("
                        + respCtx.getTransType() + ")");
            }
            Object transactionModel = transactionHandle.getTransactionModel(respCtx, targetTrans);
            targetTrans.setObject(transactionModel);
        }

        return targetTrans;
    }

    /**
     * @see com.mpos.lottery.te.trans.service.TransactionService#save(com.mpos.lottery.te.trans.domain.Transaction)
     */
    public void save(Transaction trans) throws ApplicationException {
        // check if a transaction with same(terminalId+traceMessageid) has
        // existed
        Transaction dbTrans = this.getTransactionDao().getByDeviceAndTraceMessage(trans.getDeviceId(),
                trans.getTraceMessageId());
        if (dbTrans != null) {
            throw new ApplicationException(SystemException.CODE_DULPLICATED_TRANSACTION, "The transaction(terminalId="
                    + trans.getDeviceId() + ",traceMessageId=" + trans.getTraceMessageId()
                    + ") has existed, it can NOT be dulplicated.");
        }

        this.getTransactionDao().insert(trans);
    }

    /**
     * @see com.mpos.lottery.te.trans.service.TransactionService#update(com.mpos.lottery.te.trans.domain.Transaction)
     */
    public void update(Transaction trans) throws ApplicationException {
        this.getTransactionDao().update(trans);
        this.saveTransMessage(trans.getTransMessage());
    }

    /**
     * @see ReversalOrCancelStrategy#cancelOrReverse(Context, Transaction).
     */
    public boolean reverseOrCancel(Context<?> respCtx, Transaction clientTargetTrans) throws ApplicationException {
        Transaction dbTargetTrans = null;
        if (this.getEntityManager().contains(clientTargetTrans)) {
            /**
             * If <code>Transaction</code> entity has been loaded by entity manager, no need to load it again. As
             * {@link #reverseOrCancel(Context<?>, BaseTicket)} will load transaction entity first. And {@link
             * #reverseOrCancel(Context<?>, BaseTicket)} will be called by 'cancelByTicket' of <code>Controller</code>
             * accordingly.
             */
            dbTargetTrans = clientTargetTrans;
        } else {
            dbTargetTrans = this.getTransactionDao().getByDeviceAndTraceMessage(clientTargetTrans.getDeviceId(),
                    clientTargetTrans.getTraceMessageId());
        }
        if (dbTargetTrans == null) {
            throw new ApplicationException(SystemException.CODE_NO_TRANSACTION, "can NOT find transaction(deviceId="
                    + clientTargetTrans.getDeviceId() + ",traceMessageId=" + clientTargetTrans.getTraceMessageId()
                    + ").");
        }
        // fix bug#5025
        if (SystemException.CODE_OK != dbTargetTrans.getResponseCode()) {
            if (SystemException.CODE_CANCELLED_TRANS == dbTargetTrans.getResponseCode()) {
                logger.info("NO need to rollback transaction(id=" + dbTargetTrans.getId()
                        + "), it has been rollbacked.");
                throw new ApplicationException(SystemException.CODE_CANCELLED_TRANS, "The transaction(id="
                        + dbTargetTrans.getId() + ") has been reversed, no need to reverse repeatedly.");
            } else {
                logger.info("NO need to rollback transaction(id=" + dbTargetTrans.getId()
                        + "), it is a unsuccessul trasnaction(Code:" + dbTargetTrans.getResponseCode() + ")");
            }
            return false;
        }

        // FIX#5032
        // If is automatical transactions, such as 'cancel by transaction', and
        // the operator hasn't been allocated to any merchant, in this case, the
        // merchant can be null... set the original merchant to this field
        if (respCtx.getMerchant() == null) {
            Merchant retailer = this.getBaseJpaDao().findById(Merchant.class, dbTargetTrans.getMerchantId());
            if (retailer == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "merchant(id="
                        + dbTargetTrans.getMerchantId() + ") doesn't exist.");
            }
            respCtx.setMerchant(retailer);
        }

        respCtx.getTransaction().setGameId(dbTargetTrans.getGameId());
        respCtx.getTransaction().setTotalAmount(dbTargetTrans.getTotalAmount());
        respCtx.getTransaction().setTicketSerialNo(dbTargetTrans.getTicketSerialNo());
        respCtx.getTransaction().setCancelTransactionType(dbTargetTrans.getType());
        respCtx.getTransaction().setCancelTransactionId(dbTargetTrans.getId());
        if (respCtx.getTransaction().isManualCancel()) {
            respCtx.getTransaction().setType(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType());
        }
        // set game type
        this.lookupGameType(respCtx, dbTargetTrans);
        // set transaction message
        TransactionMessage transMsg = this.getTransMessageDao().getById(dbTargetTrans.getId());
        dbTargetTrans.setTransMessage(transMsg);
        ReversalOrCancelStrategy reversalStrategy = this.getReversalStrategyFactory().lookupReversalStrategy(respCtx,
                dbTargetTrans);
        boolean isCancelDeline = reversalStrategy.cancelOrReverse(respCtx, dbTargetTrans);
        // update original transaction
        dbTargetTrans.setResponseCode(SystemException.CODE_CANCELLED_TRANS);
        dbTargetTrans.setCancelTransactionId(respCtx.getTransaction().getId());
        dbTargetTrans.setCancelTransactionType(respCtx.getTransaction().getType());

        // assemble AMQP message
        MessagePack amqpPack = this.assembleAmqpPack(respCtx, dbTargetTrans);
        respCtx.setTransMessage(amqpPack);

        return isCancelDeline;
    }

    @Override
    public boolean reverseOrCancel(Context<?> respCtx, BaseTicket ticket) throws ApplicationException {
        /**
         * Refactor to call {@link TransactionService#reverseOrCancel} service, make 'cancel by ticket' and 'cancel by
         * transaction' follow same logic, otherwise you have to change the implementation of both 'cancelbyticket' and
         * 'cancelbytransaction'. Besides {@link TransactionService#reverseOrCancel} will call
         * {@link TicketService#cancelByTicket}.
         */
        Transaction saleTrans = this.getTransactionDao().getByTicketAndType(ticket.getSerialNo(),
                TransactionType.SELL_TICKET.getRequestType());
        respCtx.getTransaction().setManualCancel(ticket.isManualCancel());
        return this.reverseOrCancel(respCtx, saleTrans);
    }

    @Override
    public void pendTransaction(int cancelTransType, Object... params) throws ApplicationException {
        // check arguments
        if (cancelTransType == TransactionType.CANCEL_BY_TICKET.getRequestType()) {
            if (params.length != 3) {
                throw new IllegalArgumentException("ticket serialNo,deviceId,traceMsgId must be provided.");
            }
        } else {
            if (params.length != 2) {
                throw new IllegalArgumentException("deviceID and traceMsgId must be provided.");
            }
        }

        // if server type is slave, write pending log
        SysConfiguration sysConf = this.getSysConfigurationDao().getSysConfiguration();
        if (SysConfiguration.SERVER_TYPE_SLAVE != sysConf.getServerType()) {
            if (cancelTransType == TransactionType.CANCEL_BY_TICKET.getRequestType()) {
                throw new ApplicationException(SystemException.CODE_NO_TICKET, "can NOT find ticket(serialNO="
                        + (String) params[0] + ") from underlying database.");
            } else {
                throw new ApplicationException(SystemException.CODE_NO_TRANSACTION,
                        "can not find transaction by terminalId=" + (Long) params[0] + ",traceMessageId="
                                + (String) params[1]);
            }
        }

        PendingTransaction pending = new PendingTransaction();
        pending.setId(this.getUuidManager().getGeneralID());
        pending.setTransType(cancelTransType);
        pending.setCreateTime(new Date());
        pending.setUpdateTime(pending.getCreateTime());
        if (cancelTransType == TransactionType.CANCEL_BY_TICKET.getRequestType()) {
            pending.setTicketSerialNo((String) params[0]);
            pending.setDeviceId((Long) params[1]);
            pending.setTraceMsgId((String) params[2]);
        } else {
            pending.setDeviceId((Long) params[0]);
            pending.setTraceMsgId((String) params[1]);
        }

        if (logger.isInfoEnabled()) {
            logger.info("This request will be pended(" + pending + ").");
        }

        this.getPendingTransactionDao().insert(pending);
    }

    // ---------------------------------------------------
    // PRIVATE METHODS
    // ---------------------------------------------------

    private MessagePack assembleAmqpPack(Context<?> respCtx, Transaction dbTargetTrans) {
        TransactionType targetTransType = TransactionType.getTransactionType(dbTargetTrans.getType());
        if (!targetTransType.isCancellation() && targetTransType.isPublishTransMsg()) {
            TeTransactionMessage.Cancellation msg = AmqpMessageUtils.assembleCancellationMsg(respCtx, dbTargetTrans);
            return new MessagePack(MessagePack.PREFIX + "." + dbTargetTrans.getType(), MessagePack.PREFIX + ".CANCEL."
                    + dbTargetTrans.getType() + "." + respCtx.getGameTypeIdIntValue(), msg);
        } else {
            logger.debug("No need to publish cancellation for target transaction(type=" + dbTargetTrans.getType()
                    + ",id=" + dbTargetTrans.getId() + ").");
            return null;
        }
    }

    private void saveTransMessage(TransactionMessage transMessage) {
        if (transMessage == null) {
            return;
        }
        // convert reqJsonMap and respJsonMap to JSON string
        if (transMessage.getRequestMsg() == null) {
            // to be compatible with old implementation, they will call {@link
            // TransactionMessage#setRequestMsg()} directly...we can't clear
            // them
            transMessage.setRequestMsg(transMessage.encodeReqJsonMap());
        }
        if (transMessage.getResponseMsg() == null) {
            transMessage.setResponseMsg(transMessage.encodeRespJsonMap());
        }

        this.getTransMessageDao().insert(transMessage);
    }

    protected int lookupGameType(Context respCtx, Transaction targetTrans) throws ApplicationException {
        int gameType = Game.TYPE_UNDEF;
        if (targetTrans.getGameId() != null) {
            gameType = this.getGameDao().findById(Game.class, targetTrans.getGameId(), false).getType();
        }
        respCtx.setGameTypeId(gameType + "");
        return gameType;
    }

    /**
     * get return result according to serialNo or gameid.
     */
    private BaseTicket getTransactionTicketByGameInfo(Context respCtx, Transaction trans) throws ApplicationException {
        DummyTicket ticket = new DummyTicket();
        ticket.setSerialNo(trans.getTicketSerialNo());
        if (trans.getTicketSerialNo() == null) {
            return null;
        }

        // UGLY!!! should query game type id according to transaction table's
        // gameID
        int gameTypeId = respCtx.getGameTypeIdIntValue();
        if (gameTypeId == Game.TYPE_UNDEF) {
            return null;
        }

        TicketEnquiryService ticketEnquiryHandler = this.ticketEnquiryMap.get(GameType.fromType(gameTypeId));
        if (ticketEnquiryHandler == null) {
            throw new SystemException(SystemException.CODE_INTERNAL_SERVER_ERROR,
                    "No dedicated ticket enquiry handler found for game type:" + gameTypeId);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Found TicketEnquiryService[" + ticketEnquiryHandler + "] for ticket enquiry.");
        }
        BaseTicket hostTicket = ticketEnquiryHandler.enquiry(respCtx, ticket, true);
        return hostTicket;
    }

    // ---------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ---------------------------------------------------

    public GameDao getGameDao() {
        return gameDao;
    }

    public void setGameDao(GameDao gameDao) {
        this.gameDao = gameDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public ReversalOrCancelStrategyFactory getReversalStrategyFactory() {
        return reversalStrategyFactory;
    }

    public void setReversalStrategyFactory(ReversalOrCancelStrategyFactory reversalStrategyFactory) {
        this.reversalStrategyFactory = reversalStrategyFactory;
    }

    public TransactionMessageDao getTransMessageDao() {
        return transMessageDao;
    }

    public void setTransMessageDao(TransactionMessageDao transMessageDao) {
        this.transMessageDao = transMessageDao;
    }

    public PendingTransactionDao getPendingTransactionDao() {
        return pendingTransactionDao;
    }

    public void setPendingTransactionDao(PendingTransactionDao pendingTransactionDao) {
        this.pendingTransactionDao = pendingTransactionDao;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidManager) {
        this.uuidManager = uuidManager;
    }

    public SysConfigurationDao getSysConfigurationDao() {
        return sysConfigurationDao;
    }

    public void setSysConfigurationDao(SysConfigurationDao sysConfigurationDao) {
        this.sysConfigurationDao = sysConfigurationDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
