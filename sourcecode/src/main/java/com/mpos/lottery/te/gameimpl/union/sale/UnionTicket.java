package com.mpos.lottery.te.gameimpl.union.sale;

import com.mpos.lottery.te.gameimpl.union.game.UnionGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTamperProofTicket;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "UN_TE_TICKET")
public class UnionTicket extends BaseTamperProofTicket {
    private static final long serialVersionUID = 4046979815737212852L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GAME_INSTANCE_ID", nullable = false)
    private UnionGameInstance gameInstance;

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (UnionGameInstance) gameInstance;
    }

}
