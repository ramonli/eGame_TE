package com.mpos.lottery.te.gameimpl.lfn.game;

import com.mpos.lottery.te.gamespec.game.BaseGameResult;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "LFN_GAME_RESULTS")
public class LfnGameResult extends BaseGameResult {

    private static final long serialVersionUID = -2208683880388631408L;

}
