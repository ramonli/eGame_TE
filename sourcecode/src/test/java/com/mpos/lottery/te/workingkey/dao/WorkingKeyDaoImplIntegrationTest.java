package com.mpos.lottery.te.workingkey.dao;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityGraph;

public class WorkingKeyDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "workingKeyDao")
    private WorkingKeyDao workingKeyDao;

    @Test
    public void testAdd() {
        WorkingKey key = this.mock();
        this.getWorkingKeyDao().insert(key);

        WorkingKey dbKey = this.getWorkingKeyDao().getWorkingKey(key.getCreateDateStr(), key.getGpeId());
        this.doAssertion(key, dbKey);
    }

    @Test
    public void testUpdate() {
        WorkingKey key = this.mock();
        this.getWorkingKeyDao().insert(key);

        WorkingKey dbKey = this.getWorkingKeyDao().getWorkingKey(key.getCreateDateStr(), key.getGpeId());
        dbKey.setDataKey("DATA_KEY");
        dbKey.setMacKey("MAC_KEY");
        this.getWorkingKeyDao().update(dbKey);
        WorkingKey dbKey1 = this.getWorkingKeyDao().getWorkingKey(dbKey.getCreateDateStr(), dbKey.getGpeId());

        this.getEntityManager().flush();

        this.doAssertion(dbKey, dbKey1);
    }

    // @Test(expected = OptimisticLockException.class)
    // public void testUpdate_JpaOptimisticLock() {
    // WorkingKey dbKey = this.getWorkingKeyDao().getWorkingKey("20140528",
    // "GPE-111");
    // dbKey.setDataKey("DATA_KEY");
    // // NOTE: must update version outside of current transaction
    // dbKey.setMacKey("MAC_KEY");
    // this.getWorkingKeyDao().update(dbKey);
    // WorkingKey dbKey1 =
    // this.getWorkingKeyDao().getWorkingKey(dbKey.getCreateDateStr(),
    // dbKey.getGpeId());
    //
    // this.getEntityManager().flush();
    //
    // this.doAssertion(dbKey, dbKey1);
    // }
    //
    // @Test
    // public void testUpdate_JpaOptimisticForceIncrement() {
    // WorkingKey dbKey = this.getWorkingKeyDao().getWorkingKey("20140528",
    // "GPE-111");
    // dbKey.setDataKey("DATA_KEY");
    // // NOTE: under this lock mode, even no update to entity, the read will
    // // increment version as well.
    // this.getEntityManager().lock(dbKey,
    // LockModeType.OPTIMISTIC_FORCE_INCREMENT);
    // // dbKey.setMacKey("MAC_KEY");
    // // this.getWorkingKeyDao().update(dbKey);
    // WorkingKey dbKey1 =
    // this.getWorkingKeyDao().getWorkingKey(dbKey.getCreateDateStr(),
    // dbKey.getGpeId());
    //
    // this.getEntityManager().flush();
    // }

    @Test
    public void testEntityGraph() {
        // WorkingKey dbKey = this.getWorkingKeyDao().getWorkingKey("20140528",
        // "GPE-111");
        // System.out.println(dbKey.getCreateDateStr());
        //

        // Seem the EntityGraph doesn't work...entity graph is just a hint, not
        // contract.
        EntityGraph<WorkingKey> eg = this.getEntityManager().createEntityGraph(WorkingKey.class);
        eg.addAttributeNodes("createDateStr");
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("javax.persistence.fetchgraph", eg);
        WorkingKey message = this.getEntityManager().find(WorkingKey.class, "GPE-KEY-111", props);
    }

    // @Test
    // public void testUpdate_JpaPessimisticRead() {
    // WorkingKey dbKey = this.getWorkingKeyDao().getWorkingKey("20140528",
    // "GPE-111");
    // dbKey.setDataKey("DATA_KEY");
    //
    // /**
    // * refer to
    // * http://docs.oracle.com/javaee/7/tutorial/doc/persistence-locking002
    // * .htm
    // * <p>
    // * (42.2.2.1 Pessimistic Locking Timeouts), no need to code in oracle
    // * way
    // */
    // this.getEntityManager().setProperty("javax.persistence.lock.timeout", 0);
    // // other transaction can read, however no modification and delete
    // // allowed
    // this.getEntityManager().lock(dbKey, LockModeType.PESSIMISTIC_READ);
    //
    // dbKey.setMacKey("MAC_KEY");
    // this.getWorkingKeyDao().update(dbKey);
    // WorkingKey dbKey1 =
    // this.getWorkingKeyDao().getWorkingKey(dbKey.getCreateDateStr(),
    // dbKey.getGpeId());
    //
    // this.getEntityManager().flush();
    //
    // this.doAssertion(dbKey, dbKey1);
    // }
    //
    // @Test
    // public void testUpdate_JpaPessimisticWrite() {
    // WorkingKey dbKey = this.getWorkingKeyDao().getWorkingKey("20140528",
    // "GPE-111");
    // dbKey.setDataKey("DATA_KEY");
    //
    // // other transaction can read, however no modification and delete
    // // allowed..different from JPA2.1 specification where it announces that
    // // even read is prevented.
    // this.getEntityManager().lock(dbKey, LockModeType.PESSIMISTIC_WRITE);
    //
    // dbKey.setMacKey("MAC_KEY");
    // this.getWorkingKeyDao().update(dbKey);
    // WorkingKey dbKey1 =
    // this.getWorkingKeyDao().getWorkingKey(dbKey.getCreateDateStr(),
    // dbKey.getGpeId());
    //
    // this.getEntityManager().flush();
    //
    // this.doAssertion(dbKey, dbKey1);
    // }

    private void doAssertion(WorkingKey key, WorkingKey dbKey) {

    }

    private WorkingKey mock() {
        WorkingKey key = new WorkingKey();
        key.setId(uuid());
        key.setCreateDateStr("20090408");
        key.setCreateTime(new Date());
        key.setDataKey("@#$%^&*MNJHUI@djfao8234fdv8234(*67123,xc89v62534(");
        key.setMacKey("JDHUE#20394JKN*&^5123kjN_)234_234");
        key.setGpeId("GPE-111");
        return key;
    }

    public WorkingKeyDao getWorkingKeyDao() {
        return workingKeyDao;
    }

    public void setWorkingKeyDao(WorkingKeyDao workingKeyDao) {
        this.workingKeyDao = workingKeyDao;
    }

}
