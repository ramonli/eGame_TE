package com.mpos.lottery.te.config.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.config.SysConfiguration;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Resource;

public class SysConfiguraionDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "sysConfigurationDao")
    private SysConfigurationDao sysConfigurationDao;

    @Test
    public void testGetSysConfiguration() {
        SysConfiguration conf = this.getSysConfigurationDao().getSysConfiguration();
        assertNotNull(conf);
        assertEquals(0, conf.getManualSettlementHandlingMode());
        // System.out.println(this.jdbcTemplate.queryFor)Int("select 1 from dual"));
        this.jdbcTemplate.query("select current_timestamp from dual", new RowCallbackHandler() {

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                System.out.println("[current timestamp]" + rs.getTimestamp(1));
            }

        });
    }

    public SysConfigurationDao getSysConfigurationDao() {
        return sysConfigurationDao;
    }

    public void setSysConfigurationDao(SysConfigurationDao sysConfigurationDao) {
        this.sysConfigurationDao = sysConfigurationDao;
    }

}
