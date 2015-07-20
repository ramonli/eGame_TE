package com.mpos.lottery.te.gameimpl.toto;

import com.mpos.lottery.te.gameimpl.toto.prize.PayoutIntegrationTest;
import com.mpos.lottery.te.gameimpl.toto.prize.PayoutReversalIntegrationTest;
import com.mpos.lottery.te.gameimpl.toto.prize.PrizeEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.toto.sale.SaleCancellationIntegrationTest;
import com.mpos.lottery.te.gameimpl.toto.sale.SaleEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.toto.sale.SaleIntegrationTest;
import com.mpos.lottery.te.gameimpl.toto.sale.TotoMannualCancelByTicketIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SaleIntegrationTest.class, SaleCancellationIntegrationTest.class, SaleEnquiryIntegrationTest.class,
        PayoutIntegrationTest.class, PrizeEnquiryIntegrationTest.class, PayoutReversalIntegrationTest.class,
        TotoMannualCancelByTicketIntegrationTest.class })
public class TotoTestSuite {

}
