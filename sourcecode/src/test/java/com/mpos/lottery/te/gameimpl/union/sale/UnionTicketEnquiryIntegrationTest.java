package com.mpos.lottery.te.gameimpl.union.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.union.UnionDomainMocker;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

public class UnionTicketEnquiryIntegrationTest extends BaseServletIntegrationTest {

    @Test
    public void testEnquiryByTicket() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        // 1st. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        UnionTicket saleRespTicket = (UnionTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2nd, enquiry ticket
        ticket.setRawSerialNo(saleRespTicket.getRawSerialNo());
        Context ctx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        UnionTicket respTicket = (UnionTicket) respCtx.getModel();

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
        assertEquals(saleRespTicket.getLastDrawNo(), "20090416");

        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), respEntry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), respEntry0.getBetOption());
        assertEquals(ticket.getEntries().get(0).getInputChannel(), respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);

        // assert game instance
        BaseGameInstance gameInstance = respTicket.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        assertEquals(2, respTicket.getEntries().size());
        UnionEntry entry0 = (UnionEntry) respTicket.getEntries().get(0);
        assertEquals(100.0, entry0.getEntryAmount().doubleValue(), 0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), entry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), entry0.getBetOption());
        UnionEntry entry1 = (UnionEntry) respTicket.getEntries().get(1);
        assertEquals(700.0, entry1.getEntryAmount().doubleValue(), 0);
        assertEquals(ticket.getEntries().get(1).getSelectNumber(), entry1.getSelectNumber());
        assertEquals(ticket.getEntries().get(1).getBetOption(), entry1.getBetOption());
    }

    @Test
    public void testEnquiryByTrans() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        // 1st. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        UnionTicket saleRespTicket = (UnionTicket) saleRespCtx.getModel();

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
        UnionTicket ticketDto = (UnionTicket) respTrans.getTicket();
        assertEquals(saleRespTicket.getRawSerialNo(), ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.STATUS_ACCEPTED, ticketDto.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals(saleRespTicket.getValidationCode(), ticketDto.getValidationCode());
        assertEquals(saleRespTicket.getMultipleDraws(), ticketDto.getMultipleDraws());
        assertEquals(saleRespTicket.getTotalBets(), ticketDto.getTotalBets());
        assertEquals(saleRespTicket.getLastDrawNo(), "20090416");

        this.sortTicketEntries(ticketDto.getEntries());
        BaseEntry respEntry0 = ticketDto.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), respEntry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), respEntry0.getBetOption());
        assertEquals(ticket.getEntries().get(0).getInputChannel(), respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);

        BaseGameInstance gameInstance = ticketDto.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        assertEquals(2, ticketDto.getEntries().size());
        UnionEntry entry0 = (UnionEntry) ticketDto.getEntries().get(0);
        assertEquals(100.0, entry0.getEntryAmount().doubleValue(), 0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), entry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), entry0.getBetOption());
        UnionEntry entry1 = (UnionEntry) ticketDto.getEntries().get(1);
        assertEquals(700.0, entry1.getEntryAmount().doubleValue(), 0);
        assertEquals(ticket.getEntries().get(1).getSelectNumber(), entry1.getSelectNumber());
        assertEquals(ticket.getEntries().get(1).getBetOption(), entry1.getBetOption());
    }

}
