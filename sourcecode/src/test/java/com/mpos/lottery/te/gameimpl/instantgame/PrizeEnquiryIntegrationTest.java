package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.util.List;

public class PrizeEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testPrizeEnquiry_OK() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("157823119021");
        ticket.setTicketXOR3("95497797");
        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);

        Context reqCtx = this.getDefaultContext(TransactionType.IG_PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        // assert the response
        PrizeLevelDto dto = (PrizeLevelDto) respCtx.getModel();
        assertEquals(1, dto.getLevelItems().size());
        List<PrizeLevelItemDto> prizeItems = dto.getItemByPrizeType(PrizeLevelDto.PRIZE_TYPE_CASH);
        assertEquals(1, prizeItems.size());
        // prizeItems = dto.getItemByPrizeType(PrizeLevel.PRIZE_TYPE_OBJECT);
        // assertEquals(1, prizeItems.size());
    }

    /**
     * Test bug#2894
     */
    @Test
    public void testPrizeEnquiry_IncorrectVirn() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("157823119021");
        ticket.setTicketXOR3("95497797-X");
        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);

        Context reqCtx = this.getDefaultContext(TransactionType.IG_PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(SystemException.CODE_XORMD5_NOTMATCH, respCtx.getResponseCode());
        // TODO assert output of table 'Transaction_retry_log'
    }

    /**
     * Prize enquiry a validated ticket.
     */
    @Test
    public void testPrizeEnquiry_Validated_OK() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("157823119021");
        ticket.setTicketXOR3("95497797");
        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);
        this.jdbcTemplate.update("update INSTANT_TICKET t set t.STATUS=" + InstantTicket.STATUS_VALIDATED
                + " where t.TICKET_SERIAL='" + ticket.getSerialNo() + "'");

        Context reqCtx = this.getDefaultContext(TransactionType.IG_PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(SystemException.CODE_VALIDATE_REPEAT, respCtx.getResponseCode());
    }

    protected void switchValidationType(int validationType) {
        this.jdbcTemplate.update("update ig_game_instance set VALIDATION_TYPE=" + validationType);
    }
}
