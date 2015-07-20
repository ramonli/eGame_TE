package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.vat.VAT;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("vatDao")
public class JpaVatDao extends BaseJpaDao implements VatDao {

    @Override
    public VAT findByCode(String code) {
        String sql = "from VAT d where d.code=:code and d.status=" + VAT.STATUS_VALID;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("code", code);
        List result = this.findByNamedParams(sql, params);
        return (VAT) this.single(result, true);
    }

}
