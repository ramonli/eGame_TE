package com.mpos.lottery.te.gameimpl.magic100.sale.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumber;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

public class JpaLuckyNumberDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "jpaLuckyNumberDao")
    private LuckyNumberDao luckyNumberDao;

    @Test(expected = DataIntegrityViolationException.class)
    public void testFindBySequence_NoResult() throws Exception {
        this.jdbcTemplate.update("update LK_PRIZE_PARAMETERS set NUMBER_SEQ=-1 where NUMBER_SEQ>=2");
        List<LuckyNumber> luckyNumbers = this.getLuckyNumberDao().findBySeuqnce(1l, "GII-111", 3);
        assertEquals(0, luckyNumbers.size());
    }

    @Test
    public void testFindBySequence_Double() throws Exception {
        this.jdbcTemplate.update("update LK_PRIZE_PARAMETERS set price_amount=0.25");

        List<LuckyNumber> luckyNumbers = this.getLuckyNumberDao().findBySeuqnces("GII-111", 8l);
        assertEquals(1, luckyNumbers.size());
        assertEquals(0.25, luckyNumbers.get(0).getPrizeAmount().doubleValue(), 0);
    }

    @Test
    public void testFindBySequence_1() throws Exception {
        List<LuckyNumber> luckyNumbers = this.getLuckyNumberDao().findBySeuqnce(1l, "GII-111", 3);
        assertEquals(3, luckyNumbers.size());

        LuckyNumber n0 = luckyNumbers.get(0);
        assertEquals("1", n0.getId());
        assertEquals(1, n0.getSequenceOfNumber());

        LuckyNumber n1 = luckyNumbers.get(1);
        assertEquals("4", n1.getId());
        assertEquals(2, n1.getSequenceOfNumber());

        LuckyNumber n2 = luckyNumbers.get(2);
        assertEquals("7", n2.getId());
        assertEquals(3, n2.getSequenceOfNumber());
    }

    @Test
    public void testFindBySequence_2() throws Exception {
        List<LuckyNumber> luckyNumbers = this.getLuckyNumberDao().findBySeuqnce(9l, "GII-111", 3);
        assertEquals(3, luckyNumbers.size());

        LuckyNumber n0 = luckyNumbers.get(0);
        assertEquals("23", n0.getId());
        assertEquals(9, n0.getSequenceOfNumber());

        LuckyNumber n1 = luckyNumbers.get(1);
        assertEquals("24", n1.getId());
        assertEquals(10, n1.getSequenceOfNumber());

        LuckyNumber n2 = luckyNumbers.get(2);
        assertEquals("1", n2.getId());
        assertEquals(1, n2.getSequenceOfNumber());
    }

    @Test
    public void testFindBySequences_1() throws Exception {
        List<LuckyNumber> luckyNumbers = this.getLuckyNumberDao().findBySeuqnces("GII-111", 9l, 10l, 1l);
        assertEquals(3, luckyNumbers.size());

        Collections.sort(luckyNumbers, new Comparator<LuckyNumber>() {

            @Override
            public int compare(LuckyNumber o1, LuckyNumber o2) {
                return (int) (o1.getSequenceOfNumber() - o2.getSequenceOfNumber());
            }

        });

        LuckyNumber n2 = luckyNumbers.get(0);
        assertEquals("1", n2.getId());
        assertEquals(1, n2.getSequenceOfNumber());

        LuckyNumber n0 = luckyNumbers.get(1);
        assertEquals("23", n0.getId());
        assertEquals(9, n0.getSequenceOfNumber());

        LuckyNumber n1 = luckyNumbers.get(2);
        assertEquals("24", n1.getId());
        assertEquals(10, n1.getSequenceOfNumber());

    }

    public LuckyNumberDao getLuckyNumberDao() {
        return luckyNumberDao;
    }

    public void setLuckyNumberDao(LuckyNumberDao luckyNumberDao) {
        this.luckyNumberDao = luckyNumberDao;
    }

}
