package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ConfirmBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchReportDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class PartialPackageTest extends BaseServletIntegrationTest {
    @Test
    public void testPartialPackage() throws Exception {
        // issue payout request

        ConfirmBatchPayoutDto dto = new ConfirmBatchPayoutDto();
        dto.setBatchNumber(1);

        List<PrizeLevelDto> payouts = new LinkedList<PrizeLevelDto>();
        PrizeLevelDto prizeDto = new PrizeLevelDto();
        prizeDto.setInputChannel(1);
        prizeDto.setClientPrizeAmount(new BigDecimal(31111));
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("198415681983");
        ticket.setTicketXOR3("27330200");

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        prizeDto.setTicket(ticket);
        // prizeDto.setPrizeAmount(new BigDecimal(200));
        payouts.add(prizeDto);

        PrizeLevelDto prizeDto2 = new PrizeLevelDto();
        prizeDto2.setInputChannel(1);
        prizeDto2.setClientPrizeAmount(new BigDecimal(31111));
        InstantTicket ticket2 = new InstantTicket();
        ticket2.setRawSerialNo("23423aaaaafgddgdfg");
        ticket2.setTicketXOR3("27330200");

        // this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        prizeDto2.setTicket(ticket2);
        // prizeDto.setPrizeAmount(new BigDecimal(200));
        payouts.add(prizeDto2);

        dto.setPayouts(payouts);

        Context payoutCtx = this.getDefaultContext(TransactionType.PARTIAL_BATCH_VALIDATION.getRequestType(), dto);
        payoutCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));
        // assert response
        assertEquals(200, payoutRespCtx.getResponseCode());
        InstantBatchReportDto respDao = (InstantBatchReportDto) payoutRespCtx.getModel();
        // assert(respDao.getTickets().size()!=1);
        // ConfirmBatchPayoutDto respDto = (ConfirmBatchPayoutDto) payoutRespCtx.getModel();
    }

    @Test
    public void testPartialPackageFailed() throws Exception {
        // issue payout request

        ConfirmBatchPayoutDto dto = new ConfirmBatchPayoutDto();
        dto.setBatchNumber(1);

        List<PrizeLevelDto> payouts = new LinkedList<PrizeLevelDto>();
        PrizeLevelDto prizeDto = new PrizeLevelDto();
        prizeDto.setInputChannel(1);
        prizeDto.setClientPrizeAmount(new BigDecimal(31111));
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("19841568198332324234");
        ticket.setTicketXOR3("27330200");

        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        prizeDto.setTicket(ticket);
        // prizeDto.setPrizeAmount(new BigDecimal(200));
        payouts.add(prizeDto);

        PrizeLevelDto prizeDto2 = new PrizeLevelDto();
        prizeDto2.setInputChannel(1);
        prizeDto2.setClientPrizeAmount(new BigDecimal(31111));
        InstantTicket ticket2 = new InstantTicket();
        ticket2.setRawSerialNo("23423aaaaafgddgdfg23432423");
        ticket2.setTicketXOR3("27330200");

        // this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_VIRN);
        prizeDto2.setTicket(ticket2);
        // prizeDto.setPrizeAmount(new BigDecimal(200));
        payouts.add(prizeDto2);

        dto.setPayouts(payouts);

        Context payoutCtx = this.getDefaultContext(TransactionType.PARTIAL_BATCH_VALIDATION.getRequestType(), dto);
        payoutCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        Context payoutRespCtx = doPost(this.mockRequest(payoutCtx));
        // assert response
        assertEquals(200, payoutRespCtx.getResponseCode());
        InstantBatchReportDto respDao = (InstantBatchReportDto) payoutRespCtx.getModel();
        // assert(respDao.getTickets().size()!=2);
        // ConfirmBatchPayoutDto respDto = (ConfirmBatchPayoutDto) payoutRespCtx.getModel();
    }

    protected void switchValidationType(int validationType) {
        this.jdbcTemplate.update("update ig_game_instance set VALIDATION_TYPE=" + validationType);
    }
}
