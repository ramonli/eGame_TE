package com.mpos.lottery.te.gameimpl.lotto.prize.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class PayoutDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;

    @Test
    public void testInsertListOfPayout() {
        Payout payout = LottoDomainMocker.mockPayout();
        this.getPayoutDao().insert(payout);
        // retrieve payout from database
        List<Payout> payouts = this.getPayoutDao().getByTicketSerialNo(payout.getTicketSerialNo());
        assertEquals(1, payouts.size());
        Payout dbPayout = payouts.get(0);
        this.doAssert(payout, dbPayout);
    }

    private void doAssert(Payout payout, Payout dbPayout) {
        assertNotNull(dbPayout);
        assertEquals(payout.getVersion(), dbPayout.getVersion());
        assertEquals(payout.getTransaction().getId(), dbPayout.getTransaction().getId());
        assertEquals(payout.getTicketSerialNo(), dbPayout.getTicketSerialNo());
        assertEquals(payout.getTotalAmount().doubleValue(), dbPayout.getTotalAmount().doubleValue(), 0);
        assertEquals(payout.getType(), dbPayout.getType());
        assertEquals(payout.isValid(), dbPayout.isValid());
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

}
