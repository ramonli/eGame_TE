package com.mpos.lottery.te.gameimpl.digital.game;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "FD_GAME_INSTANCE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "GAME_INSTANCE_ID")) })
public class DigitalGameInstance extends BaseGameInstance {
    private static final long serialVersionUID = 4340559688117719908L;

    @Column(name = "PRIZE_LOGIC_ID")
    private String prizeLogicId;

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

}
