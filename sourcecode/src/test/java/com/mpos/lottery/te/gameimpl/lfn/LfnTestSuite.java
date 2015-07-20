package com.mpos.lottery.te.gameimpl.lfn;

import com.mpos.lottery.te.gameimpl.lfn.prize.LfnPayoutConfirmationIntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.prize.LfnPayoutIntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.prize.LfnPayoutReversalIntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.prize.LfnPrizeEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnMannualCancelByTicketIntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnSaleCancellationIntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnSaleEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnSaleIntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnSale_RiskControl_IntegrationTest;
import com.mpos.lottery.te.gameimpl.lfn.sale.dao.jpa.JpaLfnStatOfSelectedNumberDaoIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { LfnPayoutConfirmationIntegrationTest.class, LfnPayoutIntegrationTest.class,
        LfnPayoutReversalIntegrationTest.class, LfnPrizeEnquiryIntegrationTest.class,
        LfnSaleCancellationIntegrationTest.class, LfnSaleEnquiryIntegrationTest.class, LfnSaleIntegrationTest.class,
        LfnSale_RiskControl_IntegrationTest.class, JpaLfnStatOfSelectedNumberDaoIntegrationTest.class,
        LfnMannualCancelByTicketIntegrationTest.class })
public class LfnTestSuite {
}
