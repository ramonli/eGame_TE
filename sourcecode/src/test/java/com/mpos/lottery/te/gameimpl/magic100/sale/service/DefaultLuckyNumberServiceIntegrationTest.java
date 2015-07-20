package com.mpos.lottery.te.gameimpl.magic100.sale.service;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.magic100.Magic100DomainMocker;
import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumber;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumberSequence;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.magic100.sale.RequeuedNumbers;
import com.mpos.lottery.te.gameimpl.magic100.sale.RequeuedNumbersItem;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberSequenceDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.RequeuedNumbersDao;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.junit.Test;

import java.util.List;

import javax.annotation.Resource;

public class DefaultLuckyNumberServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "defaultLuckyNumberService")
    private LuckyNumberService luckyNumberService;
    @Resource(name = "jpaLuckyNumberDao")
    private LuckyNumberDao luckyNumberDao;
    @Resource(name = "jpaLuckyNumberSequenceDao")
    private LuckyNumberSequenceDao luckyNumberSequenceDao;
    @Resource(name = "baseGameInstanceDao")
    private BaseGameInstanceDao gameInstanceDao;
    @Resource(name = "requeuedNumbersDao")
    private RequeuedNumbersDao requeuedNumbersDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    // @Rollback(false)
    @Test
    public void testDetermine_NoCancelCounter() throws Exception {
        this.printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        ticket.setGameInstance(this.lookupHostGameInstance(ticket));

        // -------------------------------------------
        // the 1st lookup
        // -------------------------------------------
        Context respCtx = new Context();
        Transaction trans = new Transaction();
        respCtx.setTransaction(trans);
        List<LuckyNumber> luckyNumbers = this.getLuckyNumberService().determine(respCtx, ticket);
        assertEquals(3, luckyNumbers.size());

        LuckyNumber actual = luckyNumbers.get(0);
        assertEquals(6, actual.getSequenceOfNumber());
        assertEquals("6", actual.getLuckyNumber());
        actual = luckyNumbers.get(1);
        assertEquals(7, actual.getSequenceOfNumber());
        assertEquals("7", actual.getLuckyNumber());
        actual = luckyNumbers.get(2);
        assertEquals(8, actual.getSequenceOfNumber());
        assertEquals("8", actual.getLuckyNumber());

        // assert DB
        LuckyNumberSequence luckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());
        assertEquals(8, luckyNumberSequence.getNextSequence());
        // requeued numbers
        RequeuedNumbers dbRequeuedNumbers = this.getBaseJpaDao().findById(RequeuedNumbers.class, "2");
        assertEquals(2, dbRequeuedNumbers.getCountOfValidNumbers());
        assertEquals(5, dbRequeuedNumbers.getCountOfNumbers());

        List<RequeuedNumbersItem> dbValidItems = dbRequeuedNumbers.lookupValidItems(2);
        assertEquals(9, dbValidItems.get(0).getSequenceOfNumber());
        assertEquals(10, dbValidItems.get(1).getSequenceOfNumber());

        // -------------------------------------------
        // the 2nd lookup
        // -------------------------------------------
        ((Magic100Entry) ticket.getEntries().get(0)).setTotalBets(4);
        luckyNumbers = this.getLuckyNumberService().determine(respCtx, ticket);
        assertEquals(4, luckyNumbers.size());

        actual = luckyNumbers.get(0);
        assertEquals(8, actual.getSequenceOfNumber());
        assertEquals("8", actual.getLuckyNumber());
        actual = luckyNumbers.get(1);
        assertEquals(9, actual.getSequenceOfNumber());
        assertEquals("9", actual.getLuckyNumber());
        actual = luckyNumbers.get(2);
        assertEquals(10, actual.getSequenceOfNumber());
        assertEquals("10", actual.getLuckyNumber());
        actual = luckyNumbers.get(3);
        assertEquals(1, actual.getSequenceOfNumber());
        assertEquals("1", actual.getLuckyNumber());

        // assert DB
        luckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(ticket.getGameInstance().getGameId());
        assertEquals(2, luckyNumberSequence.getNextSequence());
        // requeued numbers
        dbRequeuedNumbers = this.getBaseJpaDao().findById(RequeuedNumbers.class, "2");
        assertEquals(2, dbRequeuedNumbers.getCountOfValidNumbers());
        assertEquals(5, dbRequeuedNumbers.getCountOfNumbers());

        dbValidItems = dbRequeuedNumbers.lookupValidItems(2);
        assertEquals(9, dbValidItems.get(0).getSequenceOfNumber());
        assertEquals(10, dbValidItems.get(1).getSequenceOfNumber());
    }

    // ----------------------------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------------------------
    protected void assertLuckyNumber(LuckyNumber expect, LuckyNumber actual) {
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getSequenceOfNumber(), actual.getSequenceOfNumber());
        assertEquals(expect.getLuckyNumber(), actual.getLuckyNumber());
        assertEquals(expect.getPrizeAmount().doubleValue(), actual.getPrizeAmount().doubleValue(), 0);
        assertEquals(expect.getCancelCounter(), actual.getCancelCounter());
    }

    protected Magic100GameInstance lookupHostGameInstance(Magic100Ticket ticket) {
        return this.getGameInstanceDao().lookupByGameAndNumber(111, Magic100GameInstance.class,
                ticket.getGameInstance().getGameId(), ticket.getGameInstance().getNumber());
    }

    // ----------------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------------------------------

    public LuckyNumberService getLuckyNumberService() {
        return luckyNumberService;
    }

    public void setLuckyNumberService(LuckyNumberService luckyNumberService) {
        this.luckyNumberService = luckyNumberService;
    }

    public LuckyNumberDao getLuckyNumberDao() {
        return luckyNumberDao;
    }

    public void setLuckyNumberDao(LuckyNumberDao luckyNumberDao) {
        this.luckyNumberDao = luckyNumberDao;
    }

    public LuckyNumberSequenceDao getLuckyNumberSequenceDao() {
        return luckyNumberSequenceDao;
    }

    public void setLuckyNumberSequenceDao(LuckyNumberSequenceDao luckyNumberSequenceDao) {
        this.luckyNumberSequenceDao = luckyNumberSequenceDao;
    }

    public BaseGameInstanceDao getGameInstanceDao() {
        return gameInstanceDao;
    }

    public void setGameInstanceDao(BaseGameInstanceDao gameInstanceDao) {
        this.gameInstanceDao = gameInstanceDao;
    }

    public RequeuedNumbersDao getRequeuedNumbersDao() {
        return requeuedNumbersDao;
    }

    public void setRequeuedNumbersDao(RequeuedNumbersDao requeuedNumbersDao) {
        this.requeuedNumbersDao = requeuedNumbersDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

}
