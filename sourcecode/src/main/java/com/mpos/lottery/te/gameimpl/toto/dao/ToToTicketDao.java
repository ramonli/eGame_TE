package com.mpos.lottery.te.gameimpl.toto.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;

import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;

/**
 * toto game sell ticket db handle interface
 */
public interface ToToTicketDao extends DAO {
    /**
     * get all matchs option type to use valid sell ticket selectteam param
     */
    public List<String> getBetOptionType(String gameId, String drawNum) throws DataAccessException;

    /**
     * get toto ticket information according to serialNo
     */
    public List<ToToTicket> getToToTicketBySerialNo(String serialNo) throws DataAccessException;

    /**
     * get all toto triple info
     */
    public Map<Integer, Integer[]> getToToOperatorParameters(String gameId) throws DataAccessException;

}
