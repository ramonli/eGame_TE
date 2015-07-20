package com.mpos.lottery.te.gameimpl.bingo.sale;

import com.mpos.lottery.te.common.dao.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BG_ENTRY_REF")
public class BingoEntryRef extends BaseEntity implements Cloneable {
    private static final long serialVersionUID = -1770036212852123452L;

    @Column(name = "SELECTED_NUMBER")
    private String selectedNumber;

    /**
     * Refer to BingoTicketRef.STATUS_XXX
     */
    @Column(name = "STATUS")
    private int status;

    @Column(name = "BG_GAME_INSTANCE_ID")
    private String gameInstanceId;

    public String getSelectedNumber() {
        return selectedNumber;
    }

    public void setSelectedNumber(String selectedNumber) {
        this.selectedNumber = selectedNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getGameInstanceId() {
        return gameInstanceId;
    }

    public void setGameInstanceId(String gameInstanceId) {
        this.gameInstanceId = gameInstanceId;
    }

}
