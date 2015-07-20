package com.mpos.lottery.te.gameimpl.lotto.prize;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class ConfirmRefundAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testConfirm() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());
        Context response = this.post(request);

        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.CONFIRM_PAYOUT.getRequestType());
        LottoTicket ticket = new LottoTicket();
        ticket.setSerialNo("S-888888");
        // ticket.setSerialNo("S-123456");
        ctx.setModel(ticket);
    }

}
