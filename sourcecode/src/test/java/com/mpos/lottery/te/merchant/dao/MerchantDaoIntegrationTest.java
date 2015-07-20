package com.mpos.lottery.te.merchant.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

public class MerchantDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    private static Log logger = LogFactory.getLog(MerchantDaoIntegrationTest.class);
    @Autowired
    private MerchantDao merchantDao;
    @PersistenceContext
    private EntityManager entityManager;

    // @Test
    public void testGetById() {
        Merchant m = this.getMerchantDao().findById(Merchant.class, 111l);
        assertNotNull(m);
        assertEquals(100000000.0, m.getSaleCreditLevel().doubleValue(), 0);
        assertNotNull(m.getParentMerchant());
        assertEquals(222, m.getParentMerchant().getId());
        assertEquals("1,222", m.getParentMerchants());
        assertEquals(Operator.STATUS_ACTIVE, m.getStatus());
        System.out.println(m.getStatus());

        System.out.println("test transaction isolation level: read committed...change status in another transaction");

        Merchant m1 = this.getMerchantDao().getByCode("M-1");
        System.out.println(m1.getStatus());
        m1.setStatus(111);
        this.getMerchantDao().update(m1);
    }

    @Test
    public void testByGetParentId() {
        List<Merchant> children = this.getMerchantDao().getByParent(222L);
        assertEquals(2, children.size());
    }

    @Rollback(true)
    @Test
    public void testLock() throws Exception {
        /**
         * Find by primary key. Search for an entity of the specified class and primary key. If the entity instance is
         * contained in the persistence context, it is returned from there...Note that only the find(...) method of
         * entity manager will try to retrieve entity from persistence context first.
         */
        Merchant retailer = this.getMerchantDao().findById(Merchant.class, 111l);
        System.out.println(retailer.getName());

        Map<String, Object> hints = new HashMap<String, Object>();
        /**
         * Regarding what standard hints properties are supported, refer to "#3.4.4.3 Lock Mode Properties and Use" of
         * JPA2.1 specification document.
         * <p>
         * If no hint 'javax.persistence.lock.timeout' supplied, the underlying SQL will be:
         * <p>
         * select MERCHANT_ID from MERCHANT where MERCHANT_ID =111 for update
         * <p>
         * If provide this hint and set value to 0, the SQL will be:
         * <p>
         * select MERCHANT_ID from MERCHANT where MERCHANT_ID =111 for update nowait
         * <p>
         * If provide this hint and set a value which is greater than 0(the measurement unit for hint is millisecond,
         * however it is second in SQL, that says if your provide hint with value 10 milliseconds, the SQL will tell you
         * wait 0 seconds ), the SQL will be:
         * <p>
         * select MERCHANT_ID from MERCHANT where MERCHANT_ID =111 for update wait XXX
         * <p>
         */
        hints.put("javax.persistence.lock.timeout", 0);
        /**
         * Entity manager won't query all fields of entity in this case, and simply perform SQL: select MERCHANT_ID from
         * MERCHANT where MERCHANT_ID =111 for update...so it won't reload entity
         * <p>
         * The OPTIMISTIC lock must work with version checking.
         */
        try {
            this.entityManager.lock(retailer, LockModeType.PESSIMISTIC_READ, hints);
        } catch (Exception e) {
            Throwable root = SimpleToolkit.getRootCause(e);
            logger.warn(e.getMessage(), root);
            if (root instanceof SQLException) {
                SQLException sqlRoot = (SQLException) root;
                logger.debug("sqlState:" + sqlRoot.getSQLState());
                logger.debug("errorCode:" + sqlRoot.getErrorCode());
            }
            throw e;
        }
        // must call refresh() to reload entity
        this.getEntityManager().refresh(retailer, LockModeType.PESSIMISTIC_READ);
        // retailer = this.getMerchantDao().findById(Merchant.class, 111l);
        System.out.println(retailer.getName());
    }

    @Rollback(true)
    @Test
    public void testQuery() {
        Merchant retailer = this.getMerchantDao().getByCode("M-1");
        System.out.println(retailer.getName());
        retailer = this.getMerchantDao().getByCode("M-1");
        System.out.println(retailer.getName());
        retailer = this.getMerchantDao().findById(Merchant.class, 111l);
        System.out.println(retailer.getName());
        retailer.setCode("XX");
        this.getMerchantDao().update(retailer);
        this.getEntityManager().flush();
        /**
         * If try to refresh a entity before flush state changes into underlying database, the changes of entity will be
         * lost.
         */
        this.getEntityManager().refresh(retailer);
        System.out.println(retailer.getCode());
    }

    @Rollback(true)
    @Test
    public void testFlush() {
        Merchant retailer = this.getMerchantDao().getByCode("M-1");
        retailer.setName("testFlush1");
        /**
         * Here if any state of 'retailer' entity changed, the call of 'flush()' will generate 'update' SQL to override
         * the underlying database.
         * <p/>
         * Even call 'setName("testFlush")' on entity 'retailer, doesn't mean the state changed, as the original state
         * of name maybe 'testFlush' already, in this case, flush won't update the underlying database.
         */
        this.getEntityManager().flush();
        // Here the 'findByTaxNo(..)' will introduce the same side effect as flush(), as this finder
        // will trigger a
        // flush of entity manager.
        // this.getMerchantDao().findByTaxNo("XX");
        /**
         * refresh(...) will load state from underlying database which will ignore current states of entity in JPA
         * context, that says the states of current entity will be lost if we don't call flush(...).
         */
        this.getEntityManager().refresh(retailer, LockModeType.PESSIMISTIC_READ);
        System.out.println(retailer.getName());
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
