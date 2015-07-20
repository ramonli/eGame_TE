package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class SellInstantTicketAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testActive() throws Exception {
        Context response = this.doPost();
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.SELL_INSTANT_TICKET.getRequestType());
        InstantTicket ticket = LottoDomainMocker.mockInstantTicket();
        ctx.setModel(ticket);
    }
}
