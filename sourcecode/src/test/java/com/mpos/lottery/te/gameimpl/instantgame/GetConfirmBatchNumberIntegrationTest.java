package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class GetConfirmBatchNumberIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testGetConfirmBatchNumber_OK() throws Exception {
        printMethod();

        Context reqCtx = this.getDefaultContext(TransactionType.IG_GET_CONFIRM_BATCH_NUMBER.getRequestType());
        reqCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        // assert the response
        InstantBatchReportDto resp = (InstantBatchReportDto) respCtx.getModel();
        assertEquals(3, resp.getBatchNumber());

    }

}
