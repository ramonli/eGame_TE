package com.mpos.lottery.te.valueaddservice.vat.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Merchant;

public interface Vat2MerchantDao extends DAO {

    /**
     * Find {@code Vat2Merhant} by vat, merchant, and status(valid).
     */
    Vat2Merchant findByVatAndMerchant(String vatId, long merchantId);
}
