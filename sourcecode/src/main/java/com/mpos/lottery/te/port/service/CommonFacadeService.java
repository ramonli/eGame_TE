package com.mpos.lottery.te.port.service;

import com.mpos.lottery.common.router.NoRoutineFoundException;
import com.mpos.lottery.common.router.RoutineRegistry;
import com.mpos.lottery.common.router.Version;
import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.dao.SysConfigurationDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.dao.OperatorMerchantDao;
import com.mpos.lottery.te.merchant.domain.Device;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.domain.OperatorMerchant;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.thirdpartyservice.amqp.AmqpMessageUtils;
import com.mpos.lottery.te.thirdpartyservice.amqp.MessagePack;
import com.mpos.lottery.te.trans.dao.SettlementLogItemDao;
import com.mpos.lottery.te.trans.domain.SettlementLog;
import com.mpos.lottery.te.trans.domain.SettlementLogItem;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.service.TransactionService;
import com.mpos.lottery.te.workingkey.service.WorkingKeyService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;
import org.springframework.amqp.core.AmqpTemplate;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * The facade of all services. It means client should access service via this facade, due to some common pre-condition,
 * post-condition will be checked here.
 * <p/>
 * It includes:
 * <ol>
 * <li>Does the GPE exists?</li>
 * <li>Does timestamp in request is between local_timestamp(+/-)10 minutes..middleman-attack</li>
 * <li>Record transaction</li>
 * <li>......</li>
 * </ol>
 */
public class CommonFacadeService implements FacadeService {
    private Log logger = LogFactory.getLog(CommonFacadeService.class);
    private WorkingKeyService workingKeyService;
    private TransactionService transService;
    private UUIDService uuidManager;
    private BaseJpaDao baseJpaDao;
    private SysConfigurationDao sysConfigurationDao;
    private OperatorMerchantDao operatorMerchantDao;
    @Resource(name = "jpaSettlementLogItemDao")
    private SettlementLogItemDao settlementLogDao;
    @PersistenceContext
    private EntityManager entityManager;
    private AmqpTemplate amqpTemplate;

    /**
     * A facade method which will accept all incoming requests and route to apropriate service then.
     */
    @SuppressWarnings("rawtypes")
    public void facade(Context request, Context response) throws ApplicationException {
        this.assembleReponseContext(request, response);

        int transType = request.getTransType();
        // System service
        if (transType == TransactionType.GET_WORKING_KEY.getRequestType()
                || transType == TransactionType.CHECK_ALIVE.getRequestType()) {
            // no need to generate transactions
            routeRequest(request, response);
        }
        // merchant service
        else {
            // check operator & merchant
            this.checkOperatorMerchant(request, response);
            this.checkSettlement(response);
            // assemble a transaction
            Transaction trans = this.saveTransaction(request, response);
            request.setTransaction(trans);
            response.setTransaction(trans);

            // dispatch request
            this.routeRequest(request, response);
            this.updateTransaction(response, trans);

            /**
             * Explicitly synchronize the persistence context to the underlying database to avoid false positive.
             * Otherwise there may be risk that publish message successfully while fail to commit transaction due to
             * false positive.
             */
            this.getEntityManager().flush();
            this.publishMessage(request, response);
        }
    }

    // ------------------------------------------------
    // HELPER METHODS
    // ------------------------------------------------

    /**
     * Check whether the operator has performed settlement today. If settlement has been done, no payout allowed that
     * day.
     */
    protected final void checkSettlement(Context<?> respCtx) throws ApplicationException {
        SettlementLogItem settlementLogItem = this.getSettlementLogDao().findByOperator(respCtx.getOperatorId(),
                SettlementLog.STATE_VALID, new Date());
        if (settlementLogItem != null) {
            throw new ApplicationException(SystemException.CODE_NOTRANS_ALLOWED_AFTER_SETTLEMNT, "Operator(id="
                    + respCtx.getOperatorId() + " has done settlement, no transactions allowed today.");
        }
    }

    /**
     * WHether the transaction is issued by client automatically. In general those cancellation and reversal should be
     * automatically triggered by client...For those automatical transaction, the backend must guarantee that it should
     * be handled no matter that a operator is removed etc.
     */
    private boolean isAutoTrans(Context request, Context response) throws ApplicationException {
        int reqTransType = request.getTransType();
        if (reqTransType == TransactionType.CANCEL_BY_TRANSACTION.getRequestType()
                || reqTransType == TransactionType.REVERSAL.getRequestType()) {
            return true;
        }
        if (reqTransType == TransactionType.CANCEL_BY_TICKET.getRequestType()) {
            BaseTicket ticket = (BaseTicket) request.getModel();
            return !ticket.isManualCancel();
        }
        return false;
    }

    private final void publishMessage(Context request, Context response) {
        if (!MLotteryContext.getInstance().getBoolean("amqp.messagepublish.enable", false)) {
            logger.debug("No need to publish transaction message.");
            return;
        }
        TransactionType reqTransType = TransactionType.getTransactionType(request.getTransType());
        Boolean publishAmqp = (Boolean) response.getProperty(Context.KEY_PUBLISH_AMQP_MESSAGE);
        if (publishAmqp == null) {
            // by default, we should publish AMQP message.
            publishAmqp = true;
        }
        if (reqTransType.isPublishTransMsg() && publishAmqp) {
            StopWatch sw = new CommonsLogStopWatch();
            sw.start();
            MessagePack amqpMsg = null;
            if (reqTransType.equals(TransactionType.SELL_TICKET)) {
                if (response.getTransMessage() == null) {
                    logger.debug("No sale message found, won't publish.");
                    return;
                }
                /**
                 * For sale message, keep the original design, otherwise too many consumers have to be amended.
                 */
                amqpMsg = response.getTransMessage();
            } else {
                /**
                 * The new message publishing design...
                 * <p/>
                 * TE will publish a message carrying 'Transaction' entity for sale/payout/topup etc, and corresponding
                 * 'Cancellation' message for 'cancel' transaction. Make a uniform interface, the consumer can retrieve
                 * all information by transaction.
                 */
                if (!reqTransType.isCancellation()) {
                    // publish transaction(the context)
                    amqpMsg = new MessagePack(MessagePack.PREFIX + "." + request.getTransType(), MessagePack.PREFIX
                            + "." + request.getTransType() + "." + request.getGameTypeIdIntValue(),
                            AmqpMessageUtils.assembleTransactionMsg(response));
                } else {
                    /*
                     * publish cancellation to exchange(TE.${OrigialTransType}) with routing key
                     * (TE.CANCEL.${originalTransType}.${gameType})...assemble message in cancelByTransaction service
                     * not all cancellation should be published
                     */
                    amqpMsg = response.getTransMessage();
                }
            }
            if (amqpMsg != null) {
                // set transactionID to header
                amqpMsg.getMessageProperties().setHeader(MessagePack.HEADER_TRANSACTION,
                        response.getTransaction().getId());
                if (logger.isDebugEnabled()) {
                    logger.debug("Prepare to publish message to exchange[" + amqpMsg.getExchangeName()
                            + "] with routing key[" + amqpMsg.getRoutingKey() + "]: " + amqpMsg.getProtobuffMessage());
                }

                /**
                 * As AmqpTemplate will join current transaction context, if rollback the transaction, AmqpTemplate will
                 * send a 'TX.Rollback' to RabbitMQ Broker as well, that result in no consumer will get message.
                 * <p/>
                 * Think about that if you are running integration testcase, it will be rollbacked at the end by
                 * default, so don't expect a message will be published in this case, except you commit the transaction.
                 */
                this.getAmqpTemplate().send(amqpMsg.getExchangeName(), amqpMsg.getRoutingKey(),
                        amqpMsg.getAmqpMessage());
                logger.debug("Publish message successfully");
            }
            sw.stop("finishAMQP", "Publish AMQP message successfully");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No need to publish AMQP message of transaction(" + reqTransType + ")");
            }
        }
    }

    /**
     * Route client request to a appropriate handler. If no handler found by a routine key, its child will be used as
     * key for routing recursively. If no child routine key available, that says no handler registered for the request.
     * 
     * @param request
     *            The request context.
     * @param response
     *            The response context.
     * @throws ApplicationException
     *             The business exception thrown out by handler.
     */
    private void routeRequest(Context request, Context response) throws ApplicationException {
        int gameType = request.getGameTypeIdIntValue();
        RoutineKey routineKey = this.assembleRoutineKey(request, gameType);

        try {
            RoutineKey tmpRoutineKey = routineKey;
            while (true) {
                try {
                    RoutineRegistry.getInstance().route(tmpRoutineKey, new Context[] { request, response });
                    break;
                } catch (NoRoutineFoundException e) {
                    // try lookup handler by child routine key
                    tmpRoutineKey = tmpRoutineKey.child();
                    if (tmpRoutineKey == null) {
                        // if control flow reach here, it means no handler
                        // registered for this routine key
                        throw new SystemException("No controller found by routine key(" + routineKey + ").");
                    }
                }
            }
        } catch (InvocationTargetException e) {
            Throwable throwable = e.getTargetException();
            if (throwable != null) {
                // find the business exception thrown out by handler.
                if (throwable instanceof ApplicationException) {
                    throw (ApplicationException) throwable;
                } else if (throwable instanceof SystemException) {
                    throw (SystemException) throwable;
                } else {
                    throw new SystemException(throwable);
                }
            } else {
                throw new SystemException(e);
            }
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * Check the preconditions of operator/merchant which must be met before route to service. check below condition: 1)
     * Does the operator exists? 2) Is the operator active?
     */
    protected void checkOperatorMerchant(Context request, Context response) throws ApplicationException {
        boolean isAutoTrans = this.isAutoTrans(request, response);
        // fix bug#4420
        // 1. Verify device... device only need to be allocated to
        // distributor
        Device device = this.getBaseJpaDao().findById(Device.class, request.getTerminalId());
        if (device == null) {
            throw new ApplicationException(SystemException.CODE_NO_DEVICE, "The device(id=" + request.getTerminalId()
                    + ") doesn't exist.");
        }
        if (!isAutoTrans) {
            if (device.getMerchant() == null) {
                throw new ApplicationException(SystemException.CODE_NO_ALLOCATED_DEVICE, "device(id=" + device.getId()
                        + " hasn't been allocated to any merchant yet.");
            }
            if (Device.STATUS_ACTIVE != device.getStatus()) {
                throw new ApplicationException(SystemException.CODE_DEVICE_INACTIVE, "device(id="
                        + request.getTerminalId() + ") is not active.");
            }
        }
        response.setDevice(device);

        // 2. Verify operator
        String operatorId = request.getOperatorId();
        // check operator'status
        Operator operator = this.getBaseJpaDao().findById(Operator.class, operatorId);
        if (operator == null) {
            throw new ApplicationException(SystemException.CODE_NO_OPERATOR, "operator(id=" + operatorId
                    + ") doesn't exist.");
        }
        if (!isAutoTrans) {
            if (Operator.STATUS_ACTIVE != operator.getStatus()) {
                throw new ApplicationException(SystemException.CODE_OPERATOR_INACTIVE, "operator(id=" + operatorId
                        + ") is not active.");
            }
            /*
             * if (operator.isNeedEnrollment()) { throw new ApplicationException(SystemException.CODE_NEED_ENROLLMENT,
             * "operator(id=" + operatorId + ") needs to be enrolled first."); }
             */
        }
        response.setOperator(operator);

        // operator(card) must be assigned to leaf merchant(retailer),
        // however
        // device only needs to be assigned to distributor.
        // lookup the relationship between operator and merchant
        OperatorMerchant operatorMerchant = this.getOperatorMerchantDao().findByOperator(operatorId);
        if (!isAutoTrans && operatorMerchant == null) {
            throw new ApplicationException(SystemException.CODE_OPERATOR_NO_MERCHANT, "operator(id=" + operatorId
                    + ") doesn't belong to any merchant, allocate it first.");
        }

        // 3. Verify merchant
        if (operatorMerchant != null) {
            Merchant retailer = this.getBaseJpaDao().findById(Merchant.class, operatorMerchant.getMerchantID());
            if (retailer == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "merchant(id="
                        + operatorMerchant.getMerchantID() + ") doesn't exist.");
            }
            if (!isAutoTrans) {
                retailer.verifyActiveStatusRecursively();
            }
            response.setMerchant(retailer);

            // 4. verify whether device and operator belong to same
            // distributor
            if (!isAutoTrans
                    && device.getMerchant().lookupDistributor().getId() != retailer.lookupDistributor().getId()) {
                throw new ApplicationException(SystemException.CODE_NOT_SAME_MERCHANT, "Device(id=" + device.getId()
                        + ") and operator(id=" + operator.getId() + ") don't belong to same distributor.");
            }
        }
    }

    /**
     * Assemble a <code>RoutineKey</code> based on supplied client request.
     */
    protected RoutineKey assembleRoutineKey(Context request, int gameType) {
        return new RoutineKey(gameType, request.getTransType(), Version.from(request.getProtocalVersion()));
    }

    /**
     * Save a transaction before handling business logic.
     * 
     * @param request
     *            The request context.
     * @return a transaction record which represents current transaction.
     */
    private Transaction saveTransaction(Context request, Context response) throws ApplicationException {
        // save transaction
        Transaction trans = new Transaction();
        // guarantee 'x-timestamp' in reponse header can match the time in
        // transaction.createTime
        trans.setCreateTime(response.getTimestamp());
        trans.setDeviceId(request.getTerminalId());
        trans.setGpeId(request.getGpe() == null ? null : request.getGpe().getId());
        trans.setId(this.getUuidManager().getGeneralID());
        trans.setOperatorId(request.getOperatorId());
        trans.setTraceMessageId(request.getTraceMessageId());
        trans.setTransTimestamp(request.getTimestamp());
        trans.setType(request.getTransType());
        trans.setBatchNumber(request.getBatchNumber());
        if (request.getGpsLocation() != null) {
            // save GPS information..."Longitude,Latitude"
            String[] gps = StringUtils.split(request.getGpsLocation(), ",");
            if (gps.length == 2) {
                trans.setLongitude(new BigDecimal(gps[0]));
                trans.setLatitude(new BigDecimal(gps[1]));
            } else {
                logger.info("Invalid GPS information(" + request.getGpsLocation() + "), ignore it.");
            }
        }
        // If is automatical transactions, such as 'cancel by transaction', and
        // the operator hasn't been allocated to any merchant, in this case, the
        // merchant can be null.
        if (response.getMerchant() != null) {
            trans.setMerchantId(response.getMerchant().getId());
            trans.setParentMerchants(response.getMerchant().getParentMerchants());
        }

        /*
         * Transaction instance must be persisted before persisting ticket, or a exception will be thrown: Caused by:
         * java.lang.IllegalStateException: org.hibernate.TransientObjectExcepti on: object references an unsaved
         * transient instance - save the transient instanc e before flushing: com
         * .mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket.transaction -> com.m
         * pos.lottery.te.trans.domain.Transaction
         */
        // save transaction
        this.getTransService().save(trans);

        response.setTransactionID(trans.getId());
        return trans;
    }

    /**
     * Update transaction record after handling transaction
     * 
     * @param trans
     *            The transaction instance.
     */
    private void updateTransaction(Context response, Transaction trans) throws ApplicationException {
        // update transaction
        int transType = response.getTransType();
        if (transType != TransactionType.GET_WORKING_KEY.getResponseType()) {
            // update transaction
            trans.setResponseCode(response.getResponseCode());
            trans.setUpdateTime(response.getTimestamp());
            this.getTransService().update(trans);
        }
    }

    /**
     * Copy context headers from request to response. Not all headers will be copyed here, and some headers specified to
     * response will be assembled in service layer.
     * 
     * @param request
     *            The request context.
     * @param response
     *            The response context.
     */
    private void assembleReponseContext(Context request, Context response) {
        response.setProtocalVersion(request.getProtocalVersion());
        response.setGpe(request.getGpe());
        response.setOperatorId(request.getOperatorId());
        response.setResponseCode(SystemException.CODE_OK);
        response.setTerminalId(request.getTerminalId());
        response.setBatchNumber(request.getBatchNumber());
        response.setTraceMessageId(request.getTraceMessageId());
        response.setTransType(TransactionType.getTransactionType(request.getTransType()).getResponseType());
        response.setWorkingKey(request.getWorkingKey());
        response.setGameTypeId(request.getGameTypeIdIntValue() + "");
        response.setTimestamp(new Date());
        response.setGameTypeId(request.getGameTypeId());
        response.setInternalCall(request.isInternalCall());
    }

    // ------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ------------------------------------------------

    public WorkingKeyService getWorkingKeyService() {
        return workingKeyService;
    }

    public void setWorkingKeyService(WorkingKeyService workingKeyService) {
        this.workingKeyService = workingKeyService;
    }

    public TransactionService getTransService() {
        return transService;
    }

    public void setTransService(TransactionService transService) {
        this.transService = transService;
    }

    public UUIDService getUuidManager() {
        return uuidManager;
    }

    public void setUuidManager(UUIDService uuidService) {
        this.uuidManager = uuidService;
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

    public OperatorMerchantDao getOperatorMerchantDao() {
        return operatorMerchantDao;
    }

    public void setOperatorMerchantDao(OperatorMerchantDao operatorMerchantDao) {
        this.operatorMerchantDao = operatorMerchantDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public AmqpTemplate getAmqpTemplate() {
        return amqpTemplate;
    }

    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public SettlementLogItemDao getSettlementLogDao() {
        return settlementLogDao;
    }

    public void setSettlementLogDao(SettlementLogItemDao settlementLogDao) {
        this.settlementLogDao = settlementLogDao;
    }

}
