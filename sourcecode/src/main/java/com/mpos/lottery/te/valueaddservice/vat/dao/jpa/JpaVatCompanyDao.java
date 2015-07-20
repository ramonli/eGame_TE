package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.vat.VatCompany;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatCompanyDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("vatCompanyDao")
public class JpaVatCompanyDao extends BaseJpaDao implements VatCompanyDao {

    @Override
    public VatCompany findByMerchant(long merchantId) {
        String sql = "from VatCompany d where d.merchantId=:merchantId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        List result = this.findByNamedParams(sql, params);
        return (VatCompany) this.single(result, true);
    }

    @Override
    public VatCompany findByTaxNo(String taxNo) {
        String sql = "select d from VatCompany d, Merchant m where d.merchantId=m.id and m.taxNo=:taxNo";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taxNo", taxNo);
        List result = this.findByNamedParams(sql, params);
        return (VatCompany) this.single(result, true);
    }

}
