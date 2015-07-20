package com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumberSequence;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberSequenceDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import javax.annotation.Resource;

public class JpaLuckyNumberSequenceDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "jpaLuckyNumberSequenceDao")
    private LuckyNumberSequenceDao luckyNumberSeqLogDao;

    @Test
    public void test() {
        LuckyNumberSequence seqLog = this.getLuckyNumberSeqLogDao().lookup("LK-1");
        assertEquals("1", seqLog.getId());
        assertEquals(8, seqLog.getNextSequence());
        assertNull(seqLog.getLastestPlayer());
    }

    public LuckyNumberSequenceDao getLuckyNumberSeqLogDao() {
        return luckyNumberSeqLogDao;
    }

    public void setLuckyNumberSeqLogDao(LuckyNumberSequenceDao luckyNumberSeqLogDao) {
        this.luckyNumberSeqLogDao = luckyNumberSeqLogDao;
    }

}
