package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class BatchValidationAcceptanceTest extends BaseAcceptanceTest {
    @Test
    public void testValidateOk() throws Exception {
        Context response = this.doPost();
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.BATCH_VALIDATION.getRequestType());
        InstantTicket ticket = LottoDomainMocker.mockInstantTicket();
        ticket.setSerialNo("198415681983");
        ticket.setTicketXOR3("37330200"); // ok
        // ticket.setTicketXOR2("37330219"); //fail

        PrizeLevelDto dto = new PrizeLevelDto();
        dto.setTicket(ticket);
        dto.setPrizeAmount(new BigDecimal("21111"));
        InstantBatchPayoutDto batchDto = new InstantBatchPayoutDto();
        batchDto.getPayouts().add(dto);

        ctx.setModel(batchDto);
    }
}
