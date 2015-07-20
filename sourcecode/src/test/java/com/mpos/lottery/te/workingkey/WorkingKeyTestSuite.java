package com.mpos.lottery.te.workingkey;

import com.mpos.lottery.te.workingkey.dao.WorkingKeyDaoImplIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ WorkingKeyIntegrationTest.class, WorkingKeyDaoImplIntegrationTest.class })
public class WorkingKeyTestSuite {

}
