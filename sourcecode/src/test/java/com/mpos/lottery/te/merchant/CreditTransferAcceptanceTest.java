package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.merchant.web.CreditTransferDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class CreditTransferAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testSell() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
        assertNotNull(response.getModel());
        CreditTransferDto dto = (CreditTransferDto) response.getModel();
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        // set sell ticket transaction
        ctx.setTransType(TransactionType.TRANSFER_CREDIT.getRequestType());
        ctx.setModel(CreditTransferIntegrationTest.mockDto());
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");
    }

}
