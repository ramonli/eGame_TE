package com.mpos.lottery.te.gameimpl.raffle.sale;

import com.mpos.lottery.te.gameimpl.raffle.game.RaffleGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.DummyTicket;

import org.springframework.beans.BeanUtils;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "RA_TE_TICKET")
public class RaffleTicket extends BaseTicket {
    private static final long serialVersionUID = -1793517848131301537L;

    public RaffleTicket() {
    }

    public RaffleTicket(DummyTicket ticketDto) {
        BeanUtils.copyProperties(ticketDto, this, "gameInstance");
        // translate game instance
        RaffleGameInstance gameInstance = new RaffleGameInstance();
        BeanUtils.copyProperties(ticketDto.getGameInstance(), gameInstance);
        this.setGameInstance(gameInstance);
    }

    public static RaffleTicket defaultTicket() {
        RaffleTicket ticket = new RaffleTicket();
        ticket.getEntries().add(BaseEntry.defaultEntry());

        return ticket;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RA_GAME_INSTANCE_ID", nullable = false)
    private RaffleGameInstance gameInstance;

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (RaffleGameInstance) gameInstance;
    }

}
