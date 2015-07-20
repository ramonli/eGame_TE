package com.mpos.lottery.te.gameimpl.lfn.sale.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.lfn.sale.dao.LfnStatOfSelectedNumberDao;
import com.mpos.lottery.te.gamespec.sale.StatOfSelectedNumber;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class JpaLfnStatOfSelectedNumberDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "lfnStatOfSelectedNumberDao")
    private LfnStatOfSelectedNumberDao statOfSelectedNumberDao;

    @Test
    public void testFindByGameInstance() {
        List<StatOfSelectedNumber> stats = this.getStatOfSelectedNumberDao().findByGameInstance("GII-112", 30);
        assertEquals(30, stats.size());
        assertEquals(0, stats.get(0).getCount());
    }

    public LfnStatOfSelectedNumberDao getStatOfSelectedNumberDao() {
        return statOfSelectedNumberDao;
    }

    public void setStatOfSelectedNumberDao(LfnStatOfSelectedNumberDao statOfSelectedNumberDao) {
        this.statOfSelectedNumberDao = statOfSelectedNumberDao;
    }

}
