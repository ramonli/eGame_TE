package com.mpos.lottery.te.gameimpl.union.prize;

import com.mpos.lottery.te.gamespec.prize.BaseWinningStatistics;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "UN_WINNING_STATISTICS")
public class UnionWinningStatistics extends BaseWinningStatistics {
    private static final long serialVersionUID = 9008977694315669990L;

}
