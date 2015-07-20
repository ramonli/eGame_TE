package com.mpos.lottery.te.valueaddservice.vat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.vat.web.OfflineTicketPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.SelectedNumberPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.TicketPackDto;
import com.mpos.lottery.te.valueaddservice.vat.web.VatRefNoPackDto;

import org.junit.Test;

public class DownloadOfflineTicketIntegrationTest extends BaseServletIntegrationTest {
    // @Rollback(false)
    //
    @Test
    public void testReservedNumbers_all_offlineCancellation() throws Exception {

        printMethod();
        OfflineTicketPackDto dto = mockDto();

        this.jdbcTemplate
                .update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID=2 where operator_id='OPERATOR-111'");

        Context reqCtx = this.getDefaultContext(TransactionType.RESERVED_NUMBERS.getRequestType(), dto);
        reqCtx.setGameTypeId(String.valueOf(GameType.LUCKYNUMBER.getType()));// set vat raffle -14
        Context respCtx = doPost(this.mockRequest(reqCtx));
        OfflineTicketPackDto respDto = (OfflineTicketPackDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertEquals(dto.getSelectedNumberPackDto().getRequestCount(), respDto.getSelectedNumberPackDto()
                .getNumberDtos().size());
        assertEquals(dto.getTicketPackDto().getRequestCount(), respDto.getTicketPackDto().getTickets().size());
        assertEquals(dto.getVatRefNoPackDto().getRequestCount(), respDto.getVatRefNoPackDto().getVatRefNoDtos().size());

    }

    @Test
    public void testReservedNumbers_all_luckyNumber() throws Exception {

        printMethod();
        OfflineTicketPackDto dto = mockDto();

        this.jdbcTemplate
                .update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID=2 where operator_id='OPERATOR-111'");

        this.jdbcTemplate.update("update lk_offline_cancellation set is_handled=1 where game_id='LK-1'");

        Context reqCtx = this.getDefaultContext(TransactionType.RESERVED_NUMBERS.getRequestType(), dto);
        reqCtx.setGameTypeId(String.valueOf(GameType.LUCKYNUMBER.getType()));// set vat raffle -14
        Context respCtx = doPost(this.mockRequest(reqCtx));
        OfflineTicketPackDto respDto = (OfflineTicketPackDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertEquals(dto.getSelectedNumberPackDto().getRequestCount(), respDto.getSelectedNumberPackDto()
                .getNumberDtos().size());
        assertEquals(dto.getTicketPackDto().getRequestCount(), respDto.getTicketPackDto().getTickets().size());
        assertEquals(dto.getVatRefNoPackDto().getRequestCount(), respDto.getVatRefNoPackDto().getVatRefNoDtos().size());
    }

    @Test
    public void testReservedNumbers_Combination_luckyNumberAndOfflineCancellation() throws Exception {

        printMethod();
        OfflineTicketPackDto dto = mockDto();

        this.jdbcTemplate
                .update("update VAT_OPERATOR_MERCHANT_TYPE set VAT_MERCHANT_TYPE_ID=2 where operator_id='OPERATOR-111'");

        this.jdbcTemplate.update("update lk_offline_cancellation set is_handled=1 where ID='LOC-1'");

        Context reqCtx = this.getDefaultContext(TransactionType.RESERVED_NUMBERS.getRequestType(), dto);
        reqCtx.setGameTypeId(String.valueOf(GameType.LUCKYNUMBER.getType()));// set vat raffle -14
        Context respCtx = doPost(this.mockRequest(reqCtx));
        OfflineTicketPackDto respDto = (OfflineTicketPackDto) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertNotNull(respCtx);
        assertEquals(200, respCtx.getResponseCode());
        assertEquals(dto.getSelectedNumberPackDto().getRequestCount(), respDto.getSelectedNumberPackDto()
                .getNumberDtos().size());
        assertEquals(dto.getTicketPackDto().getRequestCount(), respDto.getTicketPackDto().getTickets().size());
        assertEquals(dto.getVatRefNoPackDto().getRequestCount(), respDto.getVatRefNoPackDto().getVatRefNoDtos().size());
    }

    private OfflineTicketPackDto mockDto() {
        SelectedNumberPackDto selectedNumberPackDto = new SelectedNumberPackDto();
        selectedNumberPackDto.setRequestCount(10);

        OfflineTicketPackDto offlineTicketPackDto = new OfflineTicketPackDto();
        offlineTicketPackDto.setSelectedNumberPackDto(selectedNumberPackDto);
        offlineTicketPackDto.setVat(mockVat());

        VatRefNoPackDto vatRefNoPackDto = new VatRefNoPackDto();
        vatRefNoPackDto.setRequestCount(5);
        offlineTicketPackDto.setVatRefNoPackDto(vatRefNoPackDto);

        TicketPackDto ticketPackDto = new TicketPackDto();
        ticketPackDto.setRequestCount(6);
        offlineTicketPackDto.setTicketPackDto(ticketPackDto);
        return offlineTicketPackDto;
    }

    public static VAT mockVat() {
        VAT vat = new VAT();
        vat.setCode("foodA");
        return vat;
    }

}
