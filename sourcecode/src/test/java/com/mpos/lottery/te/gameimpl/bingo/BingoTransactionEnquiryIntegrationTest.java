package com.mpos.lottery.te.gameimpl.bingo;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class BingoTransactionEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiry() throws Exception {
        printMethod();
        Transaction trans = new Transaction();
        trans.setDeviceId(111);
        trans.setTraceMessageId("TMI-091");

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        reqCtx.setGameTypeId("-1");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
    }

}
