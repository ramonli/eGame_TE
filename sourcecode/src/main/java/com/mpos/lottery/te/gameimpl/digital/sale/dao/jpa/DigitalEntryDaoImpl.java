package com.mpos.lottery.te.gameimpl.digital.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;

import com.mpos.lottery.te.gameimpl.digital.sale.DigitalEntry;
import com.mpos.lottery.te.gameimpl.digital.sale.dao.DigitalEntryDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;

@Repository("digitalEntryDao")
public class DigitalEntryDaoImpl extends BaseJpaDao implements DigitalEntryDao {

    @Override
    public List<DigitalEntry> findByTicketSerialNo(String serialNo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ticketSerialNo", serialNo);
        List<DigitalEntry> result = this.findByNamedParams(
                "from DigitalEntry e where e.ticketSerialNo=:ticketSerialNo", params);
        return result;
    }

}
