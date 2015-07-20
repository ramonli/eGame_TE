package com.mpos.lottery.te.gameimpl.lotto.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class CancelTicketAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testCancel() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        // set sell ticket transaction
        ctx.setTransType(TransactionType.CANCEL_BY_TICKET.getRequestType());
        LottoTicket ticket = new LottoTicket();
        ticket.setSerialNo("S-888888");
        // ticket.setSerialNo("02009122100000000001");
        ctx.setModel(ticket);
    }

}
