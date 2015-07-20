package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Merchant;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2MerchantDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("vat2MerchantDao")
public class JpaVat2MerchantDao extends BaseJpaDao implements Vat2MerchantDao {

    @Override
    public Vat2Merchant findByVatAndMerchant(String vatId, long merchantId) {
        String sql = "from Vat2Merchant d where d.vatId=:vatId and d.merchantId=:merchantId and d.status="
                + Vat2Merchant.STATUS_VALID;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vatId", vatId);
        params.put("merchantId", merchantId);
        List result = this.findByNamedParams(sql, params);
        return (Vat2Merchant) this.single(result, true);
    }

}
