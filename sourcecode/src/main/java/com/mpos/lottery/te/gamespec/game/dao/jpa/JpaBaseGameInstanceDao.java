package com.mpos.lottery.te.gamespec.game.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JpaBaseGameInstanceDao extends BaseJpaDao implements BaseGameInstanceDao {

    @Override
    public <T extends BaseGameInstance> List<T> lookupActiveByGame(long merchantId, Class<T> clazz, String gameId) {
        String sql = "select t from " + clazz.getCanonicalName()
                + " as t, MerchantCommission c where t.state=:state and t.game.id=:gameId and "
                + "t.game.id=c.game.id and c.merchantId=:merchantId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("state", BaseGameInstance.STATE_ACTIVE);
        params.put("gameId", gameId);
        params.put("merchantId", merchantId);

        List<T> result = this.findByNamedParams(sql, params);
        if (result.size() == 0) {
            return null;
        }
        // assemble gameId
        for (BaseGameInstance gameInstance : result) {
            gameInstance.setGameId(gameId);
        }
        return result;
    }

    @Override
    public <T extends BaseGameInstance> List<T> lookupActiveByGameType(long merchantId, Class<T> clazz) {
        String sql = "select t from " + clazz.getCanonicalName()
                + " as t, MerchantCommission c where t.state=:state and t.game.state=:gameState and "
                + "t.game.id=c.game.id and c.merchantId=:merchantID ORDER by t.endTime";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("state", BaseGameInstance.STATE_ACTIVE);
        params.put("gameState", Game.STATUS_ACTIVE);
        params.put("merchantID", merchantId);

        return (List<T>) this.findByNamedParams(sql, params);
    }

    @Override
    public <T extends BaseGameInstance> T lookupByGameAndNumber(long merchantId, Class<T> clazz, String gameId,
            String number) {
        String sql = "select t from " + clazz.getCanonicalName()
                + " as t, MerchantCommission c where t.number=:number and t.game.id=:gameId "
                + "and t.game.id=c.game.id and c.merchantId=:merchantId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("number", number);
        params.put("gameId", gameId);
        params.put("merchantId", merchantId);

        List result = this.findByNamedParams(sql, params);
        if (result.size() == 0) {
            return null;
        }
        if (result.size() > 1) {
            throw new DataIntegrityViolationException("Multiple(" + result.size() + ") game instance(" + clazz
                    + ") found, either multiple game instances of game(Id=" + gameId + ") have been assigned number("
                    + number + "), or the game has been assigned to merchant(id=" + merchantId + ") multiple time.");
        }
        T gameInstance = (T) result.get(0);
        gameInstance.setGameId(gameId);
        return gameInstance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseGameInstance> List<T> lookupFutureByGameAndNumber(final long merchantId,
            final Class<T> clazz, final String gameId, final String number, final int multipleDraws)
            throws ApplicationException {
        final T currentDraw = this.lookupByGameAndNumber(merchantId, clazz, gameId, number);
        if (currentDraw == null) {
            throw new ApplicationException(SystemException.CODE_NO_GAMEDRAW, "No game instance of type(" + clazz
                    + ") found by(gameId=" + gameId + ",number=" + number
                    + ") or game hasn't been allocated to merchant(id=" + merchantId + ").");
        }
        currentDraw.setGameId(gameId);
        List<T> draws = new LinkedList<T>();
        if (multipleDraws == 1) {
            draws.add(currentDraw);
            return draws;
        }

        // lookup advanced draws
        int futureMultiple = multipleDraws - 1;
        Query query = this
                .getEntityManager()
                .createQuery(
                        "select a from " + clazz.getCanonicalName() + " as a, MerchantCommission c where a.endTime>?1 "
                                + "and (a.state=?2 or a.state=?3) and a.game.id=?4 and a.game.id=c.game.id "
                                + "and c.merchantId=?5 " + "ORDER BY a.endTime").setMaxResults(futureMultiple);
        query.setParameter(1, currentDraw.getEndTime());
        query.setParameter(2, BaseGameInstance.STATE_NEW);
        query.setParameter(3, BaseGameInstance.STATE_ACTIVE);
        query.setParameter(4, gameId);
        query.setParameter(5, merchantId);
        List<T> futureDraws = query.getResultList();
        if (logger.isDebugEnabled()) {
            logger.debug("Query future game instance(" + clazz + ") by(number=" + number + ",gameId=" + gameId
                    + ", multipleDraws=" + multipleDraws + ", state=" + LottoGameInstance.STATE_NEW + "||"
                    + LottoGameInstance.STATE_ACTIVE + "):" + futureDraws.size());
        }
        for (T gameInstance : futureDraws) {
            gameInstance.setGameId(gameId);
        }

        futureDraws.add(0, currentDraw);
        return futureDraws;
    }

}
