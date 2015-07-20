package com.mpos.lottery.te.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class EnquiryTransactionAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testGetTransaction() throws Exception {
        Context response = doPost();
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.TRANSACTION_ENQUIRY.getRequestType());
        Transaction trans = new Transaction();
        trans.setDeviceId(111);
        trans.setTraceMessageId("TMI-111");
        ctx.setModel(trans);
    }

}
