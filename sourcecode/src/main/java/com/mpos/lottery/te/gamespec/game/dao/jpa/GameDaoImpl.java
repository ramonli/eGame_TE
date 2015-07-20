package com.mpos.lottery.te.gamespec.game.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.dao.GameDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GameDaoImpl extends BaseJpaDao implements GameDao {

    @Override
    public List<Game> findByType(int gameType) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", gameType);
        return this.findByNamedParams("from Game g where g.type=:type", params);
    }

    @Override
    public Game findSingleByType(int gameType, boolean allowNull) {
        List<Game> games = this.findByType(gameType);
        if (games.size() == 0) {
            if (allowNull) {
                return null;
            } else {
                throw new SystemException("No game found by type(" + gameType + ").");
            }
        } else {
            return games.get(0);
        }
    }

    @Override
    public Game findByGamdId(String gameId) {
        return this.findById(Game.class, gameId);
    }
}
