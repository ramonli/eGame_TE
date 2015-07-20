package com.mpos.lottery.te.gameimpl.digital.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.DigitalDomainMocker;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

public class SaleEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiryByTicket() throws Exception {
        printMethod();
        DigitalTicket ticket = DigitalDomainMocker.mockTicket();

        // 1st. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        DigitalTicket saleRespTicket = (DigitalTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OK, saleRespCtx.getResponseCode());

        // 2nd, enquiry ticket
        ticket.setRawSerialNo(saleRespTicket.getRawSerialNo());
        Context ctx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        // assert ticket
        assertEquals(ticket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(ticket.getRawSerialNo(), respTicket.getRawSerialNo());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(BaseTicket.STATUS_ACCEPTED, respTicket.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(saleRespTicket.getValidationCode(), respTicket.getValidationCode());
        assertEquals(saleRespTicket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(saleRespTicket.getTotalBets(), respTicket.getTotalBets());
        // assert game instance
        BaseGameInstance gameInstance = respTicket.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        assertEquals(2, respTicket.getEntries().size());
        DigitalEntry entry0 = (DigitalEntry) respTicket.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), entry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), entry0.getBetOption());
        DigitalEntry entry1 = (DigitalEntry) respTicket.getEntries().get(1);
        assertEquals(SimpleToolkit.formatNumericString(ticket.getEntries().get(1).getSelectNumber(), ","),
                entry1.getSelectNumber());
        assertEquals(ticket.getEntries().get(1).getBetOption(), entry1.getBetOption());

        // assert user
        assertEquals(ticket.getUser().getMobile(), respTicket.getUser().getMobile());
        assertEquals(ticket.getUser().getCreditCardSN(), respTicket.getUser().getCreditCardSN());
    }

    @Test
    public void testEnquiryByTrans() throws Exception {
        printMethod();
        DigitalTicket ticket = DigitalDomainMocker.mockTicket();

        // 1st. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_DIGITAL + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        DigitalTicket saleRespTicket = (DigitalTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2nd, enquiry transaction
        Transaction trans = new Transaction();
        trans.setDeviceId(saleReqCtx.getTerminalId());
        trans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context ctx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        Context respCtx = doPost(this.mockRequest(ctx));
        Transaction respTrans = (Transaction) respCtx.getModel();

        assertEquals(200, respCtx.getResponseCode());
        // assert trans
        assertEquals(saleRespCtx.getTransactionID(), respTrans.getId());
        assertEquals(SystemException.CODE_OK, respTrans.getResponseCode());
        assertEquals(TransactionType.SELL_TICKET.getRequestType(), respTrans.getType());
        // assert ticket
        DigitalTicket ticketDto = (DigitalTicket) respTrans.getTicket();
        assertEquals(saleRespTicket.getRawSerialNo(), ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.STATUS_ACCEPTED, ticketDto.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals(saleRespTicket.getValidationCode(), ticketDto.getValidationCode());
        assertEquals(saleRespTicket.getMultipleDraws(), ticketDto.getMultipleDraws());
        assertEquals(saleRespTicket.getTotalBets(), ticketDto.getTotalBets());

        // assert entries
        assertEquals(2, ticketDto.getEntries().size());
        DigitalEntry entry0 = (DigitalEntry) ticketDto.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), entry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), entry0.getBetOption());
        DigitalEntry entry1 = (DigitalEntry) ticketDto.getEntries().get(1);
        assertEquals(SimpleToolkit.formatNumericString(ticket.getEntries().get(1).getSelectNumber(), ","),
                entry1.getSelectNumber());
        assertEquals(ticket.getEntries().get(1).getBetOption(), entry1.getBetOption());

        // assert user
        assertEquals(ticket.getUser().getMobile(), ticketDto.getUser().getMobile());
        assertEquals(ticket.getUser().getCreditCardSN(), ticketDto.getUser().getCreditCardSN());
    }

}
