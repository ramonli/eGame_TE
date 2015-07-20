package com.mpos.lottery.te.gameimpl.toto.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.toto.TotoDomainMocker;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToEntry;
import com.mpos.lottery.te.gameimpl.toto.domain.ToToTicket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

import javax.annotation.Resource;

public class SaleEnquiryIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;

    @Test
    public void testEnquiryByTicket() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));

        // 1. make sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_TOTO + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        ToToTicket saleRespTicket = (ToToTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2nd, enquiry ticket
        ToToTicket enquiryTicket = new ToToTicket();
        enquiryTicket.setRawSerialNo(saleRespTicket.getRawSerialNo());
        Context ctx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), enquiryTicket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        // assert ticket
        assertEquals(ticket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(enquiryTicket.getRawSerialNo(), respTicket.getRawSerialNo());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(BaseTicket.STATUS_ACCEPTED, respTicket.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(saleRespTicket.getBarcode(), respTicket.getBarcode());
        assertEquals(saleRespTicket.getValidationCode(), respTicket.getValidationCode());
        assertEquals(saleRespTicket.getMultipleDraws(), respTicket.getMultipleDraws());
        assertEquals(saleRespTicket.getTotalBets(), respTicket.getTotalBets());
        // assert game instance
        BaseGameInstance gameInstance = respTicket.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        assertEquals(1, respTicket.getEntries().size());
        ToToEntry entry0 = (ToToEntry) respTicket.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), entry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), entry0.getBetOption());
    }

    @Test
    public void testEnquiryByTrans() throws Exception {
        printMethod();
        ToToTicket ticket = TotoDomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("600.0"));

        // 1. make sale
        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_TOTO + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        ToToTicket respTicket = (ToToTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. enquiry sale
        Transaction trans = new Transaction();
        trans.setDeviceId(ctx.getTerminalId());
        trans.setTraceMessageId(ctx.getTraceMessageId());
        Context enqCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), trans);
        Context enqRespCtx = doPost(this.mockRequest(enqCtx));
        Transaction respTrans = (Transaction) enqRespCtx.getModel();

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        // assert trans
        assertEquals(respCtx.getTransactionID(), respTrans.getId());
        assertEquals(SystemException.CODE_OK, respTrans.getResponseCode());
        assertEquals(TransactionType.SELL_TICKET.getRequestType(), respTrans.getType());
        // assert ticket
        ToToTicket ticketDto = (ToToTicket) respTrans.getTicket();
        assertEquals(respTicket.getRawSerialNo(), ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.STATUS_ACCEPTED, ticketDto.getStatus());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals(respTicket.getBarcode(), ticketDto.getBarcode());
        assertEquals(respTicket.getValidationCode(), ticketDto.getValidationCode());
        assertEquals(respTicket.getMultipleDraws(), ticketDto.getMultipleDraws());
        assertEquals(respTicket.getTotalBets(), ticketDto.getTotalBets());

        // assert game instance
        BaseGameInstance gameInstance = ticketDto.getGameInstance();
        assertEquals(ticket.getGameInstance().getGameId(), gameInstance.getGameId());
        assertEquals(ticket.getGameInstance().getNumber(), gameInstance.getNumber());
        // assert entries
        assertEquals(1, ticketDto.getEntries().size());
        ToToEntry entry0 = (ToToEntry) ticketDto.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), entry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), entry0.getBetOption());
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }
}
