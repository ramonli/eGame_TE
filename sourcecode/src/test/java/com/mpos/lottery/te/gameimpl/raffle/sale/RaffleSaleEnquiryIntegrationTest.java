package com.mpos.lottery.te.gameimpl.raffle.sale;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.raffle.RaffleDomainMocker;
import com.mpos.lottery.te.gameimpl.raffle.game.RaffleGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import javax.annotation.Resource;

public class RaffleSaleEnquiryIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;

    @Test
    public void testEnquiryTicket() throws Exception {
        this.printMethod();
        // 1. make sale fist
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();
        ticket.setRawSerialNo(respTicket.getRawSerialNo());

        // fluch entity state to underlying database.
        this.entityManager.flush();
        // will detach all entities from EM, then any operations on entity will
        // be built from DB again.
        this.entityManager.clear();

        // 2. make enquiry
        Context enquiryReqCtx = this.getDefaultContext(TransactionType.TICKET_ENQUIRY.getRequestType(), ticket);
        enquiryReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context enquiryRespCtx = doPost(this.mockRequest(enquiryReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, enquiryRespCtx.getResponseCode());
        RaffleTicket hostTicket = (RaffleTicket) enquiryRespCtx.getModel();
        assertEquals(ticket.getTotalAmount().doubleValue(), hostTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(1, hostTicket.getMultipleDraws());
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTicket.getStatus());
        assertEquals(ticket.getRawSerialNo(), hostTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, hostTicket.getTicketType());
        RaffleGameInstance hostGameInstance = (RaffleGameInstance) hostTicket.getGameInstance();
        assertEquals("RA-1", hostGameInstance.getGameId());
        assertEquals("11002", hostGameInstance.getNumber());
        assertEquals(respTicket.getValidationCode(), hostTicket.getValidationCode());
        assertEquals(respTicket.getMultipleDraws(), hostTicket.getMultipleDraws());
        assertEquals(respTicket.getTotalBets(), hostTicket.getTotalBets());
        assertEquals(0, hostTicket.getEntries().size());
    }

    @Test
    public void testEnquiryByTransaction() throws Exception {
        this.printMethod();
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        // 1. make sale fist
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();
        ticket.setRawSerialNo(respTicket.getRawSerialNo());

        // fluch entity state to underlying database.
        this.entityManager.flush();
        // will detach all entities from EM, then any operations on entity will
        // be built from DB again.
        this.entityManager.clear();

        // 2. make enquiry
        Transaction saleTrans = new Transaction();
        saleTrans.setDeviceId(saleReqCtx.getTerminalId());
        saleTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context enquiryReqCtx = this.getDefaultContext(TransactionType.TRANSACTION_ENQUIRY.getRequestType(), saleTrans);
        Context enquiryRespCtx = doPost(this.mockRequest(enquiryReqCtx));
        Transaction hostTrans = (Transaction) enquiryRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, enquiryRespCtx.getResponseCode());
        assertEquals(TransactionType.SELL_TICKET.getRequestType(), hostTrans.getType());
        assertEquals(SystemException.CODE_OK, hostTrans.getResponseCode());
        assertEquals(saleRespCtx.getTransactionID(), hostTrans.getId());
        RaffleTicket hostTicket = (RaffleTicket) hostTrans.getTicket();
        assertEquals(BaseTicket.STATUS_ACCEPTED, hostTicket.getStatus());
        assertEquals(ticket.getRawSerialNo(), hostTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, hostTicket.getTicketType());
        assertEquals(respTicket.getValidationCode(), hostTicket.getValidationCode());
        assertEquals(respTicket.getMultipleDraws(), hostTicket.getMultipleDraws());
        assertEquals(respTicket.getTotalBets(), hostTicket.getTotalBets());
        assertEquals(0, hostTicket.getEntries().size());
    }

    // ----------------------------------------------------------
    // SPRING DEPENDENCY INJECTION
    // ----------------------------------------------------------

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
}
