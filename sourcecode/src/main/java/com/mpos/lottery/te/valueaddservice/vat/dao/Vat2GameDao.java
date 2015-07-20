package com.mpos.lottery.te.valueaddservice.vat.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Game;

public interface Vat2GameDao extends DAO {

    /**
     * Find {@code Vat2Game} by vat, businessType and status(valid).
     */
    Vat2Game findByVatAndBizType(String vatId, String bizType);
}
