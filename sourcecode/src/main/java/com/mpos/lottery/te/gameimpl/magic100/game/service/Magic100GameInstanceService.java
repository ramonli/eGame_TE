package com.mpos.lottery.te.gameimpl.magic100.game.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.service.impl.AbstractGameInstanceService;
import com.mpos.lottery.te.gamespec.game.web.GameDto;
import com.mpos.lottery.te.gamespec.game.web.GameInstanceDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.util.List;

public class Magic100GameInstanceService extends AbstractGameInstanceService {

    protected void customVerifySaleReadyGameInstance(BaseGameInstance currentGameInstance) throws ApplicationException {
        Magic100GameInstance gameInstance = (Magic100GameInstance) currentGameInstance;
        // for magic100 game, 'is_suspend_payout' is used to represent
        // suspending sale.
        if (gameInstance.isPayoutBlocked()) {
            throw new ApplicationException(SystemException.CODE_NOT_ACTIVE_DRAW, "Game instance(id="
                    + gameInstance.getId() + ") is suspended.");
        }
    }

    @Override
    public void allowPayout(Context respCtx, List<? extends BaseTicket> hostTickets, boolean isEnquiry)
            throws ApplicationException {

    }

    @Override
    public GameType supportedGameType() {
        return GameType.LUCKYNUMBER;
    }

    @Override
    protected void customizeGameInstanceDto(GameInstanceDto dto, BaseGameInstance gameInstance)
            throws ApplicationException {
    }

    @Override
    protected void customizeGameDto(GameDto gameDto, BaseGameInstance gameInstance) throws ApplicationException {
    }

}
