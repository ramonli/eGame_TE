package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.merchant.web.CashOutPassDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class OperatorGetCashoutPassIntegrateTest extends BaseServletIntegrationTest {

    // @Rollback(false)
    @Test
    public void test() throws Exception {

        printMethod();
        CashOutPassDto dto = mockDto();

        Context reqCtx = this.getDefaultContext(TransactionType.GET_CASH_OUT_PASS.getRequestType(), dto);
        // reqCtx.setGameTypeId(String.valueOf(Game.TYPE_UNDEF));//set header game_type_id
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CashOutPassDto respDto = (CashOutPassDto) respCtx.getModel();

        assertNotNull(respCtx);
    }

    private CashOutPassDto mockDto() {
        CashOutPassDto dto = new CashOutPassDto();
        dto.setAmount(new BigDecimal("100"));
        dto.setPassword("1234");
        return dto;
    }

}
