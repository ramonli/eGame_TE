package com.mpos.lottery.te.gamespec.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import java.util.List;

public interface BaseEntryDao extends DAO {

    <T extends BaseEntry> List<T> findByTicketSerialNo(Class<T> clazz, String serialNo, boolean allowNull)
            throws ApplicationException;
}
