package com.mpos.lottery.te.trans;

import com.mpos.lottery.te.trans.dao.JpaSettlementLogItemDaoIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDaoImplIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionMessageDaoImplIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionRetryLogDaoIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { TransactionIntegrationTest.class, TransactionDaoImplIntegrationTest.class,
        TransactionMessageDaoImplIntegrationTest.class, TransactionRetryLogDaoIntegrationTest.class,
        JpaSettlementLogItemDaoIntegrationTest.class })
public class TransactionTestSuite {
}
