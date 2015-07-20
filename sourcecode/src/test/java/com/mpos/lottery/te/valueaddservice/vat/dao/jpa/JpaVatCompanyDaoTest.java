package com.mpos.lottery.te.valueaddservice.vat.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.valueaddservice.vat.VatCompany;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatCompanyDao;

import org.junit.Test;

import javax.annotation.Resource;

public class JpaVatCompanyDaoTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "vatCompanyDao")
    private VatCompanyDao vatCompanyDao;

    @Test
    public void testFindByMerchant() {
        VatCompany company = this.getVatCompanyDao().findByMerchant(111);
        assertEquals("COMPANY-1", company.getId());
    }

    @Test
    public void testFindByTaxNo() {
        VatCompany company = this.getVatCompanyDao().findByTaxNo("TAX-111");
        assertEquals("COMPANY-1", company.getId());
    }

    public VatCompanyDao getVatCompanyDao() {
        return vatCompanyDao;
    }

    public void setVatCompanyDao(VatCompanyDao vatCompanyDao) {
        this.vatCompanyDao = vatCompanyDao;
    }

}
