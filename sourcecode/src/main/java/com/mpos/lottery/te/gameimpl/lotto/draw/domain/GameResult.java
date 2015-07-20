package com.mpos.lottery.te.gameimpl.lotto.draw.domain;

import com.mpos.lottery.te.port.Context;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity(name = "GAME_RESULTS")
public class GameResult implements Serializable {
    private static final long serialVersionUID = -4117928817097174910L;

    @Id
    @Column(name = "GAME_RESULT_ID")
    private String id;

    @Column(name = "RESULT_NO")
    private String baseNumber;

    @Column(name = "SPECIA_NO")
    private int specialNumber = Context.UNINITIAL_VALUE;

    @OneToOne
    @JoinColumn(name = "GAME_INSTANCE_ID", nullable = false, unique = true)
    private LottoGameInstance gameDraw;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBaseNumber() {
        return baseNumber;
    }

    public void setBaseNumber(String baseNumber) {
        this.baseNumber = baseNumber;
    }

    public int getSpecialNumber() {
        return specialNumber;
    }

    public void setSpecialNumber(int specialNumber) {
        this.specialNumber = specialNumber;
    }

    public LottoGameInstance getGameDraw() {
        return gameDraw;
    }

    public void setGameDraw(LottoGameInstance gameDraw) {
        this.gameDraw = gameDraw;
    }

}
