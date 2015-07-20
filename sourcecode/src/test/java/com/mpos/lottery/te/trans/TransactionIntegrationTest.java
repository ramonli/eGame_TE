package com.mpos.lottery.te.trans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.PendingTransactionDao;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;
import com.mpos.lottery.te.valueaddservice.voucher.Voucher;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherSale;

import org.junit.Test;

public class TransactionIntegrationTest extends BaseServletIntegrationTest {
    private PendingTransactionDao pendingTransactionDao;
    private TransactionDao transactionDao;
    private BaseTicketDao ticketDao;

    @Test
    public void testEnquiry() throws Exception {
        printMethod();
        Transaction trans = new Transaction();
        trans.setDeviceId(111);
        trans.setTraceMessageId("TMI-111");

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        reqCtx.setGameTypeId("-1");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
    }

    @Test
    public void testEnquiry_airtime() throws Exception {
        printMethod();
        Transaction trans = new Transaction();
        trans.setDeviceId(111);
        trans.setTraceMessageId("aritime_msg_1");

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        reqCtx.setGameTypeId("" + GameType.AIRTIME.getType());
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respCtx.getModel());
        Transaction transaction = (Transaction) respCtx.getModel();
        assertNotNull(transaction.getObject());
        AirtimeTopup airtimeTopup = (AirtimeTopup) transaction.getObject();
        assertEquals("13128988419", airtimeTopup.getMobileNo());
        assertEquals(100, airtimeTopup.getAmount().intValue());
        assertEquals(1, airtimeTopup.getStatus());
        assertEquals("AIRTIME-1", airtimeTopup.getGame().getId());

    }

//    @Test
//    public void testEnquiry_telecovoucher() throws Exception {
//        printMethod();
//        Transaction trans = new Transaction();
//        trans.setDeviceId(111);
//        trans.setTraceMessageId("telecovoucher_msg_1");
//
//        Context reqCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
//        reqCtx.setGameTypeId("" + GameType.TELECO_VOUCHER.getType());
//        Context respCtx = this.doPost(this.mockRequest(reqCtx));
//
//        assertEquals(200, respCtx.getResponseCode());
//        assertNotNull(respCtx.getModel());
//        Transaction transaction = (Transaction) respCtx.getModel();
//        assertNotNull(transaction.getObject());
//        Voucher voucher = (Voucher) transaction.getObject();
//        assertEquals("V-1", voucher.getSerialNo());
//    }
//
    // @Test
    // public void testReversal() throws Exception {
    // printMethod();
    // Transaction trans = new Transaction();
    // trans.setDeviceId(111);
    // trans.setTraceMessageId("TMI-112");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), trans);
    // reqCtx.setGameTypeId("-1");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertEquals(200, respCtx.getResponseCode());
    // Transaction reversalTrans = this.getTransactionDao().getByDeviceAndTraceMessage(
    // reqCtx.getTerminalId(), reqCtx.getTraceMessageId());
    // // assertEquals("S-888888", reversalTrans.getTicketSerialNo());
    // assertEquals("2nwUFkQGi64oJklaIP8Aaw==", reversalTrans.getTicketSerialNo());
    // assertEquals("GAME-111", reversalTrans.getGameId());
    //
    // List<LottoTicket> tickets = this.getTicketDao().getByTransaction("TRANS-112");
    // assertEquals(1, tickets.size());
    // assertEquals(LottoTicket.STATUS_INVALID, tickets.get(0).getStatus());
    // }

    // @Test
    // public void testMasterReversal_Pending() throws Exception {
    // printMethod();
    // Transaction trans = new Transaction();
    // trans.setDeviceId(111);
    // trans.setTraceMessageId("TMI-NOEXIST");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), trans);
    // reqCtx.setGameTypeId("-1");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // assertEquals(SystemException.CODE_NO_TRANSACTION, respCtx.getResponseCode());
    // List<PendingTransaction> list = this.getPendingTransactionDao().getByDeviceAndTraceMsgId(111,
    // "TMI-NOEXIST");
    // assertEquals(0, list.size());
    // }
    //
    // @Test
    // public void testReversal_NoTrans_SLAVE() throws Exception {
    // printMethod();
    // Transaction trans = new Transaction();
    // trans.setDeviceId(111);
    // trans.setTraceMessageId("TMI-NONEXIST");
    // this.simpleJdbcTemplate.update("update sys_configuration set server_type=2");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.REVERSAL.getRequestType(), trans);
    // reqCtx.setGameTypeId("-1");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // // assert response output
    // assertEquals(200, respCtx.getResponseCode());
    // // assert database output
    // List<PendingTransaction> dbTrans =
    // this.getPendingTransactionDao().getByDeviceAndTraceMsgId(111,
    // "TMI-NONEXIST");
    // assertEquals(1, dbTrans.size());
    // }
    //
    // @Test
    // public void testCancelByTicket_NoTicket_SLAVE() throws Exception {
    // printMethod();
    // LottoTicket ticket = new LottoTicket();
    // ticket.setRawSerialNo("TMI-NONEXIST");
    // // TODO: why can't pass this case?? seem below SQL doesn't work
    // this.simpleJdbcTemplate.update("update sys_configuration set server_type=2");
    //
    // Context reqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(),
    // ticket);
    // reqCtx.setGameTypeId("-1");
    // Context respCtx = this.doPost(this.mockRequest(reqCtx));
    //
    // // assert response output
    // assertEquals(200, respCtx.getResponseCode());
    // // assert database output
    // List<PendingTransaction> dbTrans = this.getPendingTransactionDao().getByTicketSerialNo(
    // ticket.getSerialNo());
    // assertEquals(1, dbTrans.size());
    // PendingTransaction pending = dbTrans.get(0);
    // assertEquals(ticket.getSerialNo(), pending.getTicketSerialNo());
    // assertEquals(reqCtx.getTerminalId(), pending.getDeviceId());
    // assertEquals(reqCtx.getTraceMessageId(), pending.getTraceMsgId());
    // }

    public PendingTransactionDao getPendingTransactionDao() {
        return pendingTransactionDao;
    }

    public void setPendingTransactionDao(PendingTransactionDao pendingTransactionDao) {
        this.pendingTransactionDao = pendingTransactionDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public BaseTicketDao getTicketDao() {
        return ticketDao;
    }

    public void setTicketDao(BaseTicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

}
