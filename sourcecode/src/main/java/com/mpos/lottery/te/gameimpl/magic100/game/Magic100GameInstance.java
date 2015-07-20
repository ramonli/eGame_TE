package com.mpos.lottery.te.gameimpl.magic100.game;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LK_GAME_INSTANCE")
public class Magic100GameInstance extends BaseGameInstance {

    private static final long serialVersionUID = 4329860514975050567L;
    @Column(name = "START_NUMBER_SEQ")
    private long startOfSequence;
    @Column(name = "END_NUMBER_SEQ")
    private long endOfSequence;

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

}
