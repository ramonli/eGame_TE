package com.mpos.lottery.te.gameimpl.digital.prize;

import com.mpos.lottery.te.gamespec.prize.BaseWinningStatistics;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "FD_WINNING_STATISTICS")
public class DigitalWinningStatistics extends BaseWinningStatistics {
    private static final long serialVersionUID = -3190791028910278091L;
}
