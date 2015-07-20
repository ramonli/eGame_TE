package com.mpos.lottery.te.gameimpl.toto.domain;

import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 
 * TE_TOTO_ENTRY table's PO
 * 
 */
@Entity
@Table(name = "TE_TOTO_ENTRY")
@AttributeOverrides({ @AttributeOverride(name = "ticketSerialNo", column = @Column(name = "TICKET_SERIAL_NO")),
        @AttributeOverride(name = "selectNumber", column = @Column(name = "SELECT_TEAM")),
        @AttributeOverride(name = "totalBets", column = @Column(name = "TOTAL_BET")) })
public class ToToEntry extends BaseEntry {
    private static final long serialVersionUID = -247738724974759524L;

    /**
     * get bet amount from transfer message's selectmatches
     */
    public int calTotalBet() {
        String[] matchs = this.getSelectNumber().split(",");
        int count = 1;
        for (int i = 0; i < matchs.length; i++) {
            String[] m = matchs[i].split("\\|");
            count *= m.length;
        }
        return count;
    }
}
