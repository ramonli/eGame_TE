package com.mpos.lottery.te.valueaddservice.vat;

import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.merchant.web.VatTransferDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class VatTransferServletIntegrationTest extends BaseServletIntegrationTest {
    // @Rollback(false)
    @Test
    public void testTransfer() throws Exception {

        printMethod();
        VatTransferDto dto = mockDto();

        Context reqCtx = this.getDefaultContext(TransactionType.VAT_TRANSFER_CREDIT.getRequestType(), dto);
        reqCtx.setGameTypeId(String.valueOf(Game.TYPE_VAT));// set header game_type_id
        Context respCtx = doPost(this.mockRequest(reqCtx));
        VatTransferDto respDto = (VatTransferDto) respCtx.getModel();

        assertNotNull(respCtx);
    }

    private VatTransferDto mockDto() {
        VatTransferDto dto = new VatTransferDto();
        dto.setFromOperatorLoginName("8000");
        dto.setToOperatorLoginName("8888");
        dto.setCreditType(VatTransferDto.CREDITTYPE_SALE);
        dto.setAmount(new BigDecimal("100"));
        return dto;
    }
}
