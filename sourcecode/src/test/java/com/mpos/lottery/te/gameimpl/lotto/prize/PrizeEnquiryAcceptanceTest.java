package com.mpos.lottery.te.gameimpl.lotto.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class PrizeEnquiryAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testEnquiryPrize() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
        assertNotNull(response.getModel());
        PrizeDto prize = (PrizeDto) response.getModel();
        assertEquals(3, prize.getPrizeItems().size());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.PRIZE_ENQUIRY.getRequestType());
        LottoTicket ticket = new LottoTicket();
        ticket.setSerialNo("S-123456");
        ctx.setModel(ticket);
    }

}
