package com.mpos.lottery.te.gameimpl.extraball.trans;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallFunType;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class TransactionIntegrationTest extends BaseServletIntegrationTest {
    private TransactionDao transactionDao;

    @Test
    public void testEnquiry() throws Exception {
        printMethod();
        Transaction trans = new Transaction();
        trans.setDeviceId(111);
        trans.setTraceMessageId("TMI-EB-1");

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        reqCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context respCtx = this.doPost(this.mockRequest(reqCtx));

        assertEquals(200, respCtx.getResponseCode());
        Transaction respTrans = (Transaction) respCtx.getModel();
        assertEquals("TRANS-EB-1", respTrans.getId());
        assertEquals(TransactionType.SELL_TICKET.getRequestType(), respTrans.getType());
        BaseTicket ticket = (BaseTicket) respTrans.getTicket();
        assertEquals("20120711", ticket.getLastDrawNo());
        assertEquals("SN-EB-1", ticket.getRawSerialNo());
        assertEquals(BaseTicket.STATUS_ACCEPTED, ticket.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticket.getTicketType());
        ExtraBallFunType funType = (ExtraBallFunType) ticket.getGameInstance().getGame().getFunType();
        assertEquals(1, funType.getK());
        assertEquals(36, funType.getN());
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

}
