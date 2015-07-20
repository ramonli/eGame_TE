package com.mpos.lottery.te.gameimpl.union.game.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.union.game.UnionCOperationParameter;
import com.mpos.lottery.te.gameimpl.union.game.dao.UnionCOperationParameterDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaUnionCOperationParameterDao extends BaseJpaDao implements UnionCOperationParameterDao {

    @Override
    public List<UnionCOperationParameter> findByOperationParameter(String operationParameterId) {
        String sql = "from UnionCOperationParameter as t where t.operationParameterId=:operationParameterId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("operationParameterId", operationParameterId);
        return this.findByNamedParams(sql, params);
    }
}
