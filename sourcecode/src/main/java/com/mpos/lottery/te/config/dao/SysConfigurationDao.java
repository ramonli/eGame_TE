package com.mpos.lottery.te.config.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.config.SysConfiguration;

public interface SysConfigurationDao extends DAO {

    SysConfiguration getSysConfiguration();
}
