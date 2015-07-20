package com.mpos.lottery.te.gameimpl.instantgame.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;

public interface InstantGameDrawDao extends DAO {

    InstantGameDraw getByName(String name);
}
