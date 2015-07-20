package com.mpos.lottery.te.gameimpl.magic100.sale;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This entity will be used to trace the current and next sequence of lucky number.
 * 
 * @author Ramon
 */
@Entity
@Table(name = "LK_OFFLINE_PRIZE_STATUS")
public class OffLuckyNumberSequence implements Serializable {
    private static final long serialVersionUID = -5547782922325333017L;
    // there should be only one instance, and the ID will be fixed to 1.
    public static final String ID = "1";

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "GAME_ID")
    private String gameId;
    /**
     * For each successful Get Reserved Numbers transaction, it will lookup lucky number by <code>nextSequence</code>,
     * and then update <code>nextSequence</code> by increasing it by 1 if needed(not always updated it, as a lucky
     * number maybe sold multiple times in a single cycle).
     * <p>
     * Cancellation request won't affect <code>nextSequence</code>, as cancelled lucky number will be postponed to sell
     * at next cycle.
     * <p>
     * The <code>nextSequence</code> maps to <code>LuckyNumber.sequenceOfNumber</code>;
     */
    @Column(name = "NEXT_SEQ")
    private long nextSequence;
    /**
     * Who is the latest player? <code>latestPlayer</code> will be set if <code>luckyNumber.cancelCounter>0</code>, and
     * will be set to null if <code>nextSequence</code> changed.
     */
    @Column(name = "LAST_BUYER")
    private String lastestPlayer;

    public OffLuckyNumberSequence() {
    }

    public OffLuckyNumberSequence(long nextSequence) {
        super();
        this.id = ID;
        this.nextSequence = nextSequence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getNextSequence() {
        return nextSequence;
    }

    public void setNextSequence(long nextSequence) {
        this.nextSequence = nextSequence;
    }

    public String getLastestPlayer() {
        return lastestPlayer;
    }

    public void setLastestPlayer(String lastestPlayer) {
        this.lastestPlayer = lastestPlayer;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

}
