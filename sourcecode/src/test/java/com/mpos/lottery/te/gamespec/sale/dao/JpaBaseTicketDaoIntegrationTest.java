package com.mpos.lottery.te.gamespec.sale.dao;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

public class JpaBaseTicketDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    private BaseTicketDao baseTicketDao;

    @Test
    public void testFindBySerialNo() {
        // List<LottoTicket> tickets =
        // this.getBaseTicketDao().findBySerialNo(LottoTicket.class,
        // "Xgy2I1dCyLS3Za04Yu2H5Q==");
        // assertEquals(1, tickets.size());
        // LottoTicket dbTicket = tickets.get(0);
        // assertEquals("TRANS-117", dbTicket.getTransaction().getId());
        // assertEquals("GII-113", dbTicket.getGameInstanceID());
        // assertEquals(dbTicket.getGameInstanceID(),
        // dbTicket.getGameDraw().getId());
        // assertEquals(4, dbTicket.getStatus());
        // assertFalse(dbTicket.isWinning());
        // assertEquals(800.1, dbTicket.getTotalAmount().doubleValue(), 0);
        // assertTrue(dbTicket.isCountInPool());
        // assertFalse(dbTicket.isPayoutBlocked());
        // assertEquals(111, dbTicket.getDevId());
        // assertEquals(BaseTicket.TICKET_TYPE_NORMAL,
        // dbTicket.getTicketType());
    }

    @Test
    public void testFindByTransaction() {
        // List<LottoTicket> tickets =
        // this.getBaseTicketDao().findByTransaction(LottoTicket.class,
        // "TRANS-117");
        // assertEquals(1, tickets.size());
        // LottoTicket dbTicket = tickets.get(0);
        // assertEquals("Xgy2I1dCyLS3Za04Yu2H5Q==", dbTicket.getSerialNo());
        // assertEquals("GII-113", dbTicket.getGameInstanceID());
        // assertEquals(dbTicket.getGameInstanceID(),
        // dbTicket.getGameDraw().getId());
        // assertEquals(4, dbTicket.getStatus());
        // assertFalse(dbTicket.isWinning());
        // assertEquals(800.1, dbTicket.getTotalAmount().doubleValue(), 0);
        // assertTrue(dbTicket.isCountInPool());
        // assertFalse(dbTicket.isPayoutBlocked());
        // assertEquals(111, dbTicket.getDevId());
        // assertEquals(BaseTicket.TICKET_TYPE_NORMAL,
        // dbTicket.getTicketType());
    }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

}
