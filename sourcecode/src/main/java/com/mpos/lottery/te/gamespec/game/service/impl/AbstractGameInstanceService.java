package com.mpos.lottery.te.gamespec.game.service.impl;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.ChannelGameInstanceTime;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameTypeAware;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;
import com.mpos.lottery.te.gamespec.game.dao.ChannelGameInstanceTimeDao;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.gamespec.game.support.GameInstanceServiceFactory;
import com.mpos.lottery.te.gamespec.game.web.GameDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class AbstractGameInstanceService implements GameTypeAware, GameInstanceService, InitializingBean {
    private Log logger = LogFactory.getLog(AbstractGameInstanceService.class);
    private GameInstanceServiceFactory gameInstanceServiceFactory;
    private BaseGameInstanceDao gameInstanceDao;
    private BaseJpaDao baseJpaDao;
    private ChannelGameInstanceTimeDao channelGameInstanceTimeDao;
    private MerchantService merchantService;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.getGameInstanceServiceFactory().registerHandler(this.supportedGameType(), this);
    }

    @Override
    public void allowPayout(Context respCtx, List<? extends BaseTicket> hostTickets, boolean isEnquiry)
            throws ApplicationException {
        BaseGameInstance soldGameInstance = hostTickets.get(0).getGameInstance();
        if (BaseGameInstance.STATE_PAYOUT_STARTED != soldGameInstance.getState()) {
            throw new ApplicationException(SystemException.CODE_DRAW_NOTPAYOUTSTARTED, "The game draw(drawNo="
                    + soldGameInstance.getNumber() + ") of buying ticket(serialNo=" + hostTickets.get(0).getSerialNo()
                    + ") is not 'payout started':" + soldGameInstance.getState());
        }

        /**
         * If internal payout, only when all game instances are payout-started, this operation will be allowed.
         */
        BaseGameInstance lastGameInstance = hostTickets.get(hostTickets.size() - 1).getGameInstance();
        if (respCtx.isInternalCall() && BaseGameInstance.STATE_PAYOUT_STARTED != lastGameInstance.getState()) {
            throw new ApplicationException(SystemException.CODE_DRAW_NOTPAYOUTSTARTED, "The last game instance(drawNo="
                    + lastGameInstance.getNumber() + ") of buying ticket(serialNo=" + hostTickets.get(0).getSerialNo()
                    + ") is not 'payout started':" + lastGameInstance.getState()
                    + ", internal payout is allowed only when all game instances are payout-started.");
        }

        // check status of game
        if (Game.STATUS_ACTIVE != soldGameInstance.getGame().getState()) {
            throw new ApplicationException(SystemException.CODE_GAME_INACTIVE, "Game(id="
                    + soldGameInstance.getGame().getId() + ") isn't active.");
        }
        // check all game instances, whether some game instances are in progress
        // of winning analysis or payout blocked.
        BaseGameInstance payoutStartedGameInstance = soldGameInstance;
        for (int i = 0; i < hostTickets.size(); i++) {
            BaseGameInstance gameInstance = hostTickets.get(i).getGameInstance();
            if (BaseGameInstance.STATE_PAYOUT_STARTED == gameInstance.getState()) {
                payoutStartedGameInstance = gameInstance;
            }
            if ((new Date().after(gameInstance.getEndTime()) && BaseGameInstance.STATE_PAYOUT_STARTED != gameInstance
                    .getState())) {
                throw new ApplicationException(SystemException.CODE_INPROGRESSOF_WINNINGANALYSIS, "game draw(id="
                        + gameInstance.getId() + ",number=" + gameInstance.getNumber()
                        + ") is in progress of winning analysis, please try later.");
            }
            if (gameInstance.isPayoutBlocked()) {
                throw new ApplicationException(SystemException.CODE_SUSPEND_PAYOUT, "game instance(id="
                        + gameInstance.getId() + ",number=" + gameInstance.getNumber()
                        + ") has been payout suspended, please try later.");
            }
        }

        if (!isEnquiry) {
            // check max allowed payout period
            payoutStartedGameInstance.isPastLastClaimDay();
        }
    }

    @Override
    public List<? extends BaseGameInstance> enquirySaleReady(Context response, String gameId, String number,
            int multipleDraws) throws ApplicationException {
        return this.doEnquiryMultiDraw(response, gameId, number, multipleDraws, true);
    }

    @Override
    public List<? extends BaseGameInstance> enquiryMultiDraw(Context response, String gameId, String number,
            int multipleDraws) throws ApplicationException {
        return this.doEnquiryMultiDraw(response, gameId, number, multipleDraws, false);
    }

    @Override
    public List<? extends BaseGameInstance> enquirySaleReady(Context response) throws ApplicationException {
        List<? extends BaseGameInstance> gameInstances = this.getGameInstanceDao().lookupActiveByGameType(
                response.getMerchant().getId(), this.supportedGameType().getGameInstanceType());
        if (gameInstances.size() == 0) {
            throw new ApplicationException(SystemException.CODE_NO_GAMEDRAW, "can NOT find active game instances of "
                    + this.supportedGameType() + ", or no any games of type(" + this.supportedGameType()
                    + ") have been allocated to merchant(id=" + response.getMerchant().getId() + ") yet");
        }
        this.allowSale(response, gameInstances.get(0));
        this.customVerifySaleReadyGameInstance(gameInstances.get(0));
        return gameInstances;
    }

    @Override
    public List<? extends BaseGameInstance> enquirySaleReady(Context response, String gameId)
            throws ApplicationException {
        if (gameId == null) {
            throw new IllegalArgumentException("argument 'gameId' can NOT be null.");
        }

        List<? extends BaseGameInstance> gameInstances = this.getGameInstanceDao().lookupActiveByGame(
                response.getMerchant().getId(), this.supportedGameType().getGameInstanceType(), gameId);
        if (gameInstances == null || gameInstances.size() == 0) {
            throw new ApplicationException(SystemException.CODE_NOT_ACTIVE_DRAW,
                    "No active game instance found for game(id=" + gameId + ") of game type("
                            + this.supportedGameType() + "), or the game hasn't been allocated to merchant(id="
                            + response.getMerchant().getId() + ")yet.");
        }

        this.allowSale(response, gameInstances.get(0));
        this.customVerifySaleReadyGameInstance(gameInstances.get(0));
        return gameInstances;
    }

    @Override
    public BaseGameInstance enquiry(Context respCtx, String gameId, String number) throws ApplicationException {
        if (gameId == null) {
            throw new IllegalArgumentException("argument 'gameId' can NOT be null.");
        }
        if (number == null) {
            throw new IllegalArgumentException("argument 'number' can NOT be null.");
        }

        BaseGameInstance gameInstance = this.getGameInstanceDao().lookupByGameAndNumber(respCtx.getMerchant().getId(),
                this.supportedGameType().getGameInstanceType(), gameId, number);
        if (gameInstance == null) {
            throw new ApplicationException(SystemException.CODE_NO_GAMEDRAW,
                    "No raffle game instance found by game(id=" + gameId + ",number=" + number + ").");
        }
        gameInstance.setGameId(gameId);

        return gameInstance;
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------

    protected List<? extends BaseGameInstance> doEnquiryMultiDraw(Context response, String gameId, String number,
            int multipleDraws, boolean needFirstDrawSaleReady) throws ApplicationException {
        // lookup game instances
        List<? extends BaseGameInstance> gameInstances = this.getGameInstanceDao().lookupFutureByGameAndNumber(
                response.getMerchant().getId(), this.supportedGameType().getGameInstanceType(), gameId, number,
                multipleDraws);
        if (gameInstances.size() < multipleDraws) {
            throw new ApplicationException(SystemException.CODE_NOENOUGH_FUTUREGAMEDRAW, "Only " + gameInstances.size()
                    + " future game instances avaliable, while client requests " + multipleDraws);
        }
        if (gameInstances.size() == 0) {
            throw new ApplicationException(SystemException.CODE_NO_GAMEDRAW, "can NOT find "
                    + this.supportedGameType().getGameInstanceType() + " with(number=" + number + ",gameId=" + gameId
                    + ").");
        }
        if (needFirstDrawSaleReady) {
            this.allowSale(response, gameInstances.get(0));
        }
        this.customVerifySaleReadyGameInstance(gameInstances.get(0));
        return gameInstances;
    }

    /**
     * For subclass to customize the verification process of game instance(ready-for-sale).
     */
    protected void customVerifySaleReadyGameInstance(BaseGameInstance currentGameInstance) throws ApplicationException {
        // template method
    }

    /**
     * Whether a game instance is ready for sale.
     */
    protected void allowSale(Context response, BaseGameInstance gameInstance) throws ApplicationException {
        int gameState = gameInstance.getGame().getState();
        if (gameState == Game.STATUS_INACTIVE) {
            throw new ApplicationException(SystemException.CODE_GAME_INACTIVE, "Game(id="
                    + gameInstance.getGame().getId() + ") isn't active.");
        }
        
        // Determine whether a game allows sale on 'new' game instance.
        boolean allowSaleOnNewGameInstance = false;
        if (this.supportedGameType().getOperationParametersType() != null) {
            BaseOperationParameter opPara = this.getBaseJpaDao().findById(
                    this.supportedGameType().getOperationParametersType(),
                    gameInstance.getGame().getOperatorParameterId());
            allowSaleOnNewGameInstance = opPara.isAllowSaleOnNewGameInstance();
        }

        if (allowSaleOnNewGameInstance && BaseGameInstance.STATE_NEW == gameInstance.getState()) {
            return;
        }
        
        Date stopSellingTime = this.calculateAdvancedTime(response, gameInstance);
        // check the status of first game instance
        Date now = new Date();
        if (gameInstance.getState() == BaseGameInstance.STATE_ACTIVE
                && (now.after(gameInstance.getBeginTime()) && now.before(stopSellingTime))
                && !gameInstance.isSaleSuspended()) {
            // silently ignore
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append("The game instance(number=").append(gameInstance.getNumber()).append(",game.type=");
            buffer.append(gameInstance.getGame().getType()).append(",status=" + gameInstance.getState());
            buffer.append(",startSellintTime=").append(gameInstance.getBeginTime()).append(",stopSellTime=");
            buffer.append(stopSellingTime).append(",suspendSale=" + gameInstance.isSaleSuspended());
            buffer.append(") doesn't accept sale.");
            logger.error(buffer.toString());
            if (gameInstance.getState() != BaseGameInstance.STATE_ACTIVE) {
                throw new ApplicationException(SystemException.CODE_NOT_ACTIVE_DRAW, "Game instance(id="
                        + gameInstance.getId() + ") isn't active.");
            } else {
                if (now.after(stopSellingTime) || now.before(gameInstance.getBeginTime())) {
                    throw new ApplicationException(SystemException.CODE_SALE_STOPPED_CHANNEL,
                            "No sale accepted by game instance channel.");
                }
                if (gameInstance.isSaleSuspended()) {
                    throw new ApplicationException(SystemException.CODE_SUSPENDED_GAME_INSTANCE,
                            "Game instance has suspended sale.");
                }
                // workflow shouldn't arrive here.
            }
        }
    }

    /**
     * Calculate the stop-selling-time/freezing-time in advanced.
     */
    protected Date calculateAdvancedTime(Context response, BaseGameInstance gameInstance) {
        ChannelGameInstanceTime channelTime = this.getChannelGameInstanceTimeDao().findByChannelType(
                gameInstance.getGameChannelSettingId(), response.getGpe().getType());
        if (channelTime == null) {
            logger.info("No channel game instance time definiton found, ignore it.");
            return gameInstance.getEndTime();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Use channel game instance time definition(" + channelTime.getId() + ").");
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(gameInstance.getEndTime());
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - channelTime.getStopSellingTimeInMinutes());
        return calendar.getTime();
    }

    protected abstract void customizeGameInstanceDto(GameInstanceDto dto, BaseGameInstance gameInstance)
            throws ApplicationException;

    protected abstract void customizeGameDto(GameDto gameDto, BaseGameInstance gameInstance)
            throws ApplicationException;

    protected GameDto buildGameDto(BaseGameInstance gameInstance) {
        GameDto gameDto = new GameDto();
        gameDto.setId(gameInstance.getGame().getId());
        gameDto.setGameType(gameInstance.getGame().getType());
        gameDto.setBaseAmount(this.lookupOperationParameter(gameInstance.getGame().getOperatorParameterId())
                .getBaseAmount());
        return gameDto;
    }

    protected BaseOperationParameter lookupOperationParameter(String operationParamId) {
        BaseOperationParameter operationParam = this.getBaseJpaDao().findById(
                this.supportedGameType().getOperationParametersType(), operationParamId);
        if (operationParam == null) {
            throw new SystemException(SystemException.CODE_INTERNAL_SERVER_ERROR, "No entity("
                    + this.supportedGameType().getOperationParametersType() + ") found by id=" + operationParamId);
        }
        return operationParam;
    }

    // -------------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -------------------------------------------------------------------------

    public BaseGameInstanceDao getGameInstanceDao() {
        return gameInstanceDao;
    }

    public void setGameInstanceDao(BaseGameInstanceDao gameInstanceDao) {
        this.gameInstanceDao = gameInstanceDao;
    }

    public GameInstanceServiceFactory getGameInstanceServiceFactory() {
        return gameInstanceServiceFactory;
    }

    public void setGameInstanceServiceFactory(GameInstanceServiceFactory gameInstanceServiceFactory) {
        this.gameInstanceServiceFactory = gameInstanceServiceFactory;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public ChannelGameInstanceTimeDao getChannelGameInstanceTimeDao() {
        return channelGameInstanceTimeDao;
    }

    public void setChannelGameInstanceTimeDao(ChannelGameInstanceTimeDao channelGameInstanceTimeDao) {
        this.channelGameInstanceTimeDao = channelGameInstanceTimeDao;
    }

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

}
