package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class ConfirmBatchValidationReportTest extends BaseServletIntegrationTest {
    @Test
    public void testConfirmBatch() throws Exception {
        // issue payout request

        InstantBatchReportDto dto = new InstantBatchReportDto();
        dto.setBatchNumber(2);

        /*
         * List<PrizeLevelDto> payouts = new LinkedList<PrizeLevelDto>(); PrizeLevelDto prizeDto = new PrizeLevelDto();
         * prizeDto.setInputChannel(1); prizeDto.setClientPrizeAmount(new BigDecimal(31111)); InstantTicket ticket = new
         * InstantTicket(); ticket.setRawSerialNo("198415681983"); ticket.setTicketXOR3("27330200");
         * 
         * this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN); prizeDto.setTicket(ticket); //
         * prizeDto.setPrizeAmount(new BigDecimal(200)); payouts.add(prizeDto);
         * 
         * dto.setPayouts(payouts);
         */

        Context payoutCtx = this.getDefaultContext(TransactionType.CONFIRM_BATCH_VALIDATION.getRequestType(), dto);
        payoutCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));
        // assert response
        assertEquals(200, payoutRespCtx.getResponseCode());
        InstantBatchReportDto respDto = (InstantBatchReportDto) payoutRespCtx.getModel();
        assertEquals(new BigDecimal(12000), respDto.getActualAmount());
        assert (respDto.getTickets().size() == 1);
        assert (respDto.getTotalFail() == 1);
        assert (respDto.getTotalSuccess() == 1);
    }

    protected void switchValidationType(int validationType) {
        this.jdbcTemplate.update("update ig_game_instance set VALIDATION_TYPE=" + validationType);
    }
}
