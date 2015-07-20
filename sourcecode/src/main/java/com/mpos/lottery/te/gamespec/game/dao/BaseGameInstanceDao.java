package com.mpos.lottery.te.gamespec.game.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;

import java.util.List;

public interface BaseGameInstanceDao extends DAO {

    /**
     * Lookup all active game instances by given game.
     */
    <T extends BaseGameInstance> List<T> lookupActiveByGame(long merchantId, Class<T> clazz, String gameId);

    /**
     * Lookup all active game instances by given game type. The <code>clazz</code> has told the game type information.
     */
    <T extends BaseGameInstance> List<T> lookupActiveByGameType(long mercantId, Class<T> clazz);

    <T extends BaseGameInstance> T lookupByGameAndNumber(long merchantId, Class<T> clazz, String gameId, String number);

    /**
     * Lookup multiple sequential game instances since the game instance with given number. For example if the given
     * draw number is 2009002, and <code>multipleDraws</code> is 3, then this method will try to lookup 3 sequential
     * game instaces(includes draw 2009002) since instance(22009002)
     * 
     * @param clazz
     *            The game instance type. Different game type may require different game instance type.
     * @param gameId
     *            The game id.
     * @param number
     *            The draw number.
     * @param multipleDraws
     *            How many sequential game instances are required?
     * @return multiple sequential game instances.
     */
    <T extends BaseGameInstance> List<T> lookupFutureByGameAndNumber(long merchantId, Class<T> clazz, String gameId,
            String number, int multipleDraws) throws ApplicationException;
}
