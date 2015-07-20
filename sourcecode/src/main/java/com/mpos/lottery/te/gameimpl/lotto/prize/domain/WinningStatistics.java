package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import com.mpos.lottery.te.gamespec.prize.BaseWinningStatistics;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "WINNING_STATISTICS")
public class WinningStatistics extends BaseWinningStatistics {
    private static final long serialVersionUID = 9008977694315669990L;

}
