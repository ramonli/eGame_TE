package com.mpos.lottery.te.gameimpl.raffle.game;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "RA_GAME_INSTANCE")
public class RaffleGameInstance extends BaseGameInstance {
    private static final long serialVersionUID = 3570795105743925880L;

    @Column(name = "BD_PRIZE_LOGIC_ID")
    private String prizeLogicId;

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

}
