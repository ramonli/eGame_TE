package com.mpos.lottery.te.merchant.cancel;

import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.merchant.web.CashOutByManualDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class CashoutOperatorCancellationIntegrateTest extends BaseServletIntegrationTest {

    // @Rollback(false)
    @Test
    public void test() throws Exception {

        // CASH OUT BY MANUAL
        printMethod();
        CashOutByManualDto dto = mockDto();

        Context reqCtx = this.getDefaultContext(TransactionType.CASH_OUT_OPERATOR_MANUAL.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CashOutByManualDto respDto = (CashOutByManualDto) respCtx.getModel();

        assertNotNull(respCtx);

        // reversal
        printMethod();
        Transaction trans = new Transaction();
        trans.setDeviceId(reqCtx.getTerminalId()); // set in baseServletIntegrationTest
        trans.setTraceMessageId(reqCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        assertNotNull(cancelRespCtx);
    }

    private CashOutByManualDto mockDto() {
        CashOutByManualDto dto = new CashOutByManualDto();
        dto.setOperatorId("OPERATOR-LOGIN-2");
        dto.setPassword("OPERATOR-LOGIN");
        dto.setAmount(new BigDecimal("100"));
        return dto;
    }

}
