package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.dao.MerchantCommissionDao;
import com.mpos.lottery.te.merchant.domain.MerchantCommission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerchantCommissionDaoImpl extends BaseJpaDao implements MerchantCommissionDao {

    public MerchantCommission getByMerchantAndGame(long merchantId, String gameId) {
        Map params = new HashMap();
        params.put("gameId", gameId);
        params.put("merchantId", merchantId);
        String sql = "from MerchantCommission m where m.game.id=:gameId and m.merchantId=:merchantId";
        List<MerchantCommission> result = this.findByNamedParams(sql, params);
        return this.single(result, true);
    }

    @Override
    public List<MerchantCommission> getByMerchant(long merchantId) {
        Map params = new HashMap();
        params.put("merchantId", merchantId);
        String sql = "from MerchantCommission m where m.merchantId=:merchantId";
        return this.findByNamedParams(sql, params);
    }

}
