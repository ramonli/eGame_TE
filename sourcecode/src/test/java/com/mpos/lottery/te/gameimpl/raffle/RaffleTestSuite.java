package com.mpos.lottery.te.gameimpl.raffle;

import com.mpos.lottery.te.gameimpl.raffle.prize.RafflePayoutConfirmationIntegrationTest;
import com.mpos.lottery.te.gameimpl.raffle.prize.RafflePayoutIntegrationTest;
import com.mpos.lottery.te.gameimpl.raffle.prize.RafflePayoutReversalIntegrationTest;
import com.mpos.lottery.te.gameimpl.raffle.prize.RafflePrizeEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleMannualCancelByTicketIntegrationTest;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleSaleCancellationIntegrationTest;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleSaleEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleSaleIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { RaffleSaleIntegrationTest.class, RaffleSaleCancellationIntegrationTest.class,
        RaffleSaleEnquiryIntegrationTest.class, RafflePrizeEnquiryIntegrationTest.class,
        RafflePayoutConfirmationIntegrationTest.class, RafflePayoutIntegrationTest.class,
        RafflePayoutReversalIntegrationTest.class, RaffleMannualCancelByTicketIntegrationTest.class })
public class RaffleTestSuite {
}
