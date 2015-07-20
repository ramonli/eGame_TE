package com.mpos.lottery.te.workingkey.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.workingkey.dao.WorkingKeyDao;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkingKeyDaoImpl extends BaseJpaDao implements WorkingKeyDao {

    @Override
    public WorkingKey getWorkingKey(String createDateStr, String gpeId) throws DataAccessException {
        Map<String, Object> params = new HashMap<String, Object>(0);
        params.put("createDateStr", createDateStr);
        params.put("gpeId", gpeId);
        List result = this.findByNamedParams("from WorkingKey w where "
                + "w.createDateStr=:createDateStr and w.gpeId=:gpeId", params);
        if (result.size() > 0) {
            return (WorkingKey) result.get(0);
        }
        return null;
    }

}
