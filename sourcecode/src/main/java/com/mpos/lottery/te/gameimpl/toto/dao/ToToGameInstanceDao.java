package com.mpos.lottery.te.gameimpl.toto.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToGameInstance;

import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;

/**
 * toto game instance db handle interface
 */
public interface ToToGameInstanceDao extends DAO {
    /**
     * find game instance asscording to gameId and drawNo.
     */
    public ToToGameInstance findGameDrawByGameIdAndDrawNo(String gameId, String drawNo) throws DataAccessException;

    /**
     * find game instance asscording to gameId and drawNo and OMRGameSet.
     */
    public ToToGameInstance findGameDrawByGameIdAndDrawNoAndGameSet(String gameId, String drawNo, String omrGameSet)
            throws DataAccessException;

    /**
     * query matchs count by game id and drawno
     */
    public int getMatchsCountByGameIdAndDrawNo(String gameId, String drawNo) throws DataAccessException;

    /**
     * get base amount by current game draw
     */
    public BigDecimal getBaseAmoutByGameIdAndDrawNo(String gameId, String drawNo) throws DataAccessException;

}
