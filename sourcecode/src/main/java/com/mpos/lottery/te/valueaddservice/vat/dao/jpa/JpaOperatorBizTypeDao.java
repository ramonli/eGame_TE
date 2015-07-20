package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.vat.OperatorBizType;
import com.mpos.lottery.te.valueaddservice.vat.dao.OperatorBizTypeDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("operatorBizTypeDao")
public class JpaOperatorBizTypeDao extends BaseJpaDao implements OperatorBizTypeDao {

    @Override
    public OperatorBizType findByOperator(String operatorId) {
        String sql = "from OperatorBizType d where d.operatorId=:operatorId and d.status=:status";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("operatorId", operatorId);
        params.put("status", OperatorBizType.STATUS_VALID);
        List result = this.findByNamedParams(sql, params);
        return (OperatorBizType) this.single(result, true);
    }

}
