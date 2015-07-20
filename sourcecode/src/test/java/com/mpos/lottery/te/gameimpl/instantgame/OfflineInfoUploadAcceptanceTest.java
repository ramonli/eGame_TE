package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantOfflineTickets;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OfflineInfoUploadAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testOfflineInfoUpload() throws Exception {
        Context response = this.doPost();
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.OFFLINE_INSTANT_TICKET_UPLOAD.getRequestType());
        InstantOfflineTickets offlineTicket = new InstantOfflineTickets();
        List<InstantTicket> tickets = new ArrayList<InstantTicket>();
        for (int i = 0; i < 2; i++) {
            InstantTicket ticket = new InstantTicket();
            ticket.setSerialNo("IT-SERIAL-" + i);
            ticket.setSoldTime(new Date());
            tickets.add(ticket);
        }
        offlineTicket.setTickets(tickets);
        ctx.setModel(offlineTicket);
    }

}
