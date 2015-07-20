package com.mpos.lottery.te.gameimpl.bingo.prize;

import com.mpos.lottery.te.gamespec.prize.BaseWinningStatistics;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BG_WINNING_STATISTICS")
@AttributeOverrides({ @AttributeOverride(name = "gameInstanceId", column = @Column(name = "BG_GAME_INSTANCE_ID")) })
public class BingoWinningStatistics extends BaseWinningStatistics {
    private static final long serialVersionUID = 9008977694315669990L;

}
