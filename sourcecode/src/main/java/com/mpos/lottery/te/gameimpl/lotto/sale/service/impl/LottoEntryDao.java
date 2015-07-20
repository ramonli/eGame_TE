package com.mpos.lottery.te.gameimpl.lotto.sale.service.impl;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;

import java.util.List;

public interface LottoEntryDao extends BaseEntryDao {

    /**
     * Retrieve all entries of a ticket and the <code>multiCount</code> of a entry should be greater than 0.
     */
    List<LottoEntry> findBySerialNoAndMultiCount(String serialNo);

    /**
     * Retrieve all entries of a ticket.
     * */
    public List<LottoEntry> findBySerialNo(String serialNo);
}
