package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import javax.annotation.Resource;

public class ActiveTicketIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "instantTicketDao")
    private InstantTicketDao instantTicketDao;

    @Test
    public void testActiveInstantTicket() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681002");

        // 1. make activation
        Context reqCtx = this.getDefaultContext(TransactionType.ACTIVE_INSTANT_TICKET.getRequestType(), ticket);
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        // assert response
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // asert ticket
        InstantTicket dbTicket = this.getInstantTicketDao().getBySerialNo(ticket.getSerialNo());
        assertEquals(InstantTicket.STATUS_ACTIVE, dbTicket.getStatus());

    }

    public InstantTicketDao getInstantTicketDao() {
        return instantTicketDao;
    }

    public void setInstantTicketDao(InstantTicketDao instantTicketDao) {
        this.instantTicketDao = instantTicketDao;
    }

}
