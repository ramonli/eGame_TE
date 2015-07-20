package com.mpos.lottery.te.gameimpl.lfn.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.gameimpl.lfn.LfnDomainMocker;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class LfnSaleEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiryTicket_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        LfnTicket ticketDto = (LfnTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        ticket.setRawSerialNo(ticketDto.getRawSerialNo());
        Context enquiryCtx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        enquiryCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context enquiryRespCtx = doPost(this.mockRequest(enquiryCtx));
        LfnTicket respTicket = (LfnTicket) enquiryRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, enquiryRespCtx.getResponseCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(ticket.getEntries().size(), respTicket.getEntries().size());
        assertNull(respTicket.getUser());
        assertEquals(ticketDto.getValidationCode(), respTicket.getValidationCode());
        assertEquals(ticketDto.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(ticketDto.getTotalBets(), respTicket.getTotalBets());
    }

    @Test
    public void testEnquirySaleTransaction_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        LfnTicket ticketDto = (LfnTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(saleCtx.getTerminalId());
        trans.setTraceMessageId(saleCtx.getTraceMessageId());
        Context enquiryCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        enquiryCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context enquiryRespCtx = doPost(this.mockRequest(enquiryCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, enquiryRespCtx.getResponseCode());
        Transaction hostTrans = (Transaction) enquiryRespCtx.getModel();
        LfnTicket respTicket = (LfnTicket) hostTrans.getTicket();
        assertNotNull(respTicket);
        assertEquals(2, respTicket.getEntries().size());
        assertNull(respTicket.getUser());
        assertEquals(ticketDto.getValidationCode(), respTicket.getValidationCode());
        assertEquals(ticketDto.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(ticketDto.getTotalBets(), respTicket.getTotalBets());
    }

}
