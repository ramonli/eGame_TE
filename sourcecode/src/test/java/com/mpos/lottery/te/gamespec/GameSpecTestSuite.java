package com.mpos.lottery.te.gamespec;

import com.mpos.lottery.te.gamespec.game.dao.JpaBaseGameInstanceDaoIntegrationTest;
import com.mpos.lottery.te.gamespec.prize.dao.TaxThresholdDaoIntegrationTest;
import com.mpos.lottery.te.gamespec.prize.dao.jpa.PayoutDaoImplTest;
import com.mpos.lottery.te.gamespec.prize.dao.jpa.WinningItemDaoIntegrationTest;
import com.mpos.lottery.te.gamespec.prize.dao.jpa.WinningStatisticsDaoIntegrationTest;
import com.mpos.lottery.te.gamespec.sale.dao.JpaBaseTicketDaoIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ PayoutDaoImplTest.class, TaxThresholdDaoIntegrationTest.class, JpaBaseTicketDaoIntegrationTest.class,
        JpaBaseGameInstanceDaoIntegrationTest.class, WinningItemDaoIntegrationTest.class,
        WinningStatisticsDaoIntegrationTest.class })
public class GameSpecTestSuite {

}
