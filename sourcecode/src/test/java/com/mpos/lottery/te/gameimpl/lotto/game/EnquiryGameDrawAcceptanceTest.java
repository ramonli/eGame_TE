package com.mpos.lottery.te.gameimpl.lotto.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class EnquiryGameDrawAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testEnquiry() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());
        Context response = this.post(request);

        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.GAME_DRAW_ENQUIRY.getRequestType());
        LottoGameInstance draw = LottoDomainMocker.mockGameDraw();
        ctx.setModel(draw);
    }

}
