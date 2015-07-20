package com.mpos.lottery.te.gameimpl.magic100.prize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class MagicPrizeEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiry_TaxOnPayout_NewPrint_MultiDraw_OK() throws Exception {
        printMethod();
        Magic100Ticket ticket = new Magic100Ticket();
        ticket.setRawSerialNo("S-123456");
        ticket.setValidationCode("111111");

        this.jdbcTemplate.update("delete from ld_winning");
        this.jdbcTemplate.update("update GAME set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT
                + " where GAME_TYPE_ID=18");

        Context ctx = this.getDefaultContext(TransactionType.PRIZE_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        PrizeDto prize = (PrizeDto) respCtx.getModel();
        assertEquals(833.33, prize.getActualAmount().doubleValue(), 0);
        assertEquals(1000, prize.getPrizeAmount().doubleValue(), 0);
        assertEquals(166.67, prize.getTaxAmount().doubleValue(), 0);
        assertFalse(prize.isVerifyPIN());
        assertEquals(BaseOperationParameter.PAYOUTMODE_REFUND, prize.getPayoutMode());

    }

}
