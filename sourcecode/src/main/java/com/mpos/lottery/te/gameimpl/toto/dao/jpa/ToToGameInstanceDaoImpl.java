package com.mpos.lottery.te.gameimpl.toto.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.EntityNotFoundException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gameimpl.toto.dao.ToToGameInstanceDao;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToGameInstance;

import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

/**
 * toto game draw handle
 * 
 * @author Lee
 * @version [Version NO, Apr 30, 2010]
 * @since [product/Modul version]
 */
public class ToToGameInstanceDaoImpl extends BaseJpaDao implements ToToGameInstanceDao {

    /**
     * find game instance asscording to gameId and drawNo.
     */
    @Override
    @SuppressWarnings("unchecked")
    public ToToGameInstance findGameDrawByGameIdAndDrawNo(final String gameId, final String drawNo)
            throws DataAccessException {
        // effective game draw should be current game draw's status = 1 and now
        // date between start
        // selling time and end selling time
        Date nowTime = new Date();
        Query query = this.getEntityManager().createQuery(
                "from ToToGameInstance t where t.state=? and t.beginTime<? "
                        + "and t.endTime>? and t.game.id=? and t.number =?");
        query.setParameter(1, LottoGameInstance.STATE_ACTIVE);
        query.setParameter(2, nowTime);
        query.setParameter(3, nowTime);
        query.setParameter(4, gameId);
        query.setParameter(5, drawNo);
        List<ToToGameInstance> draws = query.getResultList();
        if (draws.size() <= 0) {
            throw new EntityNotFoundException(SystemException.CODE_NO_GAMEDRAW,
                    "Can not found effective GameDraw by gameId=" + gameId + ",drawNo=" + drawNo);
        } else {
            if (draws.size() > 1) {
                throw new SystemException(SystemException.CODE_INTERNAL_SERVER_ERROR,
                        "There should be only 1 GameDraw(drawNo=" + drawNo + ",gameId=" + gameId + "), actual:"
                                + draws.size());
            }
            ToToGameInstance draw = (ToToGameInstance) draws.get(0);
            draw.setGameId(gameId);
            return draw;
        }
    }

    /**
     * find game instance asscording to gameId and drawNo and OMRGameSet.
     */
    @Override
    @SuppressWarnings({ "unchecked", "deprecation" })
    public ToToGameInstance findGameDrawByGameIdAndDrawNoAndGameSet(final String gameId, final String drawNo,
            final String omrGameSet) throws DataAccessException {
        // effective game draw should be current game draw's status = 1 and now
        // date between start
        // selling time and end selling time
        Date nowTime = new Date();
        String sql = "";
        if (omrGameSet == null || "".equals(omrGameSet)) {
            sql = "from ToToGameInstance t where t.state=? and t.beginTime<? and "
                    + "t.endTime>? and t.game.id=? and t.number =? and t.omrGameSet is null";
        } else {
            sql = "from ToToGameInstance t where t.state=? and t.beginTime<? and "
                    + "t.endTime>? and t.game.id=? and t.number =? and t.omrGameSet = ?";
        }
        Query query = this.getEntityManager().createQuery(sql);
        query.setParameter(1, LottoGameInstance.STATE_ACTIVE);
        query.setParameter(2, nowTime);
        query.setParameter(3, nowTime);
        query.setParameter(4, gameId);
        query.setParameter(5, drawNo);
        if (omrGameSet != null && !"".equals(omrGameSet)) {
            query.setParameter(6, omrGameSet);
        }
        List<ToToGameInstance> draws = query.getResultList();
        if (draws.size() <= 0) {
            throw new EntityNotFoundException(SystemException.CODE_NO_GAMEDRAW,
                    "Can not found effective GameDraw by gameId=" + gameId + ",drawNo=" + drawNo + ",gameSet="
                            + omrGameSet);
        } else {
            if (draws.size() > 1) {
                throw new SystemException(SystemException.CODE_INTERNAL_SERVER_ERROR,
                        "There should be only 1 GameDraw(drawNo=" + drawNo + ",gameId=" + gameId + ",gameSet="
                                + omrGameSet + "), actual:" + draws.size());
            }
            ToToGameInstance draw = (ToToGameInstance) draws.get(0);
            draw.setGameId(gameId);
            return draw;
        }
    }

    /**
     * query matchs count by game id and drawno
     */
    @Override
    @SuppressWarnings("unchecked")
    public int getMatchsCountByGameIdAndDrawNo(final String gameId, final String drawNo) throws DataAccessException {
        StringBuffer sql = new StringBuffer();
        sql.append("select count(1) from sport_match_detail d ");
        sql.append("where d.game_instance_id = (select tgi.game_instance_id ");
        sql.append("from toto_game_instance tgi where tgi.game_id =? and tgi.draw_no =?)");
        Query query = this.getEntityManager().createNativeQuery(sql.toString());
        query.setParameter(1, gameId);
        query.setParameter(2, drawNo);
        return ((BigDecimal) query.getSingleResult()).intValue();
    }

    /**
     * get base amount by current game draw
     */
    @Override
    public BigDecimal getBaseAmoutByGameIdAndDrawNo(final String gameId, final String drawNo)
            throws DataAccessException {
        String sql = "select tgi.base_amount from toto_game_instance tgi where tgi.game_id =? and tgi.draw_no =?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, gameId);
        query.setParameter(2, drawNo);
        return (BigDecimal) query.getSingleResult();
    }

}
