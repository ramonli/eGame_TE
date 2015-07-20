package com.mpos.lottery.te.valueaddservice.airtime.service;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.merchant.dao.MerchantCommissionDao;
import com.mpos.lottery.te.merchant.domain.MerchantCommission;
import com.mpos.lottery.te.merchant.service.balance.BalanceService;
import com.mpos.lottery.te.merchant.service.balance.SaleBalanceStrategy;
import com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.thirdpartyservice.amqp.AmqpMessageUtils;
import com.mpos.lottery.te.thirdpartyservice.amqp.MessagePack;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeParameter;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;
import com.mpos.lottery.te.valueaddservice.airtime.dao.AirtimeDao;
import com.mpos.lottery.te.valueaddservice.airtime.support.AirtimeProvider;
import com.mpos.lottery.te.valueaddservice.airtime.support.AirtimeProviderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This implementation will support multiple airtime service providers. Client should call
 * {@link #topup(Context, AirtimeTopup)} to topup mobile balance, this implementation will route cient's request to
 * appropriate service provider (airtime telecomm).
 * 
 * @author Ramon
 */
public class MultiProviderAirtimeService implements AirtimeService {
    private Log logger = LogFactory.getLog(MultiProviderAirtimeService.class);
    @Resource(name = "jpaAirtimeDao")
    private AirtimeDao airtimeDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;
    @Resource(name = "saleCommissionBalanceService")
    private CommissionBalanceService commissionService;
    @Resource(name = "airtimeProviderFactory")
    private AirtimeProviderFactory airtimeProviderFactory;
    @Resource(name = "amqpTemplate")
    private AmqpTemplate amqpTemplate;
    @Resource(name = "merchantCommissionDao")
    private MerchantCommissionDao merchantCommissionDao;

    @Override
    public AirtimeTopup topup(Context respCtx, AirtimeTopup topupReq) throws ApplicationException {
        Assert.notNull(topupReq.getAmount(), "The amount can't be null");
        Assert.notNull(topupReq.getMobileNo(), "The mobileNo can't be null");
        Assert.notNull(topupReq.getGame().getId(), "THe game.id can't be null");

        // verify game status, accautlly a game represent a airtime service provider(telecom operator).
        Game game = this.getBaseJpaDao().findById(Game.class, topupReq.getGame().getId());
        if (Game.STATUS_ACTIVE != game.getState()) {
            throw new ApplicationException(SystemException.CODE_GAME_INACTIVE, "Game(id=" + game.getId()
                    + ") isn't active.");
        }
        // verify whether game have been allocated to merchant
        this.verifyGameAllocation(respCtx, game);
        // verify amount
        this.verifyGameParameter(respCtx, topupReq, game);
        // verify sale balance and lock it to avoid other transaction modify it
        this.verifyBalance(respCtx, topupReq);

        // call remote service
        AirtimeParameter parameter = this.getBaseJpaDao().findById(AirtimeParameter.class,
                game.getOperatorParameterId());
        AirtimeProvider provider = this.getAirtimeProviderFactory().lookupProvider(parameter.getAirtimeProviderId());
        if (provider == null) {
            throw new SystemException("No airtime p" + "rovider found by given providerId("
                    + parameter.getAirtimeProviderId() + ")");
        }
        AirtimeTopup result = provider.topup(respCtx, topupReq);

        if (AirtimeTopup.STATUS_SUCCESS == result.getStatus() || AirtimeTopup.STATUS_PENDING == result.getStatus()) {
            // assemble transaction
            respCtx.getTransaction().setTotalAmount(topupReq.getAmount());
            respCtx.getTransaction().setGameId(game.getId());
            respCtx.getTransaction().setTicketSerialNo(respCtx.getTransaction().getId());
            if (AirtimeTopup.STATUS_SUCCESS == result.getStatus()) {
                // maintain the balance/commission the same way with sale.
                respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, false);
                // update sale balance
                Object operatorMerchant = this.getBalanceService().balance(respCtx, BalanceService.BALANCE_TYPE_SALE,
                        respCtx.getTransaction().getOperatorId(), false);
                // generate topup transaction records
                this.getCommissionService().calCommission(respCtx, operatorMerchant);
            }
            if (AirtimeTopup.STATUS_PENDING == result.getStatus()) {
                respCtx.getTransaction().setResponseCode(SystemException.CODE_REMOTE_SERVICE_TIMEOUT);
                respCtx.setResponseCode(SystemException.CODE_REMOTE_SERVICE_TIMEOUT);
                // as it is pending, we shouldn't publish AMQP message now.
                respCtx.setProperty(Context.KEY_PUBLISH_AMQP_MESSAGE, false);
            }

            // assemble airtim topup entity
            result.setId(respCtx.getTransaction().getId());
            result.setGame(game);
            result.setGpeSourceType(respCtx.getGpe().getTicketFrom());
            result.setOperatorId(respCtx.getTransaction().getOperatorId());
            result.setDevId(respCtx.getTransaction().getDeviceId());
            result.setMerchantId(respCtx.getTransaction().getMerchantId());
            result.setCreateTime(respCtx.getTransaction().getCreateTime());
            result.setUpdateTime(respCtx.getTransaction().getCreateTime());
            result.setTransaction(respCtx.getTransaction());
            this.getBaseJpaDao().insert(result);
        } else if (AirtimeTopup.STATUS_FAIL == result.getStatus()) {
            throw new ApplicationException(SystemException.CODE_REMOTE_SERVICE_FAILUER,
                    result.getRespMessageOfRemoteService());
        }

        return result;
    }

    @Override
    public void jobTopup(JobExecutionContext jobContext, String transactionId, AirtimeTopup result)
            throws ApplicationException {
        AirtimeTopup pending = this.getBaseJpaDao().findById(AirtimeTopup.class, transactionId);
        if (AirtimeTopup.STATUS_PENDING != pending.getStatus()) {
            if (logger.isInfoEnabled()) {
                logger.info("THe transaction(id=" + transactionId + ") isn't pending, ignore and delete the job.");
            }
            return;
        }

        Context transContext = new Context();
        Transaction teTrans = this.getBaseJpaDao().findById(Transaction.class, transactionId);
        transContext.setTransaction(teTrans);
        if (AirtimeTopup.STATUS_SUCCESS == result.getStatus()) {
            // maintain the balance/commission the same way with sale.
            transContext.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, false);
            // update sale balance
            Object operatorMerchant = this.getBalanceService().balance(transContext, BalanceService.BALANCE_TYPE_SALE,
                    transContext.getTransaction().getOperatorId(), false);
            // generate topup transaction records
            this.getCommissionService().calCommission(transContext, operatorMerchant);
            teTrans.setResponseCode(SystemException.CODE_OK);

            // publish AMQP message.
            this.publishAmqp(transContext, teTrans);
        } else if (AirtimeTopup.STATUS_FAIL == result.getStatus()) {
            teTrans.setResponseCode(SystemException.CODE_REMOTE_SERVICE_FAILUER);
        }
        pending.setTelcCommTransId(result.getTelcCommTransId());
        pending.setStatus(result.getStatus());
        pending.setUpdateTime(new Date());
        this.getBaseJpaDao().update(pending);

        teTrans.setUpdateTime(pending.getUpdateTime());
        this.getBaseJpaDao().insert(teTrans);

        try {
            Scheduler scheduler = jobContext.getScheduler();
            scheduler.deleteJob(jobContext.getJobDetail().getKey());
            if (logger.isInfoEnabled()) {
                logger.info("Delete job(" + jobContext.getJobDetail().getKey() + ") successfully.");
            }
        } catch (SchedulerException e) {
            throw new SystemException(e);
        }
    }

    protected void publishAmqp(Context transContext, Transaction teTrans) {
        Game game = this.getBaseJpaDao().findById(Game.class, teTrans.getGameId());
        MessagePack amqpMsg = new MessagePack(MessagePack.PREFIX + "." + teTrans.getType(), MessagePack.PREFIX + "."
                + teTrans.getType() + "." + game.getType(), AmqpMessageUtils.assembleTransactionMsg(transContext));
        amqpMsg.getMessageProperties().setHeader(MessagePack.HEADER_TRANSACTION, teTrans.getId());
        if (logger.isDebugEnabled()) {
            logger.debug("Prepare to publish message to exchange[" + amqpMsg.getExchangeName() + "] with routing key["
                    + amqpMsg.getRoutingKey() + "]: " + amqpMsg.getProtobuffMessage());
        }

        this.getAmqpTemplate().send(amqpMsg.getExchangeName(), amqpMsg.getRoutingKey(), amqpMsg.getAmqpMessage());
        if (logger.isDebugEnabled()) {
            logger.debug("Publish message successfully");
        }
    }

    // -------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------

    private void verifyGameAllocation(Context respCtx, Game game) {
        MerchantCommission comm = this.getMerchantCommissionDao().getByMerchantAndGame(respCtx.getMerchant().getId(),
                game.getId());
        if (comm == null) {
            throw new SystemException(SystemException.CODE_OPERATOR_SELL_NOPRIVILEDGE, "operator(id="
                    + respCtx.getOperatorId() + ") has no priviledge to sell ticket of game '" + game.getId()
                    + "', allocate the game to its merchant first.");
        }
    }

    /**
     * Verify whether the sale balance of operator/merchant is enough for transaction, and lock it for later updating.
     */
    protected void verifyBalance(Context respCtx, AirtimeTopup topupReq) throws ApplicationException {
        this.getBalanceService().lockAndVerifySaleBalance(respCtx.getTransaction().getOperatorId(),
                respCtx.getTransaction().getMerchantId(), topupReq.getAmount());
    }

    protected void verifyGameParameter(Context respCtx, AirtimeTopup topupReq, Game game) throws ApplicationException {
        AirtimeParameter param = this.getBaseJpaDao().findById(AirtimeParameter.class, game.getOperatorParameterId());
        if (topupReq.getAmount().compareTo(param.getMinAmount()) < 0
                || topupReq.getAmount().compareTo(param.getMaxAmount()) > 0) {
            throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "The amount must be in range("
                    + param.getMinAmount() + "," + param.getMaxAmount() + "), however the amount is "
                    + topupReq.getAmount());
        }
        if (param.isAmountStepSupport()) {
            if (topupReq.getAmount().subtract(param.getMinAmount()).remainder(param.getStepAmount())
                    .compareTo(new BigDecimal("0")) != 0) {
                throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "The amount("
                        + topupReq.getAmount() + ") must follow the rule of step-amount(" + param.getStepAmount()
                        + ").");
            }
        }
    }

    // -------------------------------------------------------
    // SPRING DEPENDENDIES
    // -------------------------------------------------------

    public AirtimeDao getAirtimeDao() {
        return airtimeDao;
    }

    public void setAirtimeDao(AirtimeDao airtimeDao) {
        this.airtimeDao = airtimeDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public CommissionBalanceService getCommissionService() {
        return commissionService;
    }

    public void setCommissionService(CommissionBalanceService commissionService) {
        this.commissionService = commissionService;
    }

    public AirtimeProviderFactory getAirtimeProviderFactory() {
        return airtimeProviderFactory;
    }

    public void setAirtimeProviderFactory(AirtimeProviderFactory airtimeProviderFactory) {
        this.airtimeProviderFactory = airtimeProviderFactory;
    }

    public AmqpTemplate getAmqpTemplate() {
        return amqpTemplate;
    }

    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public MerchantCommissionDao getMerchantCommissionDao() {
        return merchantCommissionDao;
    }

    public void setMerchantCommissionDao(MerchantCommissionDao merchantCommissionDao) {
        this.merchantCommissionDao = merchantCommissionDao;
    }

}
