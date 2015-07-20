package com.mpos.lottery.te.gameimpl.toto.domain;

import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * TE_TT_TICKET table entry
 */
@Entity
@Table(name = "TE_TT_TICKET")
@AttributeOverrides({ @AttributeOverride(name = "multipleDraws", column = @Column(name = "MULTI_DRAW")) })
public class ToToTicket extends BaseTicket {
    private static final long serialVersionUID = 5995280073953536091L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GAME_INSTANCE_ID", nullable = false)
    private ToToGameInstance gameInstance;

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (ToToGameInstance) gameInstance;
    }

}
