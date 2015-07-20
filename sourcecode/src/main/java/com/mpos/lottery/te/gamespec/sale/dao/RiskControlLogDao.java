package com.mpos.lottery.te.gamespec.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.sale.RiskControlLog;

import java.math.BigDecimal;

public interface RiskControlLogDao extends DAO {

    RiskControlLog findByGameInstanceAndSelectedNumber(String gameInstanceId, String selectedNumber, int prizeLevelType);

    /**
     * By this method, we won't need to lock a row and then update it.
     * 
     * @param id
     *            THe identifier of risk control log.
     * @param amount
     *            THe amount of a single selected number.
     */
    void updateWithAmount(String id, BigDecimal amount);
}
