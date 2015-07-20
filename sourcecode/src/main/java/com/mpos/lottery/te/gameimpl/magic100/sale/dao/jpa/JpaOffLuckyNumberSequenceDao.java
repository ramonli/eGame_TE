package com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.OffLuckyNumberSequence;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.OffLuckyNumberSequenceDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked", "deprecation" })
public class JpaOffLuckyNumberSequenceDao extends BaseJpaDao implements OffLuckyNumberSequenceDao {

    /**
     * @param gameId
     * @return OffLuckyNumberSequence
     */
    @Override
    public OffLuckyNumberSequence findByGameId(String gameId) {
        String sql = "from OffLuckyNumberSequence o where  o.gameId=:gameId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameId", gameId);
        List<OffLuckyNumberSequence> list = this.findByNamedParams(sql, params);
        if (list == null && list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

}
