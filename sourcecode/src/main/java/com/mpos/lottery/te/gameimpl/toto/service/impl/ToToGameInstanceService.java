package com.mpos.lottery.te.gameimpl.toto.service.impl;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.service.impl.AbstractGameInstanceService;
import com.mpos.lottery.te.gamespec.game.web.GameDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;

public class ToToGameInstanceService extends AbstractGameInstanceService {

    @Override
    public GameType supportedGameType() {
        return GameType.TOTO;
    }

    @Override
    protected void customizeGameInstanceDto(GameInstanceDto dto, BaseGameInstance gameInstance)
            throws ApplicationException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void customizeGameDto(GameDto gameDto, BaseGameInstance gameInstance) throws ApplicationException {
        // TODO Auto-generated method stub

    }

}
