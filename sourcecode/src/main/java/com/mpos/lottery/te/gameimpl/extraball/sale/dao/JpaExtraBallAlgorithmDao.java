package com.mpos.lottery.te.gameimpl.extraball.sale.dao;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallAlgorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaExtraBallAlgorithmDao extends BaseJpaDao implements ExtraBallAlgorithmDao {

    @Override
    public ExtraBallAlgorithm findByType(int type) {
        String sql = "from ExtraBallAlgorithm a where a.type=:type";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("type", type);

        List result = this.findByNamedParams(sql, params);
        if (result.size() > 0) {
            return (ExtraBallAlgorithm) result.get(0);
        }
        return null;
    }

}
