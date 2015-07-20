package com.mpos.lottery.te.gameimpl.toto.domain;

import com.mpos.lottery.te.gamespec.prize.BaseWinningStatistics;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TT_WINNING_STATISTICS")
public class ToToWinningStatistics extends BaseWinningStatistics {

    private static final long serialVersionUID = -2991428813176705603L;

}
