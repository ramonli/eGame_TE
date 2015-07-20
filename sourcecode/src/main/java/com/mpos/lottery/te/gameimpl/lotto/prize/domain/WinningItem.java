package com.mpos.lottery.te.gameimpl.lotto.prize.domain;

import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "WINNING")
public class WinningItem extends BaseWinningItem {
    private static final long serialVersionUID = 6361772200420848609L;

}
