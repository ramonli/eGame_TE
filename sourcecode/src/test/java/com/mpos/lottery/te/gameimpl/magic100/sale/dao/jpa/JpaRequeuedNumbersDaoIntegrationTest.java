package com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.magic100.sale.RequeuedNumbers;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.RequeuedNumbersDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class JpaRequeuedNumbersDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "requeuedNumbersDao")
    private RequeuedNumbersDao requeueNumbersDao;

    @Test
    public void testFindByCountOfValidNumber() {
        List<RequeuedNumbers> list = this.getRequeueNumbersDao().findByGameInstanceAndCountOfValidNumber("GII-111", 4);
        assertEquals(1, list.size());
        RequeuedNumbers range = list.get(0);
        assertEquals("2", range.getId());
        assertEquals(6, range.getBeginOfValidNumbers());
        assertEquals(5, range.getCountOfValidNumbers());
        assertEquals(5, range.getCountOfNumbers());
        assertEquals(5, range.getRequeuedNumbersItemList().size());
    }

    public RequeuedNumbersDao getRequeueNumbersDao() {
        return requeueNumbersDao;
    }

    public void setRequeueNumbersDao(RequeuedNumbersDao requeueNumbersDao) {
        this.requeueNumbersDao = requeueNumbersDao;
    }

}
