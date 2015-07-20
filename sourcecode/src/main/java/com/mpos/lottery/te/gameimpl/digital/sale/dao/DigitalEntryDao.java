package com.mpos.lottery.te.gameimpl.digital.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalEntry;

import java.util.List;

public interface DigitalEntryDao extends DAO {

    /**
     * Lookup the latest used selected number.
     */
    List<DigitalEntry> findByTicketSerialNo(String serialNo);
}
