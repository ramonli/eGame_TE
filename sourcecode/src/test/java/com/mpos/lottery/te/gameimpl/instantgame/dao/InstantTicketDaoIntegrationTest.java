package com.mpos.lottery.te.gameimpl.instantgame.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

public class InstantTicketDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "instantTicketDao")
    private InstantTicketDao instantTicketDao;

    @Test
    public void testInsert() {
        this.printMethod();
        InstantTicket t = this.mock();
        this.getInstantTicketDao().insert(t);

        InstantTicket dbTicket = this.getInstantTicketDao().getBySerialNo(t.getSerialNo());
        this.doAssert(t, dbTicket);
    }

    @Test
    public void testUpdate() {
        this.printMethod();
        InstantTicket t = this.mock();
        this.getInstantTicketDao().insert(t);

        InstantTicket dbTicket = this.getInstantTicketDao().getBySerialNo(t.getSerialNo());
        dbTicket.setStatus(InstantTicket.STATUS_INACTIVE);
        dbTicket.setBookNumber("XX-111");
        this.getInstantTicketDao().update(dbTicket);

        InstantTicket updateTicket = this.getInstantTicketDao().getBySerialNo(t.getSerialNo());
        this.doAssert(dbTicket, updateTicket);
    }

    @Test
    public void testGetByGameDrawAndBook() {
        this.printMethod();
        List<InstantTicket> tickets = this.getInstantTicketDao().getByGameDrawNameAndBook("198", "198415681");
        assertEquals(3, tickets.size());
    }

    @Test
    public void testGetByRangeSerialNo() {
        this.printMethod();
        List<InstantTicket> tickets = this.getInstantTicketDao().getByRangeSerialNo("2", "5");
        assertEquals(1, tickets.size());
    }

    private void doAssert(InstantTicket ticket, InstantTicket dbTicket) {
        assertNotNull(dbTicket);
        assertEquals(ticket.getId(), dbTicket.getId());
        assertEquals(ticket.getBookNumber(), dbTicket.getBookNumber());
        assertEquals(ticket.getSerialNo(), dbTicket.getSerialNo());
        assertEquals(ticket.getStatus(), dbTicket.getStatus());
        assertEquals(ticket.getTicketMAC(), dbTicket.getTicketMAC());
        assertEquals(ticket.getTicketXOR1(), dbTicket.getTicketXOR1());
        assertEquals(ticket.getTicketXOR3(), dbTicket.getTicketXOR3());
    }

    private InstantTicket mock() {
        InstantTicket ticket = new InstantTicket();
        ticket.setId(this.uuid());
        InstantGameDraw gameDraw = new InstantGameDraw();
        gameDraw.setId("IGII-111");
        ticket.setGameDraw(gameDraw);
        ticket.setStatus(InstantTicket.STATUS_ACTIVE);
        ticket.setBookNumber("PACKET-111");
        ticket.setSoldTime(new Date());
        ticket.setSerialNo("TICKET-111");
        ticket.setTicketMAC("333CF2725BDC6C54D37CE39580197800");
        ticket.setTicketXOR1("36338604");
        ticket.setTicketXOR3("78795268");
        return ticket;
    }

    public InstantTicketDao getInstantTicketDao() {
        return instantTicketDao;
    }

    public void setInstantTicketDao(InstantTicketDao instantTicketDao) {
        this.instantTicketDao = instantTicketDao;
    }
}
