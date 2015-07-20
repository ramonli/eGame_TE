package com.mpos.lottery.te.gameimpl.bingo;

import com.mpos.lottery.te.gameimpl.bingo.prize.BingoPrizeConfirmationIntegrationTest;
import com.mpos.lottery.te.gameimpl.bingo.prize.BingoPrizeIntegrationTest;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoMannualCancelByTicketIntegrationTest;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoSaleCancellationIntegrationTest;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoSaleIntegrationTest;
import com.mpos.lottery.te.gameimpl.bingo.sale.service.PregeneratedTicketRefLookupServiceIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { PregeneratedTicketRefLookupServiceIntegrationTest.class, BingoSaleIntegrationTest.class,
        BingoSaleCancellationIntegrationTest.class, BingoMannualCancelByTicketIntegrationTest.class,
        BingoPrizeIntegrationTest.class, BingoPrizeConfirmationIntegrationTest.class })
public class BingoTestSuite {
}
