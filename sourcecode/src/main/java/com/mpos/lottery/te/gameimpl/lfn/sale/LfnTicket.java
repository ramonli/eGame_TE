package com.mpos.lottery.te.gameimpl.lfn.sale;

import com.mpos.lottery.te.gameimpl.lfn.game.LfnGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTamperProofTicket;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "LFN_TE_TICKET")
@AttributeOverrides({ @AttributeOverride(name = "mobile", column = @Column(name = "MOBLE_NO")) })
public class LfnTicket extends BaseTamperProofTicket {
    private static final long serialVersionUID = -7218604671975254477L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LFN_GAME_INSTANCE_ID", nullable = false)
    private LfnGameInstance gameInstance;

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (LfnGameInstance) gameInstance;
    }

}
