package com.mpos.lottery.te.gamespec.game.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage the implemnetation of <code>GameInstanceService</code>. A implementation of <code>GameInstanceService</code>
 * should register itself to <code>GameInstanceServiceFactory</code> by call <code>registerHandler</code>. There is a
 * base class <code>com.mpos.lottery.te.gamespec.game.service.impl</code> for implementation to inherit, it is a good
 * start point.
 * 
 * @author Ramon Li
 */
public class GameInstanceServiceFactory {
    private Log logger = LogFactory.getLog(GameInstanceServiceFactory.class);
    private MerchantService merchantService;
    private Map<GameType, GameInstanceService> gameTypeHandler = new HashMap<GameType, GameInstanceService>();

    public void registerHandler(GameType gameType, GameInstanceService gameInstanceService) {
        GameInstanceService target = gameTypeHandler.get(gameType);
        if (target != null) {
            throw new IllegalStateException("A GameInstanceService(" + target + ") has been registered by gametype("
                    + gameType + ").");
        } else {
            this.gameTypeHandler.put(gameType, gameInstanceService);
            if (logger.isDebugEnabled()) {
                logger.debug("Register a GameInstanceService(" + gameInstanceService + ") for game type:" + gameType);
            }
        }

    }

    public GameInstanceService lookupService(Context respCtx, GameType gameType) throws ApplicationException {
        // check whether merchant supported this game type
        boolean merchantSupportted = false;
        List<GameType> supportedGameTypes = this.getMerchantService().supportedGameType(respCtx.getMerchant().getId());
        for (GameType supportedGameType : supportedGameTypes) {
            if (supportedGameType.equals(gameType)) {
                merchantSupportted = true;
            }
        }
        if (!merchantSupportted) {
            return null;
        }

        GameInstanceService service = this.gameTypeHandler.get(gameType);
        if (service == null) {
            throw new SystemException("No dedicated " + GameInstanceService.class + " found for game type:" + gameType);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Found GameInstanceService(" + service + ") for game type:" + gameType);
        }
        return service;
    }

    // ------------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ------------------------------------------------------------------------

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

}
