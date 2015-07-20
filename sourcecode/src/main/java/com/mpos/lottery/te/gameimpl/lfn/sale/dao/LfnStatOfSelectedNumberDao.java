package com.mpos.lottery.te.gameimpl.lfn.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.sale.StatOfSelectedNumber;

import java.util.List;

public interface LfnStatOfSelectedNumberDao extends DAO {

    /**
     * Lookup the latest used selected number.
     * 
     * @param gameInstanceId
     *            The game instance id.
     * @param rows
     *            How many numbers should be returned.
     * @return all matched results.
     */
    List<StatOfSelectedNumber> findByGameInstance(String gameInstanceId, int rows);
}
