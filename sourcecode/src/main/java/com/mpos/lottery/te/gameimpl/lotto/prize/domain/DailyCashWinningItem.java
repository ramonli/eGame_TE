package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import com.mpos.lottery.te.common.dao.VersionEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "WINNING_DAILY_CASH")
public class DailyCashWinningItem extends VersionEntity {
    private static final long serialVersionUID = -5997504459045787538L;

    public final static int PRIZE_TYPE_CASH = 1;
    public final static int PRIZE_TYPE_OBJECT = 2;
    public final static int WINNING_TYPE_NORMAL = 1;
    public final static int WINNING_TYPE_REDO = 2;

    @Column(name = "TICKET_SERIALNO")
    private String ticketSerialNo;
    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;
    @Column(name = "PRIZE_LEVEL")
    private int prizeLevel;
    @Column(name = "PRIZE_NUMBER")
    private int numberOfPrize;
    @Column(name = "IS_VALID")
    private boolean valid;
    @Column(name = "BD_PRIZE_LEVEL_ID")
    private String prizeLevelId;
    @Column(name = "BD_PRIZE_LOGIC_ID")
    private String prizeLogicId;

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

    public int getPrizeLevel() {
        return prizeLevel;
    }

    public void setPrizeLevel(int prizeLevel) {
        this.prizeLevel = prizeLevel;
    }

    public int getNumberOfPrize() {
        return numberOfPrize;
    }

    public void setNumberOfPrize(int numberOfPrize) {
        this.numberOfPrize = numberOfPrize;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getPrizeLevelId() {
        return prizeLevelId;
    }

    public void setPrizeLevelId(String prizeLevelId) {
        this.prizeLevelId = prizeLevelId;
    }

    public String getPrizeLogicId() {
        return prizeLogicId;
    }

    public void setPrizeLogicId(String prizeLogicId) {
        this.prizeLogicId = prizeLogicId;
    }

}
