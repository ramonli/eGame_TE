package com.mpos.lottery.te.gameimpl.raffle.prize;

import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "RA_WINNING_OBJECT")
public class RaffleWinning extends BaseWinningItem {

    private static final long serialVersionUID = -2809218414291866484L;

}
