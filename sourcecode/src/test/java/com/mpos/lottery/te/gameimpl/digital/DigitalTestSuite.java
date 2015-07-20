package com.mpos.lottery.te.gameimpl.digital;

import com.mpos.lottery.te.gameimpl.digital.prize.DigitalPayoutConfirmationIntegrationTest;
import com.mpos.lottery.te.gameimpl.digital.prize.DigitalPayoutIntegrationTest;
import com.mpos.lottery.te.gameimpl.digital.prize.DigitalPayoutReversalIntegrationTest;
import com.mpos.lottery.te.gameimpl.digital.prize.DigitalPrizeEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.digital.sale.MannualCancelByTicketIntegrationTest;
import com.mpos.lottery.te.gameimpl.digital.sale.SaleCancellationIntegrationTest;
import com.mpos.lottery.te.gameimpl.digital.sale.SaleEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.digital.sale.SaleIntegrationTest;
import com.mpos.lottery.te.gameimpl.digital.sale.Sale_RiskControl_IntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SaleIntegrationTest.class, SaleEnquiryIntegrationTest.class, SaleCancellationIntegrationTest.class,
        DigitalPrizeEnquiryIntegrationTest.class, DigitalPayoutReversalIntegrationTest.class,
        DigitalPayoutIntegrationTest.class, DigitalPayoutConfirmationIntegrationTest.class,
        Sale_RiskControl_IntegrationTest.class, MannualCancelByTicketIntegrationTest.class })
public class DigitalTestSuite {

}
