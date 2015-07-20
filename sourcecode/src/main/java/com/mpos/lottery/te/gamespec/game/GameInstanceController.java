package com.mpos.lottery.te.gamespec.game;

import com.mpos.lottery.common.router.RequestMap;
import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.gamespec.game.support.GameInstanceServiceFactory;
import com.mpos.lottery.te.gamespec.game.web.GameDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDtos;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

@Controller
public class GameInstanceController {
    private Log logger = LogFactory.getLog(GameInstanceController.class);
    @Autowired
    private GameInstanceServiceFactory gameInstanceServiceFactory;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    @RequestMap("{transType:101}")
    public void enquiry(Context request, Context response) throws ApplicationException {
        GameInstanceDto gameInstanceDto = (GameInstanceDto) request.getModel();
        GameType requestGameType = GameType.fromType(gameInstanceDto.getGameType());
        GameInstanceService service = this.getGameInstanceServiceFactory().lookupService(response, requestGameType);
        if (service == null) {
            throw new ApplicationException(SystemException.CODE_NO_GAMEDRAW, "NO any games of type(" + requestGameType
                    + ") have been allocated to merchant(id=" + response.getMerchant().getId() + ") yet");
        }
        List<BaseGameInstance> gameInstances = new LinkedList<BaseGameInstance>();
        if (gameInstanceDto.getGameId() == null) {
            // query all active game instances of given game type
            if (logger.isDebugEnabled()) {
                logger.debug("Query all sale-ready game isntances of game type:" + gameInstanceDto.getGameType());
            }
            gameInstances = this.convertGeneralTypeGameInstance(service.enquirySaleReady(response));
        } else {
            if (gameInstanceDto.getNumber() == null) {
                // query all active game instances of given game
                if (logger.isDebugEnabled()) {
                    logger.debug("Query all sale-ready game isntances of game type:" + gameInstanceDto.getGameType()
                            + ",gameId:" + gameInstanceDto.getGameId());
                }
                gameInstances = this.convertGeneralTypeGameInstance(service.enquirySaleReady(response,
                        gameInstanceDto.getGameId()));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Query game isntance of game type:" + gameInstanceDto.getGameType() + ",gameId:"
                            + gameInstanceDto.getGameId() + ",number:" + gameInstanceDto.getNumber());
                }
                gameInstances.add(service.enquiry(response, gameInstanceDto.getGameId(), gameInstanceDto.getNumber()));
            }
        }

        // assemble <code>GameInstanceDto</code>s of all supported game
        // instances.
        GameInstanceDtos gameInstanceDtos = new GameInstanceDtos();
        for (BaseGameInstance gameInstance : gameInstances) {
            GameDto gameDto = this.buildGameDto(gameInstances.get(0));
            gameInstance.setGameId(gameInstance.getGame().getId());
            gameDto.getGameInstanceDtos().add(new GameInstanceDto(gameInstance));
            gameInstanceDtos.add(gameDto);
        }

        response.setModel(gameInstanceDtos);
    }

    // --------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------

    protected GameDto buildGameDto(BaseGameInstance gameInstance) {
        GameDto gameDto = new GameDto();
        gameDto.setId(gameInstance.getGame().getId());
        gameDto.setGameType(gameInstance.getGame().getType());
        gameDto.setBaseAmount(this.lookupOperationParameter(gameInstance.getGame()).getBaseAmount());
        return gameDto;
    }

    protected BaseOperationParameter lookupOperationParameter(Game game) {
        String operationParamId = game.getOperatorParameterId();
        GameType gameType = GameType.fromType(game.getType());
        BaseOperationParameter operationParam = this.getBaseJpaDao().findById(gameType.getOperationParametersType(),
                operationParamId);
        if (operationParam == null) {
            throw new SystemException(SystemException.CODE_INTERNAL_SERVER_ERROR, "No entity("
                    + gameType.getOperationParametersType() + ") found by id=" + operationParamId);
        }
        return operationParam;
    }

    /**
     * WHY <code>List<? extends BaseGameInstance></code> can't add a instance of <code>BaseGameInstance</code>?? Fucking
     * support of java general type of collection!!
     */
    protected List<BaseGameInstance> convertGeneralTypeGameInstance(
            List<? extends BaseGameInstance> generalTypeGameInstances) {
        List<BaseGameInstance> gameInstances = new LinkedList<BaseGameInstance>();
        for (BaseGameInstance g : generalTypeGameInstances) {
            gameInstances.add(g);
        }

        return gameInstances;
    }

    // --------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------------------

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

}
