package com.mpos.lottery.te.gamespec.sale;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

/**
 * For temporary usage.
 * 
 * @author Ramon Li
 */
public class DummyTicket extends BaseTicket {
    private static final long serialVersionUID = 3082987773474338117L;

    /**
     * A DTO of game instance, will be converted into other specific game instance, such as Raffle game instance.
     */
    private BaseGameInstance gameInstaneDto;

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstaneDto;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstaneDto = gameInstance;
    }

}
