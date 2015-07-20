package com.mpos.lottery.te.gameimpl.lotto.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class EnquiryTicketAcceptanceTest extends BaseAcceptanceTest {
    private String serialNo = "S-123456-aa";

    // private String serialNo = "02009113000000000001";

    @Test
    public void testEnquiryTicket_OK() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());

        Context response = this.post(request);
        assertEquals(200, response.getResponseCode());
        assertNotNull(response.getModel());
    }

    protected void customizeRequestContext(Context ctx) throws Exception {
        LottoTicket ticket = new LottoTicket();
        ticket.setSerialNo(serialNo);
        ctx.setModel(ticket);
        ctx.setTransType(TransactionType.TICKET_ENQUIRY.getRequestType());

    }
}
