package com.mpos.lottery.te.gameimpl.bingo.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.BingoDomainMocker;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoEntryRefDao;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoTicketRefDao;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class BingoMannualCancelByTicketIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;
    @Resource(name = "bingoTicketRefDao")
    private BingoTicketRefDao ticketRefDao;
    @Resource(name = "bingoEntryRefDao")
    private BingoEntryRefDao EntryRefDao;

    @Test
    public void testCancelByTicket_SingleDraw_PlayerPickNumber_OK() throws Exception {
        printMethod();
        BingoTicket ticket = BingoDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        BingoTicket respTicket = (BingoTicket) saleRespCtx.getModel();
        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        BingoTicket cancelTicket = new BingoTicket();
        cancelTicket.setRawSerialNo(respTicket.getRawSerialNo());
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getTransactionDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert cancellation transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(cancelRespCtx.getTransactionID());
        expectedTrans.setDeviceId(cancelCtx.getTerminalId());
        expectedTrans.setOperatorId(cancelCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setType(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType());
        expectedTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectedTrans.setGameId("BINGO-1");
        expectedTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(respTicket.getRawSerialNo()));
        expectedTrans.setTotalAmount(ticket.getTotalAmount());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectedTrans,
                this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID()));

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(BingoTicket.class,
                BaseTicket.encryptSerialNo(respTicket.getRawSerialNo()), false);
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), hostTickets.get(0).getTransType());

        // assert referenced ticket
        BingoTicketRef ticketRef = this.getTicketRefDao().findBySerialNo(respTicket.getImportedSerialNo());
        assertEquals(BingoTicketRef.STATUS_CANCEL, ticketRef.getStatus());
    }

    @Test
    public void testCancel_SingleDraw_SystemPickNumber_OK() throws Exception {
        printMethod();
        BingoTicket ticket = BingoDomainMocker.mockTicket();
        ticket.getEntries().clear();

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        BingoTicket respTicket = (BingoTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        BingoTicket cancelTicket = new BingoTicket();
        cancelTicket.setRawSerialNo(respTicket.getRawSerialNo());
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getTransactionDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(cancelRespCtx.getTransactionID());
        expectedTrans.setDeviceId(cancelCtx.getTerminalId());
        expectedTrans.setOperatorId(cancelCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setType(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType());
        expectedTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectedTrans.setGameId("BINGO-1");
        expectedTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(respTicket.getRawSerialNo()));
        expectedTrans.setTotalAmount(ticket.getTotalAmount());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectedTrans,
                this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID()));

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(BingoTicket.class,
                BaseTicket.encryptSerialNo(respTicket.getRawSerialNo()), false);
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), hostTickets.get(0).getTransType());

        // assert referenced ticket
        BingoTicketRef ticketRef = this.getTicketRefDao().findBySerialNo(respTicket.getImportedSerialNo());
        assertEquals(BingoTicketRef.STATUS_CANCEL, ticketRef.getStatus());

        // assert referenced entry
        List<BingoEntry> hostEntries = this.getBaseEntryDao().findByTicketSerialNo(BingoEntry.class,
                respTicket.getSerialNo(), false);
        for (BingoEntry hostEntry : hostEntries) {
            BingoEntryRef entryRef = this.getEntryRefDao().findById(BingoEntryRef.class, hostEntry.getEntryRefId());
            assertEquals(BingoTicketRef.STATUS_CANCEL, entryRef.getStatus());
        }
    }

    /**
     * disable the allow-cancellation of game
     */
    @Test
    public void testCancel_Game_Disable() throws Exception {
        printMethod();
        BingoTicket ticket = BingoDomainMocker.mockTicket();

        this.jdbcTemplate.update("update BG_OPERATION_PARAMETERS set ALLOW_CANCELLATION=0");

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        BingoTicket ticketDto = (BingoTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        BingoTicket cancelTicket = new BingoTicket();
        cancelTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_MANUAL_CANCEL_DISABLED, cancelRespCtx.getResponseCode());
    }

    /**
     * disable the allow-cancellation of a game instance
     */
    @Test
    public void testCancel_GameInstance_Disable() throws Exception {
        printMethod();
        BingoTicket ticket = BingoDomainMocker.mockTicket();

        this.jdbcTemplate.update("update BG_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=1");

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        BingoTicket ticketDto = (BingoTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        BingoTicket cancelTicket = new BingoTicket();
        cancelTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(SystemException.CODE_MANUAL_CANCEL_DISABLED, cancelRespCtx.getResponseCode());
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
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

    public BingoTicketRefDao getTicketRefDao() {
        return ticketRefDao;
    }

    public void setTicketRefDao(BingoTicketRefDao ticketRefDao) {
        this.ticketRefDao = ticketRefDao;
    }

    public BingoEntryRefDao getEntryRefDao() {
        return EntryRefDao;
    }

    public void setEntryRefDao(BingoEntryRefDao entryRefDao) {
        EntryRefDao = entryRefDao;
    }

}
