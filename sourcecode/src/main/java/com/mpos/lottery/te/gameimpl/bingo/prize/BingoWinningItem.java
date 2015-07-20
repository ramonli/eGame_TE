package com.mpos.lottery.te.gameimpl.bingo.prize;

import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BG_WINNING")
@AttributeOverrides({ @AttributeOverride(name = "ticketSerialNo", column = @Column(name = "SERIAL_NO")),
        @AttributeOverride(name = "gameInstanceId", column = @Column(name = "BG_GAME_INSTANCE_ID")),
        @AttributeOverride(name = "entryId", column = @Column(name = "TE_BG_ENTRY_ID")) })
public class BingoWinningItem extends BaseWinningItem {
    private static final long serialVersionUID = 6361772200420848609L;

}
