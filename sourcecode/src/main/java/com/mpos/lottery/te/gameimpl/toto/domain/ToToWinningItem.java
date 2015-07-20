package com.mpos.lottery.te.gameimpl.toto.domain;

import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TT_WINNING")
public class ToToWinningItem extends BaseWinningItem {
    private static final long serialVersionUID = 2375188682036203780L;
}
