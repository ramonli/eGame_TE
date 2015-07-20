package com.mpos.lottery.te.gameimpl.union.prize;

import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "UN_WINNING")
public class UnionWinningItem extends BaseWinningItem {
    private static final long serialVersionUID = 6361772200420848609L;

}
