package com.mpos.lottery.te.gameimpl.lotto.sale.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.gamespec.sale.InstantaneousSale;
import com.mpos.lottery.te.gamespec.sale.dao.InstantaneousSaleDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.math.BigDecimal;

import javax.annotation.Resource;

public class InstantaneousSaleDaoImplIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "instantaneousSaleDao")
    private InstantaneousSaleDao instantaneousDao;

    @Test
    public void testGetNoneResult() {
        InstantaneousSale sale = this.getInstantaneousDao().findByGameDraw("NONE");
        assertNull(sale);
    }

    @Test
    public void testInsert() {
        InstantaneousSale sale = this.mock();
        this.getInstantaneousDao().insert(sale);

        // query from db
        InstantaneousSale dbSale = this.getInstantaneousDao().findByGameDraw(sale.getGameInstanceId());
        this.doAssert(dbSale, sale);
    }

    @Test
    public void testUpdate() {
        InstantaneousSale sale = this.mock();
        this.getInstantaneousDao().insert(sale);
        InstantaneousSale tmpSale = this.getInstantaneousDao().findByGameDraw(sale.getGameInstanceId());
        tmpSale.setTurnover(new BigDecimal("1923"));
        tmpSale.setSaleCount(23);
        this.getInstantaneousDao().update(tmpSale);
        InstantaneousSale dbSale = this.getInstantaneousDao().findByGameDraw(sale.getGameInstanceId());
        this.doAssert(dbSale, tmpSale);
    }

    private void doAssert(InstantaneousSale dbSale, InstantaneousSale sale) {
        assertNotNull(dbSale);
        assertEquals(dbSale.getId(), sale.getId());
        assertEquals(dbSale.getGameInstanceId(), sale.getGameInstanceId());
        assertEquals(dbSale.getSaleCount(), sale.getSaleCount());
        assertEquals(dbSale.getTurnover(), sale.getTurnover());
    }

    private InstantaneousSale mock() {
        InstantaneousSale sale = new InstantaneousSale();
        sale.setId("Test-111");
        sale.setGameInstanceId("GII-111");
        sale.setSaleCount(1);
        sale.setTurnover(new BigDecimal("12"));
        return sale;
    }

    public InstantaneousSaleDao getInstantaneousDao() {
        return instantaneousDao;
    }

    public void setInstantaneousDao(InstantaneousSaleDao instantaneousDao) {
        this.instantaneousDao = instantaneousDao;
    }

}
