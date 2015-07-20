package com.mpos.lottery.te.valueaddservice.vat.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.vat.VAT;

public interface VatDao extends DAO {

    VAT findByCode(String code);
}
