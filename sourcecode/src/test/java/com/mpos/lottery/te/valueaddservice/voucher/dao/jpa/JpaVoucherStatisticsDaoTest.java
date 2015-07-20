package com.mpos.lottery.te.valueaddservice.voucher.dao.jpa;

import static org.junit.Assert.*;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherStatistics;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherStatisticsDao;

import org.junit.Test;

import java.math.BigDecimal;

import javax.annotation.Resource;

public class JpaVoucherStatisticsDaoTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "jpaVoucherStatDao")
    private VoucherStatisticsDao voucherStatDao;

    @Test
    public void testFindByGameAndFaceAmount() {
        VoucherStatistics voucherStat = this.getVoucherStatDao().findByGameAndFaceAmount(new BigDecimal("5.0"),
                "No-Exist");
        assertTrue(voucherStat == null);

        voucherStat = this.getVoucherStatDao().findByGameAndFaceAmount(new BigDecimal("5.0"), "VOUCHER-1");
        assertEquals("1", voucherStat.getId());
        assertEquals(100, voucherStat.getRemainCount());
        assertEquals("VOUCHER-1", voucherStat.getGameId());
        assertEquals(5.0, voucherStat.getFaceAmount().doubleValue(), 0);
    }

    public VoucherStatisticsDao getVoucherStatDao() {
        return voucherStatDao;
    }

    public void setVoucherStatDao(VoucherStatisticsDao voucherStatDao) {
        this.voucherStatDao = voucherStatDao;
    }

}
