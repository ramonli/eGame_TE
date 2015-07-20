package com.mpos.lottery.te.gameimpl.instantgame;

import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.InstantBatchPayoutDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelItemDto;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

public class BatchUploadOfflineValidationIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testOfflineBatchValidation_OK() throws Exception {
        printMethod();
        // TODO
        // InstantBatchPayoutDto reqDto = this.mock();
        // this.switchValidationType("IGII-111",
        // InstantGameDraw.VALIDATION_TYPE_EGAME);
        //
        // Context reqCtx =
        // this.getDefaultContext(TransactionType.IG_VALIDATION_BATCH_UPLOAD.getRequestType(),
        // reqDto);
        // reqCtx.setGameTypeId(Game.TYPE_INSTANT + "");
        // Context respCtx = this.doPost(this.mockRequest(reqCtx));
        //
        // assertEquals(200, respCtx.getResponseCode());
        // // assert the response
        // InstantBatchPayoutDto dto = (InstantBatchPayoutDto)
        // respCtx.getModel();
        // assertEquals(1, dto.getPayouts().size());
    }

    protected void switchValidationType(int validationType) {
        this.jdbcTemplate.update("update ig_game_instance set VALIDATION_TYPE=" + validationType);
    }

    public InstantBatchPayoutDto mock() {
        InstantBatchPayoutDto dto = new InstantBatchPayoutDto();
        // 1st prize level
        PrizeLevelDto level = new PrizeLevelDto();
        level.setPrizeAmount(new BigDecimal("400000.0"));
        level.setTaxAmount(new BigDecimal("80000.0"));
        level.setActualAmount(new BigDecimal("320000.0"));
        level.setValidateTime(new Date());

        InstantTicket ticket1 = new InstantTicket();
        ticket1.setSerialNo("157823119021");
        ticket1.setTicketXOR3("95497797");
        level.setTicket(ticket1);

        PrizeLevelItemDto item = new PrizeLevelItemDto();
        item.setPrizeType(PrizeLevelDto.PRIZE_TYPE_CASH);
        item.setNumberOfObject(1);
        item.setPrizeAmount(new BigDecimal("400000.0"));
        item.setTaxAmount(new BigDecimal("80000.0"));
        level.getLevelItems().add(item);
        dto.getPayouts().add(level);

        // duplicated prize level
        PrizeLevelDto level1 = new PrizeLevelDto();
        level1.setPrizeAmount(new BigDecimal("400000.0"));
        level1.setTaxAmount(new BigDecimal("80000.0"));
        level1.setActualAmount(new BigDecimal("320000.0"));
        level1.setValidateTime(new Date());
        level1.setTicket(ticket1);

        PrizeLevelItemDto item1 = new PrizeLevelItemDto();
        item1.setPrizeType(PrizeLevelDto.PRIZE_TYPE_CASH);
        item1.setNumberOfObject(1);
        item1.setPrizeAmount(new BigDecimal("400000.0"));
        item1.setTaxAmount(new BigDecimal("80000.0"));
        level1.getLevelItems().add(item1);
        dto.getPayouts().add(level1);

        // 2nd prize level
        PrizeLevelDto level2 = new PrizeLevelDto();
        level2.setPrizeAmount(new BigDecimal("300060.0"));
        level2.setTaxAmount(new BigDecimal("2400.0"));
        level2.setActualAmount(new BigDecimal("276060.0"));
        level2.setValidateTime(new Date());

        InstantTicket ticket2 = new InstantTicket();
        ticket2.setSerialNo("198415681983");
        ticket2.setTicketXOR3("37330218");
        level2.setTicket(ticket2);

        PrizeLevelItemDto item21 = new PrizeLevelItemDto();
        item21.setPrizeType(PrizeLevelDto.PRIZE_TYPE_CASH);
        item21.setNumberOfObject(1);
        item21.setPrizeAmount(new BigDecimal("300000"));
        item21.setTaxAmount(new BigDecimal("2400.0"));
        level2.getLevelItems().add(item21);

        PrizeLevelItemDto item22 = new PrizeLevelItemDto();
        item22.setPrizeType(PrizeLevelDto.PRIZE_TYPE_OBJECT);
        item22.setNumberOfObject(2);
        item22.setPrizeAmount(new BigDecimal("30.0"));
        item22.setTaxAmount(new BigDecimal("0.0"));
        item22.setObjectName("Dog Pull");
        level2.getLevelItems().add(item22);

        dto.getPayouts().add(level2);
        return dto;
    }
}
