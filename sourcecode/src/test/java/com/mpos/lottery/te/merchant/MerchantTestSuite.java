package com.mpos.lottery.te.merchant;

import com.mpos.lottery.te.merchant.dao.MerchantCommissionDaoIntegrationTest;
import com.mpos.lottery.te.merchant.dao.MerchantDaoIntegrationTest;
import com.mpos.lottery.te.merchant.service.CreditServiceIntegrationTest;
import com.mpos.lottery.te.merchant.service.balance.CashoutBalanceServiceIntegrationTest;
import com.mpos.lottery.te.merchant.service.balance.CommissionBalanceServiceIntegrationTest;
import com.mpos.lottery.te.merchant.service.balance.PayoutBalanceServiceIntegrationTest;
import com.mpos.lottery.te.merchant.service.balance.SaleBalanceServiceIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { CreditTransferIntegrationTest.class, CreditServiceIntegrationTest.class,
        MerchantCommissionDaoIntegrationTest.class, MerchantDaoIntegrationTest.class,
        CreditTransferReversalIntegrationTest.class, ActivityReportIntegrationTest.class,
        OperatorTopupIntegrationTest.class, IncomeBalanceIntegrationTest.class,
        IncomeBalanceCancellationIntegrationTest.class, OperatorCashoutByPassIntegrateTest.class,
        SaleBalanceServiceIntegrationTest.class, PayoutBalanceServiceIntegrationTest.class,
        CashoutBalanceServiceIntegrationTest.class, CommissionBalanceServiceIntegrationTest.class })
public class MerchantTestSuite {
}
