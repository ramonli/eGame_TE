package com.mpos.lottery.te.gameimpl.instantgame;

import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDaoIntegrationTest;
import com.mpos.lottery.te.gameimpl.instantgame.dao.jpa.IgOperationParameterDaoIntegrationTest;
import com.mpos.lottery.te.gamespec.prize.dao.jpa.PrizeLevelDaoImplIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { VIRNValidationIntegrationTest.class, EGameValidationIntegrationTest.class,
        PrizeEnquiryIntegrationTest.class, BatchUploadOfflineValidationIntegrationTest.class,
        BatchValidationVIRNIntegrationTest.class, InstantTicketDaoIntegrationTest.class,
        PrizeLevelDaoImplIntegrationTest.class, IgOperationParameterDaoIntegrationTest.class,
        ActiveTicketIntegrationTest.class, ReportOfConfirmBatchValidationIntegrationTest.class,
        GetConfirmBatchNumberIntegrationTest.class, ConfirmBatchValidationReportTest.class, PartialPackageTest.class })
public class InstantGameTestSuite {
}
