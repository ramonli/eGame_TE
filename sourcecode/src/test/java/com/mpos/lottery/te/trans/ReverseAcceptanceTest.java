package com.mpos.lottery.te.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class ReverseAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testReverse() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        ctx.setTransType(TransactionType.REVERSAL.getRequestType());
        Transaction trans = new Transaction();
        trans.setDeviceId(111);
        // trans.setTraceMessageId("202224231");
        trans.setTraceMessageId("00001.256121006078E9");
        ctx.setModel(trans);
    }

}
