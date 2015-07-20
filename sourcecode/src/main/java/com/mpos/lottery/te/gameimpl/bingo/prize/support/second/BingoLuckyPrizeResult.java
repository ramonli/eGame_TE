package com.mpos.lottery.te.gameimpl.bingo.prize.support.second;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BG_LUCKY_PRIZE_RESULT")
public class BingoLuckyPrizeResult {
    private static final long serialVersionUID = 6361772200420848609L;

    @Id
    @Column(name = "ID")
    // // create seqence TE_SEQ start with 1 increment by 1;
    // @SequenceGenerator(name="TE_SEQ", sequenceName="TE_SEQ")
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="TE_SEQ")
    private String id;

    @Column(name = "LUCKYNO")
    private String luckyNo;

    @Column(name = "BG_GAME_INSTANCE_ID")
    private String gameInstanceId;

    @Column(name = "PRIZE_LEVEL")
    private int prizeLevel;

    @Column(name = "VERSION")
    private int version;

    public String getLuckyNo() {
        return luckyNo;
    }

    public void setLuckyNo(String luckyNo) {
        this.luckyNo = luckyNo;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public int getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(int prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
