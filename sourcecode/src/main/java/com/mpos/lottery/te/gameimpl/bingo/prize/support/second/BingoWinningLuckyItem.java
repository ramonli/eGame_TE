package com.mpos.lottery.te.gameimpl.bingo.prize.support.second;

import com.mpos.lottery.te.common.dao.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BG_WINNING_LUCKY")
public class BingoWinningLuckyItem extends BaseEntity {
    private static final long serialVersionUID = 6361772200420848609L;

    @Column(name = "SERIAL_NO")
    private String ticketSerialNo;

    @Column(name = "BG_GAME_INSTANCE_ID")
    private String gameInstanceId;

    @Column(name = "LUCKYNO")
    private String luckyNo;

    @Column(name = "VERSION")
    private int version;

    public String getLuckyNo() {
        return luckyNo;
    }

    public void setLuckyNo(String luckyNo) {
        this.luckyNo = luckyNo;
    }

    public String getTicketSerialNo() {
        return ticketSerialNo;
    }

    public void setTicketSerialNo(String ticketSerialNo) {
        this.ticketSerialNo = ticketSerialNo;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
