package com.mpos.lottery.te.gameimpl.extraball.sale;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "EB_GAME_INSTANCE")
@AttributeOverrides({ @AttributeOverride(name = "prizeLogicId", column = @Column(name = "EB_PRIZE_LOGIC_ID")) })
public class ExtraBallGameInstance extends BaseGameInstance {
    private static final long serialVersionUID = -9133544938514257255L;

}
