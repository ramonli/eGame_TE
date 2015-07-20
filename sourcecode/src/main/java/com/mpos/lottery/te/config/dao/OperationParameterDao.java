package com.mpos.lottery.te.config.dao;

import com.mpos.lottery.te.common.dao.DAO;

public interface OperationParameterDao<T> extends DAO {

    T getDefault();

}
