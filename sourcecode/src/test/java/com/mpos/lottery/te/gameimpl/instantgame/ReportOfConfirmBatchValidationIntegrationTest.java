package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class ReportOfConfirmBatchValidationIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testGetReportOfConfirmBatchValidation_OK() throws Exception {
        printMethod();
        InstantBatchReportDto dto = new InstantBatchReportDto();
        dto.setBatchNumber(200901);

        Context reqCtx = this.getDefaultContext(TransactionType.REPORT_OF_CONFIRM_BATCH_VALIDATION.getRequestType(),
                dto);
        reqCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        // assert the response
        InstantBatchReportDto resp = (InstantBatchReportDto) respCtx.getModel();
        assertEquals(200901, resp.getBatchNumber());
        assertEquals(100.00, resp.getActualAmount().doubleValue(), 0);
        assertEquals(5.00, resp.getTaxAmount().doubleValue(), 0);
        assertEquals(3, resp.getTotalSuccess(), 0);
        assertEquals(3, resp.getTotalFail(), 0);
        assertEquals(1, resp.getTickets().size(), 0);
        /*
         * assert(resp.getTickets().size()==1); assert(resp.getTotalFail()==1); assert(respDto.getTotalSuccess()==1);
         */

        assertEquals(resp.getTickets().get(0).getRawSerialNo(), "157823119021");

    }

}
