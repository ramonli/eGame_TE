package com.mpos.lottery.te.gameimpl.bingo.game;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BG_GAME_INSTANCE")
public class BingoGameInstance extends BaseGameInstance {
    private static final long serialVersionUID = -2030350918065638606L;

    // @Column(name = "PAYOUT_START_TIME")
    // @Temporal(TemporalType.TIMESTAMP)
    // private Date payoutStartTime;
    //
    @Column(name = "BG_LUCKY_PRIZE_LOGIC_ID")
    private String luckyPrizeLogicId;

    @Column(name = "START_NUMBER_SEQ")
    private long startOfSequence;
    @Column(name = "END_NUMBER_SEQ")
    private long endOfSequence;
    @Column(name = "CURRENT_SEQUENCE")
    private long currentSequence;

    public String getLuckyPrizeLogicId() {
        return luckyPrizeLogicId;
    }

    public void setLuckyPrizeLogicId(String luckyPrizeLogicId) {
        this.luckyPrizeLogicId = luckyPrizeLogicId;
    }

    public long getStartOfSequence() {
        return startOfSequence;
    }

    public void setStartOfSequence(long startOfSequence) {
        this.startOfSequence = startOfSequence;
    }

    public long getEndOfSequence() {
        return endOfSequence;
    }

    public void setEndOfSequence(long endOfSequence) {
        this.endOfSequence = endOfSequence;
    }

    public long getCurrentSequence() {
        return currentSequence;
    }

    public void setCurrentSequence(long currentSequence) {
        this.currentSequence = currentSequence;
    }

}
