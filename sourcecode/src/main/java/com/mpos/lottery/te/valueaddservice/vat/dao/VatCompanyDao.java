package com.mpos.lottery.te.valueaddservice.vat.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.vat.VatCompany;

public interface VatCompanyDao extends DAO {

    VatCompany findByMerchant(long merchantId);

    VatCompany findByTaxNo(String taxNo);
}
