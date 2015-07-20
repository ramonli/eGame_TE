package com.mpos.lottery.te.gameimpl.bingo.sale;

import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * At present, only single bet option is supported.
 * 
 * @author Ramon
 * 
 */
@Entity
@Table(name = "TE_BG_ENTRY")
public class BingoEntry extends BaseEntry implements Cloneable {
    private static final long serialVersionUID = -2283333105080432558L;

    @Column(name = "ENTRY_REF_ID")
    private String entryRefId;

    @Column(name = "TE_BG_TICKET_ID")
    private String ticketId;
    @Column(name = "BG_GAME_INSTANCE_ID")
    private String gameInstanceId;

    public BingoEntry() {
    }

    public BingoEntry(BingoEntryRef entryRef) {
        this.setEntryRefId(entryRef.getId());
        this.setBetOption(BETOPTION_SINGLE);
        this.setInputChannel(INPUT_CHANNEL_QP_NOTOMR);
        this.setSelectNumber(entryRef.getSelectedNumber());
    }

    public String getEntryRefId() {
        return entryRefId;
    }

    public void setEntryRefId(String entryRefId) {
        this.entryRefId = entryRefId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

}
