package com.mpos.lottery.te.trans.dao;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.util.DateUtils;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.SettlementLog;
import com.mpos.lottery.te.trans.domain.SettlementLogItem;

import org.junit.Test;

import java.util.Date;

import javax.annotation.Resource;

public class JpaSettlementLogItemDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "jpaSettlementLogItemDao")
    private SettlementLogItemDao payoutSettlementDao;

    @Test
    public void testFindByOperator() {
        Date settlementTime = SimpleToolkit.parseDate("20150121164123", DateUtils.DEFAULT_TIMEFORMAT);
        SettlementLogItem log = this.getPayoutSettlementDao().findByOperator("OPERATOR-111", SettlementLog.STATE_VALID,
                settlementTime);
        assertEquals("1", log.getId());
        assertEquals("20150121", SimpleToolkit.formatDate(log.getCheckDay(), "yyyyMMdd"));
        // assertEquals(SettlementLog.STATE_VALID, log.getSettlementLog().getStatus());
    }

    @Test
    public void testFindByOperator_NoEntity() {
        Date settlementTime = SimpleToolkit.parseDate("20150121164123", DateUtils.DEFAULT_TIMEFORMAT);
        SettlementLogItem log = this.getPayoutSettlementDao().findByOperator("OPERATOR-111", SettlementLog.STATE_VALID,
                settlementTime);
        assertEquals("1", log.getId());
        assertEquals("20150121", SimpleToolkit.formatDate(log.getCheckDay(), "yyyyMMdd"));
        // assertEquals(SettlementLog.STATE_VALID, log.getSettlementLog().getStatus());
    }

    public SettlementLogItemDao getPayoutSettlementDao() {
        return payoutSettlementDao;
    }

    public void setPayoutSettlementDao(SettlementLogItemDao payoutSettlementDao) {
        this.payoutSettlementDao = payoutSettlementDao;
    }

}
