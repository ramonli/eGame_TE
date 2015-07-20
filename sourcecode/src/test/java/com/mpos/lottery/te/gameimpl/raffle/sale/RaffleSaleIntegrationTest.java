package com.mpos.lottery.te.gameimpl.raffle.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
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
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class RaffleSaleIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;

    @Rollback(true)
    @Test
    public void testSale_OK() throws Exception {
        this.printMethod();
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, saleRespCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(ticket.getGameInstance().getNumber(), respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_RAFFLE, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(3, respTicket.getTotalBets());
        assertEquals(0, respTicket.getEntries().size());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(saleRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(saleReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(saleReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        expectTrans.setType(saleReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<RaffleTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, dbTickets.size());

        RaffleTicket expTicket = new RaffleTicket();
        expTicket.setSerialNo(respTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setDevId(saleReqCtx.getTerminalId());
        expTicket.setOperatorId(saleReqCtx.getOperatorId());
        expTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expTicket.setMerchantId(expectTrans.getMerchantId());
        expTicket.setTotalAmount(SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                new BigDecimal(ticket.getMultipleDraws())));
        expTicket.setMultipleDraws(1);
        expTicket.setMobile(ticket.getUser() != null ? ticket.getUser().getMobile() : null);
        expTicket.setCreditCardSN(ticket.getUser() != null ? ticket.getUser().getCreditCardSN() : null);
        expTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expTicket.setTotalBets(3);
        expTicket.setBarcode(respTicket.getBarcode());
        expTicket.setValidationCode(respTicket.getValidationCode());
        RaffleGameInstance gameInstance = new RaffleGameInstance();
        gameInstance.setId("GII-112");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));
    }

    @Rollback(true)
    @Test
    public void testSale_MultiDraw_OK() throws Exception {
        this.printMethod();
        RaffleTicket ticket = RaffleDomainMocker.ticket();
        ticket.setTotalAmount(new BigDecimal("600.0"));
        ticket.setMultipleDraws(2);

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, saleRespCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("11003", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_RAFFLE, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(2, respTicket.getMultipleDraws());
        assertEquals(6, respTicket.getTotalBets());
        assertEquals(0, respTicket.getEntries().size());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(saleRespCtx.getTransactionID());
        expectTrans.setGameId("RA-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(saleReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(saleReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        expectTrans.setType(saleReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<RaffleTicket> dbTickets = this.getBaseTicketDao().findBySerialNo(RaffleTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbTickets.size());

        RaffleTicket expTicket = new RaffleTicket();
        expTicket.setSerialNo(respTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setDevId(saleReqCtx.getTerminalId());
        expTicket.setOperatorId(saleReqCtx.getOperatorId());
        expTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expTicket.setMerchantId(expectTrans.getMerchantId());
        expTicket.setTotalAmount(SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                new BigDecimal(ticket.getMultipleDraws())));
        expTicket.setMultipleDraws(ticket.getMultipleDraws());
        expTicket.setMobile(ticket.getUser() != null ? ticket.getUser().getMobile() : null);
        expTicket.setCreditCardSN(ticket.getUser() != null ? ticket.getUser().getCreditCardSN() : null);
        expTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expTicket.setTotalBets(3);
        expTicket.setBarcode(respTicket.getBarcode());
        expTicket.setValidationCode(respTicket.getValidationCode());
        RaffleGameInstance gameInstance = new RaffleGameInstance();
        gameInstance.setId("GII-112");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));
    }

    @Rollback(true)
    @Test
    public void testSale_UnmatchedTotalAmount() throws Exception {
        this.printMethod();
        RaffleTicket ticket = RaffleDomainMocker.ticket();
        ticket.setTotalAmount(new BigDecimal("500.0"));
        ticket.setMultipleDraws(2);

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, saleRespCtx.getResponseCode());
    }

    @Rollback(true)
    @Test
    public void testSale_ExceedMaxAllowedMultiDraw() throws Exception {
        this.printMethod();
        RaffleTicket ticket = RaffleDomainMocker.ticket();
        ticket.setTotalAmount(new BigDecimal("600.0"));
        ticket.setMultipleDraws(2);

        this.jdbcTemplate.update("update RA_OPERATION_PARAMETERS set MAX_MULTI_DRAW=1");

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, saleRespCtx.getResponseCode());
    }

    @Test
    public void testSale_GameInstanceSuspendSale() throws Exception {
        this.printMethod();
        RaffleTicket ticket = RaffleDomainMocker.ticket();

        this.jdbcTemplate.update("update RA_GAME_INSTANCE set IS_SUSPEND_SALE=1");

        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleReqCtx.setGameTypeId(Game.TYPE_RAFFLE + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        RaffleTicket respTicket = (RaffleTicket) saleRespCtx.getModel();
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, saleRespCtx.getResponseCode());
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
