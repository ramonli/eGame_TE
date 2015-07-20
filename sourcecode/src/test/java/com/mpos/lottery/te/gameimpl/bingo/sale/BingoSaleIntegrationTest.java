package com.mpos.lottery.te.gameimpl.bingo.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.BingoDomainMocker;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoEntryRefDao;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoTicketRefDao;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class BingoSaleIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;
    @Resource(name = "bingoTicketRefDao")
    private BingoTicketRefDao ticketRefDao;
    @Resource(name = "bingoEntryRefDao")
    private BingoEntryRefDao bingoEntryRefDao;
    @Resource(name = "baseGameInstanceDao")
    private BaseGameInstanceDao gameInstanceDao;

    @Test
    public void testSell_SingleDraw_PlayerPickNumber_OK() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        BingoTicket ticketDto = (BingoTicket) respCtx.getModel();
        // this.setComplete();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals("11002", ticketDto.getLastDrawNo());
        assertEquals(new Barcoder(GameType.BINGO.getType(), ticketDto.getRawSerialNo()).getBarcode(),
                ticketDto.getBarcode());
        assertNotNull(ticketDto.getValidationCode());
        assertEquals(clientTicket.getTotalAmount().doubleValue(), ticketDto.getTotalAmount().doubleValue(), 0);
        assertEquals("01110394646910962353", ticketDto.getRawSerialNo());
        assertEquals(1, ticketDto.getMultipleDraws());
        assertEquals(3, ticketDto.getTotalBets());
        assertEquals(3, ticketDto.getEntries().size());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(clientTicket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(clientTicket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(clientTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<BingoTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(BingoTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(1, hostTickets.size());
        BingoTicket expectTicket = new BingoTicket();
        expectTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(clientTicket.getTotalAmount(),
                new BigDecimal(clientTicket.getMultipleDraws())));
        expectTicket.setMultipleDraws(clientTicket.getMultipleDraws());
        expectTicket.setMobile(clientTicket.getUser() != null ? clientTicket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(clientTicket.getUser() != null ? clientTicket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
        expectTicket.setTotalBets(3);
        expectTicket.setValidationCode(ticketDto.getValidationCode());
        expectTicket.setBarcode(ticketDto.getBarcode());
        BingoGameInstance gameInstance = new BingoGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertNotNull(hostTickets.get(0).getImportedSerialNo());
        // assert bingo specific fields
        assertEquals(ticketDto.getRawSerialNo(), ticketDto.getImportedSerialNo());
        BingoTicketRef ticketRef = this.getTicketRefDao().findBySerialNo(ticketDto.getRawSerialNo());
        assertEquals(ticketRef.getLuckySerial(), hostTickets.get(0).getLuckySerial());
        assertEquals(ticketRef.getBookNo(), hostTickets.get(0).getBookNo());

        // assert entries
        List<BingoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(BingoEntry.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        this.sortTicketEntries(dbEntries);
        assertEquals(3, dbEntries.size());
        for (BingoEntry entry : dbEntries) {
            assertEquals(100.0, entry.getEntryAmount().doubleValue(), 0);
            assertEquals(1, entry.getTotalBets());
            assertEquals(BaseEntry.BETOPTION_SINGLE, entry.getBetOption());
            assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, entry.getInputChannel());
            assertNull(entry.getEntryRefId());
            assertEquals(hostTickets.get(0).getId(), entry.getTicketId());
            assertEquals(hostTickets.get(0).getGameInstance().getId(), entry.getGameInstanceId());
        }
        assertEquals(clientTicket.getEntries().get(0).getSelectNumber(), dbEntries.get(0).getSelectNumber());
        assertEquals(clientTicket.getEntries().get(1).getSelectNumber(), dbEntries.get(1).getSelectNumber());
        assertEquals(clientTicket.getEntries().get(2).getSelectNumber(), dbEntries.get(2).getSelectNumber());

        // assert referenced ticket
        assertEquals(BingoTicketRef.STATUS_SOLD, ticketRef.getStatus());

        // assert game instance
        BingoGameInstance hostGameInstance = this.getGameInstanceDao().lookupByGameAndNumber(dbTrans.getMerchantId(),
                BingoGameInstance.class, clientTicket.getGameInstance().getGameId(),
                clientTicket.getGameInstance().getNumber());
        assertEquals(4, hostGameInstance.getCurrentSequence());
    }

    /**
     * Player doesn't pick any numbers, system will pick for him/her.
     */
    @Test
    public void testSell_SingleDraw_PlayerNoPickNumber_OK() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();
        clientTicket.getEntries().clear();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        BingoTicket ticketDto = (BingoTicket) respCtx.getModel();
        // this.setComplete();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals("11002", ticketDto.getLastDrawNo());
        assertEquals(new Barcoder(GameType.BINGO.getType(), ticketDto.getRawSerialNo()).getBarcode(),
                ticketDto.getBarcode());
        assertNotNull(ticketDto.getValidationCode());
        assertEquals(1, ticketDto.getMultipleDraws());
        assertEquals(3, ticketDto.getTotalBets());
        assertEquals(3, ticketDto.getEntries().size());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(clientTicket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(clientTicket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(clientTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<BingoTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(BingoTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(1, hostTickets.size());
        BingoTicket expectTicket = new BingoTicket();
        expectTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(clientTicket.getTotalAmount(),
                new BigDecimal(clientTicket.getMultipleDraws())));
        expectTicket.setMultipleDraws(clientTicket.getMultipleDraws());
        expectTicket.setMobile(clientTicket.getUser() != null ? clientTicket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(clientTicket.getUser() != null ? clientTicket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
        expectTicket.setTotalBets(3);
        expectTicket.setValidationCode(ticketDto.getValidationCode());
        expectTicket.setBarcode(ticketDto.getBarcode());
        BingoGameInstance gameInstance = new BingoGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertNotNull(hostTickets.get(0).getImportedSerialNo());
        // assert bingo specific fields
        assertEquals(ticketDto.getRawSerialNo(), ticketDto.getImportedSerialNo());
        BingoTicketRef ticketRef = this.getTicketRefDao().findBySerialNo(ticketDto.getRawSerialNo());
        assertEquals(ticketRef.getLuckySerial(), hostTickets.get(0).getLuckySerial());
        assertEquals(ticketRef.getBookNo(), hostTickets.get(0).getBookNo());

        // assert entries
        List<BingoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(BingoEntry.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        this.sortTicketEntries(dbEntries);
        assertEquals(3, dbEntries.size());
        for (BingoEntry entry : dbEntries) {
            assertEquals(100.0, entry.getEntryAmount().doubleValue(), 0);
            assertEquals(1, entry.getTotalBets());
            assertEquals(BaseEntry.BETOPTION_SINGLE, entry.getBetOption());
            assertEquals(BaseEntry.INPUT_CHANNEL_QP_NOTOMR, entry.getInputChannel());
            assertNotNull(entry.getSelectNumber());

            // assert referenced entry
            BingoEntryRef entryRef = this.getBaseJpaDao().findById(BingoEntryRef.class, entry.getEntryRefId());
            assertEquals(entryRef.getSelectedNumber(), entry.getSelectNumber());
            assertEquals(BingoTicketRef.STATUS_SOLD, entryRef.getStatus());
        }

        // assert referenced ticket
        assertEquals(BingoTicketRef.STATUS_SOLD, ticketRef.getStatus());
    }

    @Test
    public void testSell_SingleDraw_Multiple_OK() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16");
        clientTicket.getEntries().get(0).setBetOption(BaseEntry.BETOPTION_MULTIPLE);
        clientTicket.setTotalAmount(new BigDecimal("1800.0"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        BingoTicket ticketDto = (BingoTicket) respCtx.getModel();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals("11002", ticketDto.getLastDrawNo());
        assertEquals(new Barcoder(GameType.BINGO.getType(), ticketDto.getRawSerialNo()).getBarcode(),
                ticketDto.getBarcode());
        assertNotNull(ticketDto.getValidationCode());
        assertEquals(1, ticketDto.getMultipleDraws());
        assertEquals(18, ticketDto.getTotalBets());
        assertEquals(3, ticketDto.getEntries().size());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(clientTicket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(clientTicket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(clientTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(BingoTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(1, hostTickets.size());
        BingoTicket expectTicket = new BingoTicket();
        expectTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(clientTicket.getTotalAmount(),
                new BigDecimal(clientTicket.getMultipleDraws())));
        expectTicket.setMultipleDraws(clientTicket.getMultipleDraws());
        expectTicket.setMobile(clientTicket.getUser() != null ? clientTicket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(clientTicket.getUser() != null ? clientTicket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
        expectTicket.setTotalBets(18);
        expectTicket.setValidationCode(ticketDto.getValidationCode());
        expectTicket.setBarcode(ticketDto.getBarcode());
        BingoGameInstance gameInstance = new BingoGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));

        // assert entries
        List<BingoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(BingoEntry.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        this.sortTicketEntries(dbEntries);
        assertEquals(3, dbEntries.size());
        for (BingoEntry entry : dbEntries) {
            // assertEquals(BaseEntry.BETOPTION_SINGLE, entry.getBetOption());
            assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, entry.getInputChannel());
        }
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, dbEntries.get(0).getBetOption());
        assertEquals(BaseEntry.BETOPTION_SINGLE, dbEntries.get(1).getBetOption());
        assertEquals(BaseEntry.BETOPTION_SINGLE, dbEntries.get(2).getBetOption());
        assertEquals(16, dbEntries.get(0).getTotalBets());
        assertEquals(1, dbEntries.get(1).getTotalBets());
        assertEquals(1, dbEntries.get(2).getTotalBets());
        assertEquals(1600.0, dbEntries.get(0).getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, dbEntries.get(1).getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, dbEntries.get(2).getEntryAmount().doubleValue(), 0);
        assertEquals(clientTicket.getEntries().get(0).getSelectNumber(), dbEntries.get(0).getSelectNumber());
        assertEquals(clientTicket.getEntries().get(1).getSelectNumber(), dbEntries.get(1).getSelectNumber());
        assertEquals(clientTicket.getEntries().get(2).getSelectNumber(), dbEntries.get(2).getSelectNumber());
    }

    @Test
    public void testSell_MultiDraw_Multiple_OK() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16");
        clientTicket.getEntries().get(0).setBetOption(BaseEntry.BETOPTION_MULTIPLE);
        clientTicket.setMultipleDraws(3);
        clientTicket.setTotalAmount(new BigDecimal("5400.0"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        BingoTicket ticketDto = (BingoTicket) respCtx.getModel();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals("11004", ticketDto.getLastDrawNo());
        assertEquals(new Barcoder(GameType.BINGO.getType(), ticketDto.getRawSerialNo()).getBarcode(),
                ticketDto.getBarcode());
        assertNotNull(ticketDto.getValidationCode());
        assertEquals(3, ticketDto.getMultipleDraws());
        assertEquals(54, ticketDto.getTotalBets());
        assertEquals(3, ticketDto.getEntries().size());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(clientTicket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(clientTicket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(clientTicket.getTotalAmount());
        expectTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(BingoTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(3, hostTickets.size());
        BingoTicket expectTicket = new BingoTicket();
        expectTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(clientTicket.getTotalAmount(),
                new BigDecimal(clientTicket.getMultipleDraws())));
        expectTicket.setMultipleDraws(clientTicket.getMultipleDraws());
        expectTicket.setMobile(clientTicket.getUser() != null ? clientTicket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(clientTicket.getUser() != null ? clientTicket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
        expectTicket.setTotalBets(18);
        expectTicket.setValidationCode(ticketDto.getValidationCode());
        expectTicket.setBarcode(ticketDto.getBarcode());
        BingoGameInstance gameInstance = new BingoGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        // assert 1st ticket
        this.assertTicket(expectTicket, hostTickets.get(0));
        expectTicket.getGameInstance().setId("GII-113");
        expectTicket.setMultipleDraws(0);
        // assert 2nd ticket
        this.assertTicket(expectTicket, hostTickets.get(1));
        expectTicket.getGameInstance().setId("GII-114");
        expectTicket.setMultipleDraws(0);
        // assert 3rd ticket
        this.assertTicket(expectTicket, hostTickets.get(2));

        // assert entries
        List<BingoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(BingoEntry.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        this.sortTicketEntries(dbEntries);
        assertEquals(3, dbEntries.size());
        for (BingoEntry entry : dbEntries) {
            // assertEquals(BaseEntry.BETOPTION_SINGLE, entry.getBetOption());
            assertEquals(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR, entry.getInputChannel());
        }
        assertEquals(BaseEntry.BETOPTION_MULTIPLE, dbEntries.get(0).getBetOption());
        assertEquals(BaseEntry.BETOPTION_SINGLE, dbEntries.get(1).getBetOption());
        assertEquals(BaseEntry.BETOPTION_SINGLE, dbEntries.get(2).getBetOption());
        assertEquals(16, dbEntries.get(0).getTotalBets());
        assertEquals(1, dbEntries.get(1).getTotalBets());
        assertEquals(1, dbEntries.get(2).getTotalBets());
        assertEquals(1600.0, dbEntries.get(0).getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, dbEntries.get(1).getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, dbEntries.get(2).getEntryAmount().doubleValue(), 0);
        assertEquals(clientTicket.getEntries().get(0).getSelectNumber(), dbEntries.get(0).getSelectNumber());
        assertEquals(clientTicket.getEntries().get(1).getSelectNumber(), dbEntries.get(1).getSelectNumber());
        assertEquals(clientTicket.getEntries().get(2).getSelectNumber(), dbEntries.get(2).getSelectNumber());
    }

    @Test
    public void testSell_SingleDraw_MultipleNotAllowed() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16");
        clientTicket.getEntries().get(0).setBetOption(BaseEntry.BETOPTION_MULTIPLE);
        clientTicket.setTotalAmount(new BigDecimal("1800.0"));

        this.jdbcTemplate.update("update BG_OPERATION_PARAMETERS set multiple=0");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        BingoTicket ticketDto = (BingoTicket) respCtx.getModel();

        assertEquals(SystemException.CODE_UNSUPPORTED_BETOPTION, respCtx.getResponseCode());
    }

    @Test
    public void testSell_SingleDraw_ExceedMaxAllowedMultiDraw() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16");
        clientTicket.getEntries().get(0).setBetOption(BaseEntry.BETOPTION_MULTIPLE);
        clientTicket.setMultipleDraws(3);
        clientTicket.setTotalAmount(new BigDecimal("5400.0"));

        this.jdbcTemplate.update("update BG_OPERATION_PARAMETERS set MIN_MULTI_DRAW=1,MAX_MULTI_DRAW=2");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.BINGO.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        BingoTicket ticketDto = (BingoTicket) respCtx.getModel();

        assertEquals(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, respCtx.getResponseCode());
    }

    @Test
    public void testSell_GameInstanceSuspendSale() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16");
        clientTicket.getEntries().get(0).setBetOption(BaseEntry.BETOPTION_MULTIPLE);
        clientTicket.setMultipleDraws(3);
        clientTicket.setTotalAmount(new BigDecimal("5400.0"));

        this.jdbcTemplate.update("update BG_GAME_INSTANCE set IS_SUSPEND_SALE=1");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        ctx.setGameTypeId(GameType.BINGO.getType() + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, respCtx.getResponseCode());
    }

    @Test
    public void testSell_InactiveGame() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16");
        clientTicket.getEntries().get(0).setBetOption(BaseEntry.BETOPTION_MULTIPLE);
        clientTicket.setMultipleDraws(3);
        clientTicket.setTotalAmount(new BigDecimal("5400.0"));

        this.jdbcTemplate.update("update Game set status=" + Game.STATUS_INACTIVE);

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        ctx.setGameTypeId(GameType.BINGO.getType() + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_GAME_INACTIVE, respCtx.getResponseCode());
    }

    /**
     * The count of entries is unmatched with pre-configured count in <code>BingoFunType</code>
     */
    @Test
    public void testSell_UnmatchedEntries() throws Exception {
        printMethod();
        BingoTicket clientTicket = BingoDomainMocker.mockTicket();
        BingoEntry entry = new BingoEntry();
        entry.setSelectNumber("10,20,30,40,5,6,7,8,9,10,11,12,13,14,15");
        entry.setBetOption(BingoEntry.BETOPTION_SINGLE);
        entry.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_NOTOMR);
        clientTicket.getEntries().add(entry);

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        ctx.setGameTypeId(GameType.BINGO.getType() + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_UNMATCHED_ENTRY_COUNT, respCtx.getResponseCode());
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

    public BingoTicketRefDao getTicketRefDao() {
        return ticketRefDao;
    }

    public void setTicketRefDao(BingoTicketRefDao ticketRefDao) {
        this.ticketRefDao = ticketRefDao;
    }

    public BingoEntryRefDao getBingoEntryRefDao() {
        return bingoEntryRefDao;
    }

    public void setBingoEntryRefDao(BingoEntryRefDao bingoEntryRefDao) {
        this.bingoEntryRefDao = bingoEntryRefDao;
    }

    public BaseGameInstanceDao getGameInstanceDao() {
        return gameInstanceDao;
    }

    public void setGameInstanceDao(BaseGameInstanceDao gameInstanceDao) {
        this.gameInstanceDao = gameInstanceDao;
    }

}
