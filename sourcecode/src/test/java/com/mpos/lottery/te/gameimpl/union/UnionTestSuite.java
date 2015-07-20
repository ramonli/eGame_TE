package com.mpos.lottery.te.gameimpl.union;

import com.mpos.lottery.te.gameimpl.union.game.service.UnionGameInstanceServiceIntegrationTest;
import com.mpos.lottery.te.gameimpl.union.prize.UnionPayoutReversalIntegrationTest;
import com.mpos.lottery.te.gameimpl.union.prize.UnionPrizeConfirmationIntegrationTest;
import com.mpos.lottery.te.gameimpl.union.prize.UnionPrizeEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.union.prize.UnionPrizeIntegrationTest;
import com.mpos.lottery.te.gameimpl.union.sale.UnionMannualCancellationIntegrationTest;
import com.mpos.lottery.te.gameimpl.union.sale.UnionSaleIntegrationTest;
import com.mpos.lottery.te.gameimpl.union.sale.UnionTicketCancellationIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UnionSaleIntegrationTest.class, UnionMannualCancellationIntegrationTest.class,
        UnionTicketCancellationIntegrationTest.class, UnionGameInstanceServiceIntegrationTest.class,
        UnionPayoutReversalIntegrationTest.class, UnionPrizeConfirmationIntegrationTest.class,
        UnionPrizeEnquiryIntegrationTest.class, UnionPrizeIntegrationTest.class })
public class UnionTestSuite {

}
