package com.mpos.lottery.te.workingkey.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.springframework.dao.DataAccessException;

public interface WorkingKeyDao extends DAO {

    public WorkingKey getWorkingKey(String createTimeStr, String gpeId) throws DataAccessException;

}
