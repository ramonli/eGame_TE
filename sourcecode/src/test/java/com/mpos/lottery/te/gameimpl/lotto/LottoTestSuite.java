package com.mpos.lottery.te.gameimpl.lotto;

import com.mpos.lottery.te.gameimpl.lotto.game.service.LottoGameInstanceServiceIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.InternalPrizeIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.PayoutReversalIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.PrizeConfirmationIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.PrizeEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.PrizeIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.NewPrintTicketDaoIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.PayoutDaoIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa.LuckyWinningItemDaoImplIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa.PayoutDetailDaoImplIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa.PrizeGroupItemDaoImplIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa.PrizeObjectDaoImplIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.sale.MannualCancelByTicketIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.sale.TicketCancellationIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.sale.TicketEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.sale.TicketIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.sale.dao.jpa.InstantaneousSaleDaoImplIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.sale.dao.jpa.JpaCancelPendingTicketDaoIntegrationTest;
import com.mpos.lottery.te.gameimpl.lotto.sale.dao.jpa.LottoActivityReportDaoTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TicketIntegrationTest.class, PrizeIntegrationTest.class, PayoutDaoIntegrationTest.class,
        NewPrintTicketDaoIntegrationTest.class, PrizeObjectDaoImplIntegrationTest.class,
        PrizeGroupItemDaoImplIntegrationTest.class, LuckyWinningItemDaoImplIntegrationTest.class,
        PayoutDetailDaoImplIntegrationTest.class, PayoutReversalIntegrationTest.class,
        LottoActivityReportDaoTest.class, MannualCancelByTicketIntegrationTest.class,
        InstantaneousSaleDaoImplIntegrationTest.class, PrizeEnquiryIntegrationTest.class,
        TicketCancellationIntegrationTest.class, TicketEnquiryIntegrationTest.class,
        PrizeConfirmationIntegrationTest.class, LottoGameInstanceServiceIntegrationTest.class,
        InternalPrizeIntegrationTest.class, JpaCancelPendingTicketDaoIntegrationTest.class })
public class LottoTestSuite {

}
