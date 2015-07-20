package com.mpos.lottery.te.valueaddservice.airtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class CoobillAirtimeTopupAcceptanceTest extends BaseAcceptanceTest {

    @Test
    public void testAirtimeTopup_OK() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());
        request.setGameTypeId(GameType.AIRTIME.getType() + "");

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(200, response.getResponseCode());
        assertNotNull(response.getModel());
        assertNotNull(response.getTransactionID());
        AirtimeTopup respDto = (AirtimeTopup) response.getModel();
        assertEquals(AirtimeTopup.STATUS_SUCCESS, respDto.getStatus());
    }

    @Test
    public void testAirtimeTopup_Pending() throws Exception {
        Context request = this.mockRequestContext();
        request.setWorkingKey(this.getDefaultWorkingKey());
        request.setGameTypeId(GameType.AIRTIME.getType() + "");
        // refer to AirtimePortServlet...mobile:222 will trigger timeout.
        AirtimeTopup reqDto = (AirtimeTopup) request.getModel();
        reqDto.setMobileNo("222");

        Context response = this.post(request);
        assertNotNull(response);
        assertEquals(SystemException.CODE_REMOTE_SERVICE_TIMEOUT, response.getResponseCode());
        assertNotNull(response.getModel());
        assertNotNull(response.getTransactionID());
        AirtimeTopup respDto = (AirtimeTopup) response.getModel();
        assertEquals(AirtimeTopup.STATUS_PENDING, respDto.getStatus());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        // set sell ticket transaction
        ctx.setTransType(TransactionType.AIRTIME_TOPUP.getRequestType());
        AirtimeTopup reqDto = AirtimeDomainMocker.mockCoobillAirtimeTopup();
        ctx.setModel(reqDto);
    }

}
