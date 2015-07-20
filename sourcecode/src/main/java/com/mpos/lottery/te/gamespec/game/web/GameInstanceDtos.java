package com.mpos.lottery.te.gamespec.game.web;

import java.util.LinkedList;
import java.util.List;

public class GameInstanceDtos implements java.io.Serializable {
    private static final long serialVersionUID = 7730544837915400132L;
    private List<GameDto> gameDtos = new LinkedList<GameDto>();

    public List<GameDto> getGameDtos() {
        return gameDtos;
    }

    public void setGameDtos(List<GameDto> gameDtos) {
        this.gameDtos = gameDtos;
    }

    public void add(GameDto gameDto) {
        GameDto exist = null;
        for (GameDto g : this.gameDtos) {
            if (g.getId().equals(gameDto.getId())) {
                exist = g;
                exist.getGameInstanceDtos().addAll(gameDto.getGameInstanceDtos());
            }
        }
        if (exist == null) {
            this.getGameDtos().add(gameDto);
        }
    }
}
