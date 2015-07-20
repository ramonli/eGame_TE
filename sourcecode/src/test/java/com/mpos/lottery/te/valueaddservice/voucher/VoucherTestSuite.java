package com.mpos.lottery.te.valueaddservice.voucher;

import com.mpos.lottery.te.valueaddservice.voucher.dao.jpa.JpaVoucherDaoTest;
import com.mpos.lottery.te.valueaddservice.voucher.dao.jpa.JpaVoucherStatisticsDaoTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ VoucherIntegrationTest.class, JpaVoucherDaoTest.class, VoucherTransactionEnquiryIntegrationTest.class,
        JpaVoucherStatisticsDaoTest.class })
public class VoucherTestSuite {

}
