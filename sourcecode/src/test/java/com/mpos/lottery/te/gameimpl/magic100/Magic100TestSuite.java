package com.mpos.lottery.te.gameimpl.magic100;

import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstanceIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.game.service.Magic100GameInstanceServiceIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.prize.Magic100PrizeIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.prize.MagicPrizeEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.prize.MagicReversalIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100MannualCancelByTicketIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100SaleCancellationIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100SaleEnquiryIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100SaleIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa.JpaLuckyNumberDaoIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa.JpaLuckyNumberSequenceDaoIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa.JpaRequeuedNumbersDaoIntegrationTest;
import com.mpos.lottery.te.gameimpl.magic100.sale.service.DefaultLuckyNumberServiceIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ JpaLuckyNumberSequenceDaoIntegrationTest.class, JpaLuckyNumberDaoIntegrationTest.class,
        DefaultLuckyNumberServiceIntegrationTest.class, Magic100SaleIntegrationTest.class,
        Magic100SaleEnquiryIntegrationTest.class, Magic100SaleCancellationIntegrationTest.class,
        Magic100GameInstanceIntegrationTest.class, Magic100GameInstanceServiceIntegrationTest.class,
        JpaRequeuedNumbersDaoIntegrationTest.class, Magic100MannualCancelByTicketIntegrationTest.class,
        Magic100PrizeIntegrationTest.class, MagicPrizeEnquiryIntegrationTest.class, MagicReversalIntegrationTest.class })
public class Magic100TestSuite {

}
