package com.mpos.lottery.te.config.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.SysConfiguration;
import com.mpos.lottery.te.config.dao.SysConfigurationDao;

public class SysConfigurationDaoImpl extends BaseJpaDao implements SysConfigurationDao {

    /**
     * only return the first result.
     */
    public SysConfiguration getSysConfiguration() {
        return this.single(SysConfiguration.class);
    }

}
