package com.mpos.lottery.te.merchant.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.merchant.domain.MerchantCommission;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class MerchantCommissionDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    private static Log logger = LogFactory.getLog(MerchantCommissionDaoIntegrationTest.class);
    @Autowired
    private MerchantCommissionDao merchantCommissionDao;

    @Test
    public void testGetByMerchantAndGame() {
        MerchantCommission comm = this.getMerchantCommissionDao().getByMerchantAndGame(111, "LFN-1");
        assertNotNull(comm);
        assertEquals(111, comm.getMerchantId());
        assertEquals("LFN-1", comm.getGame().getId());
        assertEquals(0.2, comm.getSaleCommissionRate().doubleValue(), 0);
        assertEquals(0.1, comm.getPayoutCommissionRate().doubleValue(), 0);
    }

    /**
     * Test the the relationship between java instance and JPA entity.
     * 
     * Refer to http://stackoverflow.com/questions/24359471/jpa-entities-updated- implicitly-by-new-query
     */
    @Test
    public void testGetByMerchantAndGame_EntityInstance() {
        MerchantCommission oldComm = this.getMerchantCommissionDao().getByMerchantAndGame(111, "LFN-1");
        // now oldComm is a java instance, and also a managed JPA entity
        logger.debug("oldComm:" + oldComm);
        assertEquals(0.2, oldComm.getSaleCommissionRate().doubleValue(), 0);
        MerchantCommission newComm = this.getMerchantCommissionDao().getByMerchantAndGame(111, "LFN-1");
        logger.debug("newComm:" + newComm);

        // detach a entity(not detach java instance)
        this.getEntityManager().detach(oldComm);
        MerchantCommission latestComm = this.getMerchantCommissionDao().getByMerchantAndGame(111, "LFN-1");
        logger.debug("latestComm:" + latestComm);

        newComm.setSaleCommissionRate(new BigDecimal("0.2"));
        assertEquals(0.2, oldComm.getSaleCommissionRate().doubleValue(), 0);
        assertEquals(0.2, latestComm.getSaleCommissionRate().doubleValue(), 0);

        MerchantCommission mergedComm = this.getEntityManager().merge(newComm);
        logger.debug("newComm:" + newComm);
        logger.debug("mergerd newComm:" + mergedComm);

        assertEquals(0.2, oldComm.getSaleCommissionRate().doubleValue(), 0);
        assertEquals(0.2, latestComm.getSaleCommissionRate().doubleValue(), 0);
    }

    public MerchantCommissionDao getMerchantCommissionDao() {
        return merchantCommissionDao;
    }

    public void setMerchantCommissionDao(MerchantCommissionDao merchantCommissionDao) {
        this.merchantCommissionDao = merchantCommissionDao;
    }

}
