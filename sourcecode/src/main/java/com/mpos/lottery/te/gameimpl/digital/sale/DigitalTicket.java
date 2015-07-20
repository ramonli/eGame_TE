package com.mpos.lottery.te.gameimpl.digital.sale;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "TE_FD_TICKET")
public class DigitalTicket extends BaseTicket {
    private static final long serialVersionUID = -8340265506823150324L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GAME_INSTANCE_ID", nullable = false)
    private DigitalGameInstance gameInstance;

    @Override
    public BaseGameInstance getGameInstance() {
        return this.gameInstance;
    }

    @Override
    public void setGameInstance(BaseGameInstance gameInstance) {
        this.gameInstance = (DigitalGameInstance) gameInstance;
    }

    @Override
    protected void verifyExtendTxt(List<? extends BaseEntry> actualEntries) throws ApplicationException {
        // TODO Auto-generated method stub

    }

}
