package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Game;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2GameDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("vat2GameDao")
public class JpaVat2GameDao extends BaseJpaDao implements Vat2GameDao {

    @Override
    public Vat2Game findByVatAndBizType(String vatId, String bizType) {
        String sql = "from Vat2Game d where d.vatId=:vatId and d.businessType=:bizType and d.status="
                + Vat2Game.STATUS_VALID;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vatId", vatId);
        params.put("bizType", bizType);
        List result = this.findByNamedParams(sql, params);
        return (Vat2Game) this.single(result, true);
    }

}
