package com.mpos.lottery.te.gamespec.game;

import com.mpos.lottery.te.port.Context;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@SuppressWarnings("serial")
@MappedSuperclass
public class BaseGameResult implements Serializable {
    @Id
    @Column(name = "GAME_RESULT_ID")
    private String id;

    @Column(name = "RESULT_NO")
    private String baseNumber;

    @Column(name = "SPECIA_NO")
    private int specialNumber = Context.UNINITIAL_VALUE;

    @Column(name = "GAME_INSTANCE_ID", nullable = false)
    private String gameInstanceId;

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

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

}
