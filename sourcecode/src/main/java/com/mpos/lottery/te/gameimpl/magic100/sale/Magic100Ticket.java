package com.mpos.lottery.te.gameimpl.magic100.sale;

import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.DummyTicket;

import org.springframework.beans.BeanUtils;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "LK_TE_TICKET")
@AttributeOverrides({ @AttributeOverride(name = "mobile", column = @Column(name = "MOBLE_NO")) })
public class Magic100Ticket extends BaseTicket {
    private static final long serialVersionUID = 2217898628945269742L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LK_GAME_INSTANCE_ID", nullable = false)
    private Magic100GameInstance gameInstance;

    public Magic100Ticket() {
    }

    public Magic100Ticket(DummyTicket ticketDto) {
        BeanUtils.copyProperties(ticketDto, this, "gameInstance", "entries");
        this.getEntries().clear();
        // translate game instance
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        BeanUtils.copyProperties(ticketDto.getGameInstance(), gameInstance);
        this.setGameInstance(gameInstance);
        // translate entries
        for (BaseEntry entry : ticketDto.getEntries()) {
            Magic100Entry mEntry = new Magic100Entry();
            BeanUtils.copyProperties(entry, mEntry);
            this.getEntries().add(mEntry);
        }
    }

    public static Magic100Ticket defaultTicket() {
        Magic100Ticket ticket = new Magic100Ticket();

        ticket.getEntries().add(Magic100Entry.defaultEntry());

        return ticket;
    }

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (Magic100GameInstance) gameInstance;
    }

}
