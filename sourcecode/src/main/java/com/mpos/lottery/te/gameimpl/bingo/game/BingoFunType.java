package com.mpos.lottery.te.gameimpl.bingo.game;

import com.mpos.lottery.te.gamespec.game.BaseFunType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BG_FUN_TYPE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "LFT_ID")) })
public class BingoFunType extends BaseFunType {
    private static final long serialVersionUID = 2403549400130459677L;

    @Column(name = "ENTRY_COUNT")
    private int maxEntriesInTicket;

    public int getMaxEntriesInTicket() {
        return maxEntriesInTicket;
    }

    public void setMaxEntriesInTicket(int maxEntriesInTicket) {
        this.maxEntriesInTicket = maxEntriesInTicket;
    }

}
