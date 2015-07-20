package com.mpos.lottery.te.gameimpl.union.game;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "UN_GAME_INSTANCE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "GAME_INSTANCE_ID")) })
public class UnionGameInstance extends BaseGameInstance implements java.io.Serializable {
    private static final long serialVersionUID = 7571508149815755956L;

    @Column(name = "IS_SNOWBALL")
    private boolean isSnowBall;

    @Column(name = "OBJECT_PRIZE_LOGIC_ID")
    private String objectPrizeLogicId;

    @Column(name = "PRIZE_LOGIC_ID")
    private String prizeLogicId;

    public boolean isSnowBall() {
        return isSnowBall;
    }

    public void setSnowBall(boolean isSnowBall) {
        this.isSnowBall = isSnowBall;
    }

    public String getObjectPrizeLogicId() {
        return objectPrizeLogicId;
    }

    public void setObjectPrizeLogicId(String objectPrizeLogicId) {
        this.objectPrizeLogicId = objectPrizeLogicId;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

}
