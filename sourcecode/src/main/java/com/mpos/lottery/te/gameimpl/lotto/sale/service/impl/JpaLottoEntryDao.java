package com.mpos.lottery.te.gameimpl.lotto.sale.service.impl;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gamespec.sale.dao.jpa.JpaBaseEntryDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("jpaLottoEntryDao")
public class JpaLottoEntryDao extends JpaBaseEntryDao implements LottoEntryDao {

    @Override
    public List<LottoEntry> findBySerialNoAndMultiCount(String serialNo) {
        String sql = "from LottoEntry as t where t.ticketSerialNo=:serialNo and t.multipleCount>0 order by t.entryNo";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNo", serialNo);
        return (List<LottoEntry>) this.findByNamedParams(sql, params);
    }

    @Override
    public List<LottoEntry> findBySerialNo(String serialNo) {
        String sql = "from LottoEntry as t where t.ticketSerialNo=:serialNo  order by t.entryNo";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNo", serialNo);
        return (List<LottoEntry>) this.findByNamedParams(sql, params);
    }

}
