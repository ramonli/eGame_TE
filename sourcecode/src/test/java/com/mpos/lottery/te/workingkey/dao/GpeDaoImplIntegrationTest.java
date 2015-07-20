package com.mpos.lottery.te.workingkey.dao;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.junit.Test;

import java.util.Date;

import javax.annotation.Resource;

public class GpeDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
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
        this.doAssertion(dbKey, dbKey1);
    }

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
