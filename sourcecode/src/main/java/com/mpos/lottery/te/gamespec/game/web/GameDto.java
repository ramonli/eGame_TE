package com.mpos.lottery.te.gamespec.game.web;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class GameDto implements java.io.Serializable {
    private static final long serialVersionUID = 8698463464559675999L;
    // gameId
    private String id;
    private Integer gameType;
    private BigDecimal baseAmount;
    private List<GameInstanceDto> gameInstanceDtos = new LinkedList<GameInstanceDto>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getGameType() {
        return gameType;
    }

    public void setGameType(Integer gameType) {
        this.gameType = gameType;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public List<GameInstanceDto> getGameInstanceDtos() {
        return gameInstanceDtos;
    }

    public void setGameInstanceDtos(List<GameInstanceDto> gameInstanceDtos) {
        this.gameInstanceDtos = gameInstanceDtos;
    }

}
