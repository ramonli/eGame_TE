package com.mpos.lottery.te.gamespec.prize;

import com.mpos.lottery.te.common.dao.VersionEntity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * A base winning item for all game type.
 * 
 * @author Ramon Li
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class BaseWinningItem extends VersionEntity {
    @Column(name = "TICKET_SERIALNO")
    private String ticketSerialNo;

    @Column(name = "GAME_INSTANCE_ID")
    private String gameInstanceId;

    @Column(name = "ENTRY_ID")
    private String entryId;

    @Column(name = "IS_VALID")
    private boolean valid;

    @Column(name = "PRIZE_LEVEL")
    private int prizeLevel;

    @Column(name = "PRIZE_NUMBER")
    private int numberOfPrize;

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

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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

}
