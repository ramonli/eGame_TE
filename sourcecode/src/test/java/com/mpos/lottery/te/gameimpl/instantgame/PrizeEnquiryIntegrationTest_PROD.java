package com.mpos.lottery.te.gameimpl.instantgame;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantGameDraw;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.junit.Test;

import java.util.List;

/**
 * For debug the test cases on Test or Prod environment.
 * 
 * @author Ramon
 * 
 */
public class PrizeEnquiryIntegrationTest_PROD extends BaseServletIntegrationTest {

    /**
     * Unmatched XOR which will cause system to count the failed validation attempts.
     */
    @Test
    public void testEnquiry_UnmatchedXOR_PROD() throws Exception {
        printMethod();
        InstantTicket ticket = new InstantTicket();
        ticket.setRawSerialNo("004000075047");
        ticket.setTicketXOR3("11111111");
        this.switchValidationType(InstantGameDraw.VALIDATION_TYPE_EGAME);

        Context reqCtx = this.getDefaultContext(TransactionType.IG_PRIZE_ENQUIRY.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        reqCtx.setTerminalId(141);
        Gpe gpe = new Gpe();
        gpe.setId("IGPE");
        reqCtx.setGpe(gpe);
        reqCtx.setOperatorId("ff808081409a940201409eda7ed30064");
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

    protected WorkingKey getDefaultWorkingKey() {
        WorkingKey workingKey = new WorkingKey();
        workingKey.setDataKey("W0JANjZiMjQ0Mzc0OGQ1YjNlMS1hNTlh");
        workingKey.setMacKey("Lpji1/XoW6edUnNisYRV84pKPAN4jbGD");
        return workingKey;
    }

    protected void switchValidationType(int validationType) {
        this.jdbcTemplate.update("update ig_game_instance set VALIDATION_TYPE=" + validationType);
    }
}
