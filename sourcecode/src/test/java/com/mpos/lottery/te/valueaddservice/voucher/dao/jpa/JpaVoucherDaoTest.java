package com.mpos.lottery.te.valueaddservice.voucher.dao.jpa;

import static org.junit.Assert.*;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.valueaddservice.voucher.Voucher;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherDao;

import org.junit.Test;

import java.math.BigDecimal;

import javax.annotation.Resource;

public class JpaVoucherDaoTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "jpaVoucherDao")
    private VoucherDao voucherDao;

    @Test
    public void testFindByGameAndFaceAmount() {
        Voucher voucher = this.getVoucherDao().findByGameAndFaceAmount("VOUCHER-1", new BigDecimal("60"), 1);
        assertEquals("1", voucher.getId());
    }

    @Test
    public void testFindByGameAndFaceAmount_NoEntity() {
        Voucher voucher = this.getVoucherDao().findByGameAndFaceAmount("VOUCHER-1", new BigDecimal("66"), 1);
        assertEquals(null, voucher);
    }

    public VoucherDao getVoucherDao() {
        return voucherDao;
    }

    public void setVoucherDao(VoucherDao voucherDao) {
        this.voucherDao = voucherDao;
    }

}
