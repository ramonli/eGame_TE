package com.mpos.lottery.te.workingkey;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.Gpe;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Date;

public class WorkingKeyIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testGetWorkingKey_OK() throws Exception {
        this.printMethod();
        Context reqCtx = this.mockRequst();
        reqCtx.setOperator(null);
        // reqCtx.setTerminalId(terminalId)
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        WorkingKey workingKey = (WorkingKey) respCtx.getModel();
        assertEquals(KEY_DATA, workingKey.getDataKey());
        assertEquals(KEY_MAC, workingKey.getMacKey());
    }

    protected MockHttpServletRequest mockRequest(Context reqContext) throws Exception {
        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        // assemble request headers
        mockReq.addHeader(Context.HEADER_PROTOCAL_VERSION, reqContext.getProtocalVersion());
        mockReq.addHeader(Context.HEADER_GPE_ID, reqContext.getGpe().getId());
        mockReq.addHeader(Context.HEADER_TRANSACTION_TYPE, reqContext.getTransType() + "");
        mockReq.addHeader(Context.HEADER_TIMESTAMP, reqContext.getStrTimestamp());

        return mockReq;
    }

    protected Context mockRequst() {
        Context ctx = new Context();
        ctx.setProtocalVersion("1.5");
        Gpe gpe = new Gpe();
        gpe.setId("GPE-111");
        ctx.setGpe(gpe);
        // SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // ctx.setTimestamp(sdf.parse("20091201121212"));
        ctx.setTimestamp(new Date());
        ctx.setTransType(TransactionType.GET_WORKING_KEY.getRequestType());
        return ctx;
    }
}
