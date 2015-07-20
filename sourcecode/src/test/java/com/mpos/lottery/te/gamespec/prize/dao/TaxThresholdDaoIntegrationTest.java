package com.mpos.lottery.te.gamespec.prize.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gamespec.prize.TaxThreshold;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;

public class TaxThresholdDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "taxThresholdDao")
    private TaxThresholdDao taxThresholdDao;

    @Test
    public void testGetByPolicyAndAmountAndDateRange() {
        TaxThreshold threshold = this.getTaxThresholdDao().getByPolicyAndAmountAndDateRange("TP-1",
                new BigDecimal("2000"), new Date());
        assertNotNull(threshold);
        assertEquals("TP-1", threshold.getTaxPolicyId());
        assertEquals("WTT-1", threshold.getId());
        assertEquals("TDR-1", threshold.getTaxDateRange().getId());
        assertEquals(new BigDecimal("1000"), threshold.getMinAmount());
        assertEquals(new BigDecimal("9999999999"), threshold.getMaxAmount());
        assertEquals(1, threshold.getRuleType());
        assertEquals(new BigDecimal("0.2"), threshold.getTaxAmount());
        assertEquals(TaxThreshold.TAXBASE_PAYOUT, threshold.getTaxBase());
    }

    public TaxThresholdDao getTaxThresholdDao() {
        return taxThresholdDao;
    }

    public void setTaxThresholdDao(TaxThresholdDao taxThresholdDao) {
        this.taxThresholdDao = taxThresholdDao;
    }

}
