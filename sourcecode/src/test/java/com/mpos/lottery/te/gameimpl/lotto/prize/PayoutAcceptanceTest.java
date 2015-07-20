package com.mpos.lottery.te.gameimpl.lotto.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class PayoutAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testPayout() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
        assertNotNull(response.getModel());
        PrizeDto dto = (PrizeDto) response.getModel();
        assertEquals(new BigDecimal("7232000"), dto.getPrizeAmount());
        assertEquals(new BigDecimal("1205333.33"), dto.getTaxAmount());
        // assertEquals(new BigDecimal("2500.1"), dto.getReturnAmount());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.PAYOUT.getRequestType());
        LottoTicket ticket = new LottoTicket();
        ticket.setSerialNo("S-123456"); // print new ticket
        // ticket.setSerialNo("S-123456-a"); // no winning ticket
        ticket.setPIN("PIN-111");
        ticket.setPayoutInputChannel(Payout.INPUT_CHANNEL_SCANNER);
        //
        // PayoutDto payoutDto = new PayoutDto();
        // payoutDto.setActualAmount(new BigDecimal("4887973"));
        // payoutDto.setTicket(ticket);
        ctx.setModel(ticket);
    }

}
