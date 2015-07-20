package com.mpos.lottery.te.gamespec.game.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.game.Game;

import java.util.List;

public interface GameDao extends DAO {

    List<Game> findByType(int gameType);

    /**
     * Find a single game by given type even multiple games found. The 1st entity will be returned.
     * 
     * @param gameType
     *            The type of game.
     * @param allowNull
     *            can null be returned if no game found? if false, SystemException will be thrown out.
     * @return A sigle game entity.
     */
    Game findSingleByType(int gameType, boolean allowNull);

    Game findByGamdId(String gameId);
}
