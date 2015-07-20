package com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.OfflineCancellation;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.OfflinecancellationDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked", "deprecation" })
public class JpaOfflineCancellationDao extends BaseJpaDao implements OfflinecancellationDao {

    @Override
    public List<OfflineCancellation> findByGameId(String gameId) {
        String sql = "from OfflineCancellation oc where oc.isHandled=:isHandled and oc.gameId=:gameId order by oc.createTime";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("isHandled", OfflineCancellation.STATE_PROCESSING);
        params.put("gameId", gameId);
        return this.findByNamedParams(sql, params);
    }

    @Override
    public List<OfflineCancellation> findByTransactionId(String teTransactionId) {
        String sql = "from OfflineCancellation oc where oc.teTransactionId=:teTransactionId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("teTransactionId", teTransactionId);
        return this.findByNamedParams(sql, params);
    }

}
