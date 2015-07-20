package com.mpos.lottery.te.gameimpl.lotto.prize.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import javax.annotation.Resource;

public class NewPrintTicketDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "newPrintTicketDao")
    private NewPrintTicketDao newPrintTicketDao;

    @Test
    public void testGetByOldTicket() {
        NewPrintTicket ticket = this.mock();
        this.getNewPrintTicketDao().insert(ticket);
        NewPrintTicket dbTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getOldTicketSerialNo());
        this.doAssert(ticket, dbTicket);
    }

    @Test
    public void testUpdate() {
        NewPrintTicket ticket = this.mock();
        this.getNewPrintTicketDao().insert(ticket);
        NewPrintTicket ticket1 = this.getNewPrintTicketDao().getByOldTicket(ticket.getOldTicketSerialNo());
        ticket1.setStatus(6);
        this.getNewPrintTicketDao().update(ticket1);
        NewPrintTicket dbTicket = this.getNewPrintTicketDao().getByOldTicket(ticket1.getOldTicketSerialNo());
        this.doAssert(ticket1, dbTicket);
    }

    private void doAssert(NewPrintTicket ticket, NewPrintTicket dbTicket) {
        assertNotNull(dbTicket);
        assertEquals(ticket.getId(), dbTicket.getId());
        assertEquals(ticket.getOldTicketSerialNo(), dbTicket.getOldTicketSerialNo());
        assertEquals(ticket.getNewTicketSerialNo(), dbTicket.getNewTicketSerialNo());
        assertEquals(ticket.getStatus(), dbTicket.getStatus());
    }

    private NewPrintTicket mock() {
        NewPrintTicket ticket = new NewPrintTicket();
        ticket.setId("NPT-111");
        ticket.setOldTicketSerialNo("O-111");
        ticket.setNewTicketSerialNo("N-111");
        ticket.setStatus(NewPrintTicket.STATUS_WAITCONFIRM);
        return ticket;
    }

    public NewPrintTicketDao getNewPrintTicketDao() {
        return newPrintTicketDao;
    }

    public void setNewPrintTicketDao(NewPrintTicketDao newPrintTicketDao) {
        this.newPrintTicketDao = newPrintTicketDao;
    }

}
