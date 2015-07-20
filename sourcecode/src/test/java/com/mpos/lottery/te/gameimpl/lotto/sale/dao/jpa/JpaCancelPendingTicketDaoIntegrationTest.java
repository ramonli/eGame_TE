package com.mpos.lottery.te.gameimpl.lotto.sale.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.dao.PendingTransactionDao;
import com.mpos.lottery.te.trans.domain.PendingTransaction;

import org.junit.Test;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

public class JpaCancelPendingTicketDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "pendingTransactionDao")
    private PendingTransactionDao cancelPendingTicketDao;

    @Test
    public void testInsertAndQueryBySerialNo() {
        PendingTransaction ticket = mock();
        this.getCancelPendingTicketDao().insert(ticket);
        List<PendingTransaction> result = this.getCancelPendingTicketDao().getByTicketSerialNo(
                ticket.getTicketSerialNo());

        // assert output
        this.doAssert(ticket, result.get(0));
    }

    @Test
    public void testInsertAndQueryByDeviceAndTraceMsgId() {
        PendingTransaction ticket = mock();
        this.getCancelPendingTicketDao().insert(ticket);
        List<PendingTransaction> result = this.getCancelPendingTicketDao().getByDeviceAndTraceMsgId(
                ticket.getDeviceId(), ticket.getTraceMsgId());

        // assert output
        this.doAssert(ticket, result.get(0));
    }

    protected void doAssert(PendingTransaction clientTicket, PendingTransaction dbTicket) {
        assertEquals(clientTicket.getId(), dbTicket.getId());
        assertEquals(clientTicket.getTicketSerialNo(), dbTicket.getTicketSerialNo());
        assertEquals(clientTicket.getDeviceId(), dbTicket.getDeviceId());
        assertEquals(clientTicket.getTraceMsgId(), dbTicket.getTraceMsgId());
        assertEquals(clientTicket.getTransType(), dbTicket.getTransType());
        assertEquals(clientTicket.getCreateTime(), dbTicket.getCreateTime());
        assertEquals(clientTicket.getUpdateTime(), dbTicket.getUpdateTime());
        assertEquals(clientTicket.isDealed(), dbTicket.isDealed());
    }

    public static PendingTransaction mock() {
        PendingTransaction ticket = new PendingTransaction();
        ticket.setId("CPT-1");
        ticket.setTicketSerialNo("S-888888");
        ticket.setDeviceId(111);
        ticket.setTraceMsgId("TMS-1");
        ticket.setTransType(203);
        ticket.setDealed(false);
        ticket.setCreateTime(new Date());
        ticket.setUpdateTime(new Date());
        return ticket;
    }

    public PendingTransactionDao getCancelPendingTicketDao() {
        return cancelPendingTicketDao;
    }

    public void setCancelPendingTicketDao(PendingTransactionDao cancelPendingTicketDao) {
        this.cancelPendingTicketDao = cancelPendingTicketDao;
    }

}
