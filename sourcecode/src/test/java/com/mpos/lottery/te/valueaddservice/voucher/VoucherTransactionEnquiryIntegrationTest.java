package com.mpos.lottery.te.valueaddservice.voucher;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

public class VoucherTransactionEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Rollback(true)
    @Test
    public void test_Enquiry_OK() throws Exception {
        printMethod();
        Voucher reqDto = VoucherDomainMocker.mockVoucherTopup();

        // 1. buy voucher
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), reqDto);
        saleReqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));

        this.getEntityManager().flush();
        this.getEntityManager().clear();
        assertEquals(SystemException.CODE_OK, saleRespCtx.getResponseCode());

        // 2. enquiry
        Transaction trans = new Transaction();
        trans.setDeviceId(saleReqCtx.getTerminalId());
        trans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context ctx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        Context respCtx = doPost(this.mockRequest(ctx));
        Transaction respTrans = (Transaction) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        Voucher voucherDto = (Voucher) respTrans.getObject();
        assertEquals(reqDto.getFaceAmount().doubleValue(), voucherDto.getFaceAmount().doubleValue(), 0);
        assertEquals("V-1", voucherDto.getSerialNo());
        assertEquals("V-1-PIN", voucherDto.getPlainPin());
        assertTrue(voucherDto.getExpireDate() != null);
        assertEquals(reqDto.getGame().getId(), voucherDto.getGame().getId());
        assertEquals(reqDto.getGame().getId(), voucherDto.getGame().getId());
    }

}
