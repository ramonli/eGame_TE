package com.mpos.lottery.te.gameimpl.bingo.game.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.service.impl.AbstractGameInstanceService;
import com.mpos.lottery.te.gamespec.game.web.GameDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;

public class BingoGameInstanceService extends AbstractGameInstanceService {

    @Override
    public GameType supportedGameType() {
        return GameType.BINGO;
    }

    @Override
    protected void customizeGameDto(GameDto gameDto, BaseGameInstance gameInstance) throws ApplicationException {
        // do nothing
    }

    @Override
    protected void customizeGameInstanceDto(GameInstanceDto dto, BaseGameInstance gameInstance)
            throws ApplicationException {
        // do nothing
    }
}
