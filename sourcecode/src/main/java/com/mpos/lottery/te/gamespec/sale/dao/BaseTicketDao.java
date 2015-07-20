package com.mpos.lottery.te.gamespec.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.util.List;

public interface BaseTicketDao extends DAO {
    <T extends BaseTicket> List<T> findBySerialNo(Class<T> clazz, String seiralNo, boolean allowNull)
            throws ApplicationException;

    <T extends BaseTicket> List<T> findByTransaction(Class<T> clazz, String transactionId);
}
