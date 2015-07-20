package com.mpos.lottery.te.gameimpl.digital.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.sale.StatOfSelectedNumber;

import java.util.List;

public interface DigitalStatOfSelectedNumberDao extends DAO {

    /**
     * Lookup the latest used selected number.
     * 
     * @param gameInstanceId
     *            The game instance id.
     * @return all matched results.
     */
    List<StatOfSelectedNumber> findByGameInstance(String gameInstanceId);
}
