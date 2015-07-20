package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.merchant.web.CashOutByOperatorPassDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class OperatorCashoutByPassIntegrateTest extends BaseServletIntegrationTest {

    // @Rollback(false)
    @Test
    public void test() throws Exception {

        printMethod();
        CashOutByOperatorPassDto dto = mockDto();

        Context reqCtx = this.getDefaultContext(TransactionType.CASH_OUT_OPERATOR_PASS.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CashOutByOperatorPassDto respDto = (CashOutByOperatorPassDto) respCtx.getModel();

        assertNotNull(respCtx);
    }

    private CashOutByOperatorPassDto mockDto() {
        CashOutByOperatorPassDto dto = new CashOutByOperatorPassDto();
        dto.setBarcode("00cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt+Zh8NCsYChdtc1Tr86hJ9URtgXeRMhtcc=");
        dto.setPassword("1234111111111");
        return dto;
    }
}
