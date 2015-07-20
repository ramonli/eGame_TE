package com.mpos.lottery.te.gamespec.game.web;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

public class GameInstanceDto extends BaseGameInstance {
    private static final long serialVersionUID = -7540219937526390962L;
    private String result;
    private Boolean snowBall;
    // associate with the entity
    private BaseGameInstance gameInstance;

    public GameInstanceDto() {
    }

    public GameInstanceDto(BaseGameInstance baseGameInstance) {
        this.gameInstance = baseGameInstance;
        this.setNumber(baseGameInstance.getNumber());
        this.setBeginTime(baseGameInstance.getBeginTime());
        this.setEndTime(baseGameInstance.getEndTime());
        this.setState(baseGameInstance.getState());
        this.setGameId(baseGameInstance.getGame() != null ? baseGameInstance.getGame().getId() : baseGameInstance
                .getGameId());
        this.setGameType(baseGameInstance.getGame() != null ? baseGameInstance.getGame().getType() : null);
        // Remind: you should assemble snowball and result at game-type specific
        // implementation.
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Boolean getSnowBall() {
        return snowBall;
    }

    public void setSnowBall(Boolean snowBall) {
        this.snowBall = snowBall;
    }

    public BaseGameInstance getGameInstance() {
        return gameInstance;
    }

    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

}
