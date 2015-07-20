package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.dao.OperationParameterDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;

import java.util.List;

public class IgOperationParameterDaoImpl extends BaseJpaDao implements OperationParameterDao<IgOperationParameter> {

    @Override
    public IgOperationParameter getDefault() {
        List<IgOperationParameter> params = this.all(IgOperationParameter.class);
        if (params.size() != 0) {
            return params.get(0);
        }
        return null;
    }
}
