package com.mpos.lottery.te.gameimpl.lotto.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class SellTicketAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testLottoSale() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());
        request.setGameTypeId(GameType.LOTTO.getType() + "");

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
        assertNotNull(response.getModel());
        LottoTicket ticket = (LottoTicket) response.getModel();
        assertNotNull(ticket.getSerialNo());
        assertNotNull(response.getTransactionID());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        // set sell ticket transaction
        ctx.setTransType(TransactionType.SELL_TICKET.getRequestType());
        LottoTicket ticket = LottoDomainMocker.mockTicket();

        ctx.setModel(ticket);
    }

}
