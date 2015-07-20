package com.mpos.lottery.te.gamespec.prize.support.luckydraw;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LD_GAME_INSTANCE")
public class LuckyGameInstance extends BaseGameInstance {
    private static final long serialVersionUID = 5053325411347864930L;
    @Column(name = "PRIZE_LOGIC_ID")
    private String prizeLogicId;

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

}
