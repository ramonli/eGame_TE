package com.mpos.lottery.te.gameimpl.lotto.draw.domain;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "GAME_INSTANCE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "GAME_INSTANCE_ID")) })
public class LottoGameInstance extends BaseGameInstance implements java.io.Serializable {
    private static final long serialVersionUID = 7571508149815755956L;

    @Column(name = "IS_SNOWBALL")
    private boolean isSnowBall;

    @Column(name = "OBJECT_PRIZE_LOGIC_ID")
    private String objectPrizeLogicId;

    @Column(name = "PRIZE_LOGIC_ID")
    private String prizeLogicId;

    // @OneToOne(mappedBy="gameDraw",optional=true)
    @Transient
    private GameResult result;

    @Transient
    private String gameId;

    @Transient
    private String gameTypeId;

    // @Column(name="WINNER_ANALYSED")
    // private int winAnalysisStatus;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public boolean isSnowBall() {
        return isSnowBall;
    }

    public void setSnowBall(boolean isSnowBall) {
        this.isSnowBall = isSnowBall;
    }

    public GameResult getResult() {
        return result;
    }

    public void setResult(GameResult result) {
        this.result = result;
    }

    public String getGameTypeId() {
        return gameTypeId;
    }

    public void setGameTypeId(String gameTypeId) {
        this.gameTypeId = gameTypeId;
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
