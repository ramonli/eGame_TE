package com.mpos.lottery.te.gameimpl.magic100.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.Magic100DomainMocker;
import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberSequenceDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.RequeuedNumbersDao;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

public class Magic100SaleIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;
    @Resource(name = "payoutDao")
    private PayoutDao payoutDao;
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "jpaLuckyNumberSequenceDao")
    private LuckyNumberSequenceDao luckyNumberSequenceDao;
    @Resource(name = "requeuedNumbersDao")
    private RequeuedNumbersDao requeuedNumbersDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    @Test
    public void testSell_SingleNumber_NoWin_TaxWhenWinnerAnalysis() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("100"));
        ((Magic100Entry) ticket.getEntries().get(0)).setTotalBets(1);

        // old sale credit level
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket respTicket = (Magic100Ticket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(ticket.getGameInstance().getNumber(), respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LUCKYNUMBER, respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(1, respTicket.getTotalBets());
        assertEquals(1, respTicket.getEntries().size());

        // verify credit level
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newSaleCreditOperator.doubleValue(), 0);
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
        assertEquals(oldPayoutCreditOperator.doubleValue(), newPayoutCreditOperator.doubleValue(), 0);

        // verify transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(respCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(respCtx.getTerminalId());
        expectTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectTrans.setType(ctx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        Magic100Ticket expTicket = new Magic100Ticket();
        expTicket.setSerialNo(respTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setStatus(BaseTicket.STATUS_ACCEPTED);
        expTicket.setDevId(respCtx.getTerminalId());
        expTicket.setOperatorId(respCtx.getOperatorId());
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
        expTicket.setWinning(false);
        expTicket.setTotalBets(1);
        expTicket.setValidationCode(respTicket.getValidationCode());
        expTicket.setBarcode(respTicket.getBarcode());
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        gameInstance.setId("GII-111");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));

        // assert entry
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, dbEntries.size());
        this.sortEntries(dbEntries);

        assertEquals(7, dbEntries.get(0).getSequenceOfNumber());
        assertEquals("7", dbEntries.get(0).getSelectNumber());
        assertEquals(false, dbEntries.get(0).isWinning());
        assertEquals(0.0, dbEntries.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(0).getTaxAmount().doubleValue(), 0);

        // assert lucky number sequence
        LuckyNumberSequence dbLuckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());
        assertEquals(8, dbLuckyNumberSequence.getNextSequence());
        assertNull(dbLuckyNumberSequence.getLastestPlayer());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(respTicket.getSerialNo());
        assertEquals(0, dbPayouts.size());

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction("nonexist-id-0");
        assertEquals(1, requeuedNumbers.getCountOfValidNumbers());
        assertEquals(2, requeuedNumbers.getCountOfNumbers());
    }

    @Test
    public void testSell_1st_Win_TaxWhenWinnerAnalysis_NoAutoPayout() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        this.jdbcTemplate.update("update game set need_payout=0");

        // old sale credit level
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket respTicket = (Magic100Ticket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(ticket.getGameInstance().getNumber(), respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LUCKYNUMBER, respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(3, respTicket.getTotalBets());
        assertEquals(3, respTicket.getEntries().size());
        // assert entries
        List<BaseEntry> respEntries = respTicket.getEntries();
        assertEquals(3, respEntries.size());
        this.sortTicketEntries(respEntries);
        assertEquals("6", ((Magic100Entry) respEntries.get(0)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) respEntries.get(0)).isWinning());
        assertEquals(0.0, ((Magic100Entry) respEntries.get(0)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(0)).getTaxAmount().doubleValue(), 0);
        assertEquals("7", ((Magic100Entry) respEntries.get(1)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) respEntries.get(1)).isWinning());
        assertEquals(0.0, ((Magic100Entry) respEntries.get(1)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(1)).getTaxAmount().doubleValue(), 0);
        assertEquals("8", ((Magic100Entry) respEntries.get(2)).getSelectNumber());
        assertEquals(true, ((Magic100Entry) respEntries.get(2)).isWinning());
        assertEquals(1000.0, ((Magic100Entry) respEntries.get(2)).getPrizeAmount().doubleValue(), 0);
        assertEquals(10.0, ((Magic100Entry) respEntries.get(2)).getTaxAmount().doubleValue(), 0);

        // verify credit level
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newSaleCreditOperator.doubleValue(), 0);
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
        assertEquals(oldPayoutCreditOperator.doubleValue(), newPayoutCreditOperator.doubleValue(), 0);

        // verify transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(respCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(respCtx.getTerminalId());
        expectTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectTrans.setType(ctx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        Magic100Ticket expTicket = new Magic100Ticket();
        expTicket.setSerialNo(respTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setStatus(BaseTicket.STATUS_ACCEPTED);
        expTicket.setDevId(respCtx.getTerminalId());
        expTicket.setOperatorId(respCtx.getOperatorId());
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
        expTicket.setWinning(true);
        expTicket.setTotalBets(3);
        expTicket.setValidationCode(respTicket.getValidationCode());
        expTicket.setBarcode(respTicket.getBarcode());
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        gameInstance.setId("GII-111");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));
        assertTrue(dbTickets.get(0).isWinning());

        // assert entry
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(3, dbEntries.size());
        this.sortEntries(dbEntries);

        assertEquals(6, dbEntries.get(0).getSequenceOfNumber());
        assertEquals("6", dbEntries.get(0).getSelectNumber());
        assertEquals(false, dbEntries.get(0).isWinning());
        assertEquals(0.0, dbEntries.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(7, dbEntries.get(1).getSequenceOfNumber());
        assertEquals("7", dbEntries.get(1).getSelectNumber());
        assertEquals(false, dbEntries.get(1).isWinning());
        assertEquals(0.0, dbEntries.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(1).getTaxAmount().doubleValue(), 0);
        assertEquals(8, dbEntries.get(2).getSequenceOfNumber());
        assertEquals("8", dbEntries.get(2).getSelectNumber());
        assertEquals(true, dbEntries.get(2).isWinning());
        assertEquals(1000.0, dbEntries.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(10.0, dbEntries.get(2).getTaxAmount().doubleValue(), 0);

        // assert lucky number sequence
        LuckyNumberSequence dbLuckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());
        assertEquals(8, dbLuckyNumberSequence.getNextSequence());
        assertNull(dbLuckyNumberSequence.getLastestPlayer());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(respTicket.getSerialNo());
        assertEquals(0, dbPayouts.size());

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction("nonexist-id-1");
        assertEquals(2, requeuedNumbers.getCountOfValidNumbers());
        assertEquals(5, requeuedNumbers.getCountOfNumbers());
    }

    // @Rollback(false)
    @Test
    public void testSell_1st_Win_TaxWhenWinnerAnalysis() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        // old sale credit level
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket respTicket = (Magic100Ticket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(ticket.getGameInstance().getNumber(), respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LUCKYNUMBER, respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(3, respTicket.getTotalBets());
        assertEquals(3, respTicket.getEntries().size());
        // assert entries
        List<BaseEntry> respEntries = respTicket.getEntries();
        assertEquals(3, respEntries.size());
        this.sortTicketEntries(respEntries);
        assertEquals("6", ((Magic100Entry) respEntries.get(0)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) respEntries.get(0)).isWinning());
        assertEquals(0.0, ((Magic100Entry) respEntries.get(0)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(0)).getTaxAmount().doubleValue(), 0);
        assertEquals("7", ((Magic100Entry) respEntries.get(1)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) respEntries.get(1)).isWinning());
        assertEquals(0.0, ((Magic100Entry) respEntries.get(1)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(1)).getTaxAmount().doubleValue(), 0);
        assertEquals("8", ((Magic100Entry) respEntries.get(2)).getSelectNumber());
        assertEquals(true, ((Magic100Entry) respEntries.get(2)).isWinning());
        assertEquals(1000.0, ((Magic100Entry) respEntries.get(2)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(2)).getTaxAmount().doubleValue(), 0);

        // verify credit level
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newSaleCreditOperator.doubleValue(), 0);
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
        assertEquals(oldPayoutCreditOperator.add(new BigDecimal("1000.0")).doubleValue(),
                newPayoutCreditOperator.doubleValue(), 0);

        // verify transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(respCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(respCtx.getTerminalId());
        expectTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectTrans.setType(ctx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        Magic100Ticket expTicket = new Magic100Ticket();
        expTicket.setSerialNo(respTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setStatus(BaseTicket.STATUS_PAID);
        expTicket.setDevId(respCtx.getTerminalId());
        expTicket.setOperatorId(respCtx.getOperatorId());
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
        expTicket.setWinning(true);
        expTicket.setTotalBets(3);
        expTicket.setValidationCode(respTicket.getValidationCode());
        expTicket.setBarcode(respTicket.getBarcode());
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        gameInstance.setId("GII-111");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));
        assertTrue(dbTickets.get(0).isWinning());

        // assert entry
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(3, dbEntries.size());
        this.sortEntries(dbEntries);

        assertEquals(6, dbEntries.get(0).getSequenceOfNumber());
        assertEquals("6", dbEntries.get(0).getSelectNumber());
        assertEquals(false, dbEntries.get(0).isWinning());
        assertEquals(0.0, dbEntries.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(7, dbEntries.get(1).getSequenceOfNumber());
        assertEquals("7", dbEntries.get(1).getSelectNumber());
        assertEquals(false, dbEntries.get(1).isWinning());
        assertEquals(0.0, dbEntries.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(1).getTaxAmount().doubleValue(), 0);
        assertEquals(8, dbEntries.get(2).getSequenceOfNumber());
        assertEquals("8", dbEntries.get(2).getSelectNumber());
        assertEquals(true, dbEntries.get(2).isWinning());
        assertEquals(1000.0, dbEntries.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(2).getTaxAmount().doubleValue(), 0);

        // assert lucky number sequence
        LuckyNumberSequence dbLuckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());
        assertEquals(8, dbLuckyNumberSequence.getNextSequence());
        assertNull(dbLuckyNumberSequence.getLastestPlayer());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(respTicket.getSerialNo());
        assertEquals(1, dbPayouts.size());
        assertEquals(1000.0, dbPayouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        // tax amount is 0
        assertEquals(1000.0, dbPayouts.get(0).getTotalAmount().doubleValue(), 0);

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction("nonexist-id-1");
        assertEquals(2, requeuedNumbers.getCountOfValidNumbers());
        assertEquals(5, requeuedNumbers.getCountOfNumbers());
    }

    @Rollback(true)
    @Test
    public void testSell_1st_Win_TaxWhenPayout() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);

        // old sale credit level
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket respTicket = (Magic100Ticket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(ticket.getGameInstance().getNumber(), respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LUCKYNUMBER, respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(3, respTicket.getTotalBets());
        assertEquals(3, respTicket.getEntries().size());
        // assert entries
        List<BaseEntry> respEntries = respTicket.getEntries();
        assertEquals(3, respEntries.size());
        this.sortTicketEntries(respEntries);
        assertEquals("6", ((Magic100Entry) respEntries.get(0)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) respEntries.get(0)).isWinning());
        assertEquals(0.0, ((Magic100Entry) respEntries.get(0)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(0)).getTaxAmount().doubleValue(), 0);
        assertEquals("7", ((Magic100Entry) respEntries.get(1)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) respEntries.get(1)).isWinning());
        assertEquals(0.0, ((Magic100Entry) respEntries.get(1)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(1)).getTaxAmount().doubleValue(), 0);
        assertEquals("8", ((Magic100Entry) respEntries.get(2)).getSelectNumber());
        assertEquals(true, ((Magic100Entry) respEntries.get(2)).isWinning());
        assertEquals(1000.0, ((Magic100Entry) respEntries.get(2)).getPrizeAmount().doubleValue(), 0);
        assertEquals(166.67, ((Magic100Entry) respEntries.get(2)).getTaxAmount().doubleValue(), 0);

        // verify credit level
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newSaleCreditOperator.doubleValue(), 0);
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
        assertEquals(oldPayoutCreditOperator.add(new BigDecimal("833.33")).doubleValue(),
                newPayoutCreditOperator.doubleValue(), 0);

        // verify transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(respCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(respCtx.getTerminalId());
        expectTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectTrans.setType(ctx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        Magic100Ticket expTicket = new Magic100Ticket();
        expTicket.setSerialNo(respTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setStatus(BaseTicket.STATUS_PAID);
        expTicket.setDevId(respCtx.getTerminalId());
        expTicket.setOperatorId(respCtx.getOperatorId());
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
        expTicket.setWinning(true);
        expTicket.setTotalBets(3);
        expTicket.setValidationCode(respTicket.getValidationCode());
        expTicket.setBarcode(respTicket.getBarcode());
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        gameInstance.setId("GII-111");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));
        assertTrue(dbTickets.get(0).isWinning());

        // assert entry
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(3, dbEntries.size());
        this.sortEntries(dbEntries);
        assertEquals(6, dbEntries.get(0).getSequenceOfNumber());
        assertEquals("6", dbEntries.get(0).getSelectNumber());
        assertEquals(false, dbEntries.get(0).isWinning());
        assertEquals(0.0, dbEntries.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(7, dbEntries.get(1).getSequenceOfNumber());
        assertEquals("7", dbEntries.get(1).getSelectNumber());
        assertEquals(false, dbEntries.get(1).isWinning());
        assertEquals(0.0, dbEntries.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(1).getTaxAmount().doubleValue(), 0);
        assertEquals(8, dbEntries.get(2).getSequenceOfNumber());
        assertEquals("8", dbEntries.get(2).getSelectNumber());
        assertEquals(true, dbEntries.get(2).isWinning());
        assertEquals(1000.0, dbEntries.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(166.67, dbEntries.get(2).getTaxAmount().doubleValue(), 0);

        // assert lucky number sequence
        LuckyNumberSequence dbLuckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());
        assertEquals(8, dbLuckyNumberSequence.getNextSequence());
        assertNull(dbLuckyNumberSequence.getLastestPlayer());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(respTicket.getSerialNo());
        assertEquals(1, dbPayouts.size());
        assertEquals(1000.0, dbPayouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        // tax amount is 166.67
        assertEquals(833.33, dbPayouts.get(0).getTotalAmount().doubleValue(), 0);

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction("nonexist-id-1");
        assertEquals(2, requeuedNumbers.getCountOfValidNumbers());
        assertEquals(5, requeuedNumbers.getCountOfNumbers());

        // verify commission
        // sale commission(300*0.1) + payout commission(833.33*0.2)
        BigDecimal transComm = new BigDecimal("196.666");
        assertEquals(oldCommBalance.add(transComm).doubleValue(),
                this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance().doubleValue(), 0);
    }

    @Rollback(true)
    @Test
    public void testSell_Win_TaxWhenPayout_OperatorUseParent() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        this.jdbcTemplate.update("update operator set limit_type=" + Merchant.CREDIT_TYPE_USE_PARENT);
        this.jdbcTemplate.update("update game set TAX_CALCULATION_METHOD=" + Game.TAXMETHOD_PAYOUT);
        this.jdbcTemplate.update("update merchant set parent_id=1,is_distribute=1 where merchant_id=111");

        // old sale credit level
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldPayoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket respTicket = (Magic100Ticket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(ticket.getGameInstance().getNumber(), respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LUCKYNUMBER, respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(3, respTicket.getTotalBets());
        assertEquals(3, respTicket.getEntries().size());
        // assert entries
        List<BaseEntry> respEntries = respTicket.getEntries();
        assertEquals(3, respEntries.size());
        this.sortTicketEntries(respEntries);
        assertEquals("6", ((Magic100Entry) respEntries.get(0)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) respEntries.get(0)).isWinning());
        assertEquals(0.0, ((Magic100Entry) respEntries.get(0)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(0)).getTaxAmount().doubleValue(), 0);
        assertEquals("7", ((Magic100Entry) respEntries.get(1)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) respEntries.get(1)).isWinning());
        assertEquals(0.0, ((Magic100Entry) respEntries.get(1)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) respEntries.get(1)).getTaxAmount().doubleValue(), 0);
        assertEquals("8", ((Magic100Entry) respEntries.get(2)).getSelectNumber());
        assertEquals(true, ((Magic100Entry) respEntries.get(2)).isWinning());
        assertEquals(1000.0, ((Magic100Entry) respEntries.get(2)).getPrizeAmount().doubleValue(), 0);
        assertEquals(166.67, ((Magic100Entry) respEntries.get(2)).getTaxAmount().doubleValue(), 0);

        // verify credit level
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newPayoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        assertEquals(oldSaleCreditOperator.doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        assertEquals(oldSaleCreditMerchant.subtract(ticket.getTotalAmount()).doubleValue(),
                newSaleCreditMerchant.doubleValue(), 0);
        assertEquals(oldPayoutCreditOperator.doubleValue(), newPayoutCreditOperator.doubleValue(), 0);
        assertEquals(oldPayoutCreditMerchant.add(new BigDecimal("833.33")).doubleValue(),
                newPayoutCreditMerchant.doubleValue(), 0);

        // verify transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(respCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(respCtx.getTerminalId());
        expectTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectTrans.setType(ctx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        Magic100Ticket expTicket = new Magic100Ticket();
        expTicket.setSerialNo(respTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setStatus(BaseTicket.STATUS_PAID);
        expTicket.setDevId(respCtx.getTerminalId());
        expTicket.setOperatorId(respCtx.getOperatorId());
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
        expTicket.setWinning(true);
        expTicket.setTotalBets(3);
        expTicket.setValidationCode(respTicket.getValidationCode());
        expTicket.setBarcode(respTicket.getBarcode());
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        gameInstance.setId("GII-111");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));
        assertTrue(dbTickets.get(0).isWinning());

        // assert entry
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(3, dbEntries.size());
        this.sortEntries(dbEntries);
        assertEquals(6, dbEntries.get(0).getSequenceOfNumber());
        assertEquals("6", dbEntries.get(0).getSelectNumber());
        assertEquals(false, dbEntries.get(0).isWinning());
        assertEquals(0.0, dbEntries.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(7, dbEntries.get(1).getSequenceOfNumber());
        assertEquals("7", dbEntries.get(1).getSelectNumber());
        assertEquals(false, dbEntries.get(1).isWinning());
        assertEquals(0.0, dbEntries.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(1).getTaxAmount().doubleValue(), 0);
        assertEquals(8, dbEntries.get(2).getSequenceOfNumber());
        assertEquals("8", dbEntries.get(2).getSelectNumber());
        assertEquals(true, dbEntries.get(2).isWinning());
        assertEquals(1000.0, dbEntries.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(166.67, dbEntries.get(2).getTaxAmount().doubleValue(), 0);

        // assert lucky number sequence
        LuckyNumberSequence dbLuckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());
        assertEquals(8, dbLuckyNumberSequence.getNextSequence());
        assertNull(dbLuckyNumberSequence.getLastestPlayer());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(respTicket.getSerialNo());
        assertEquals(1, dbPayouts.size());
        assertEquals(1000.0, dbPayouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        // tax amount is 166.67
        assertEquals(833.33, dbPayouts.get(0).getTotalAmount().doubleValue(), 0);

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction("nonexist-id-1");
        assertEquals(2, requeuedNumbers.getCountOfValidNumbers());
        assertEquals(5, requeuedNumbers.getCountOfNumbers());

        // verify commission
        // sale commission(300*0.1) + payout commission(833.33*0.2)...as
        // operator's credit type is 'use parent', no commission balance will be
        // maintained.
        BigDecimal transComm = new BigDecimal("196.67");
        assertEquals(oldCommBalance.doubleValue(), this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance().doubleValue(), 0);

        // // assert commission logs
        // List<BalanceTransactions> balanceLogs = this.getBalanceTransactionsDao().find(
        // respCtx.getTransactionID());
        // this.sortBalanceTransactions(balanceLogs);
        // assertEquals(4, balanceLogs.size());
        // BalanceTransactions operatorBalanceLog = balanceLogs.get(0);
        // assertEquals(dbTrans.getOperatorId(), operatorBalanceLog.getOperatorId());
        // assertEquals(dbTrans.getMerchantId(), operatorBalanceLog.getMerchantId());
        // assertEquals(dbTrans.getDeviceId(), operatorBalanceLog.getDeviceId());
        // assertEquals(dbTrans.getOperatorId(), operatorBalanceLog.getOwnerId());
        // assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, operatorBalanceLog.getOwnerType());
        // assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY,
        // operatorBalanceLog.getPaymentType());
        // assertEquals(dbTrans.getType(), operatorBalanceLog.getTransactionType());
        // assertEquals(dbTrans.getType(), operatorBalanceLog.getOriginalTransType());
        // assertEquals()
        // assertEquals(dbTrans.getTotalAmount().doubleValue(),
        // operatorBalanceLog.getTransactionAmount()
        // .doubleValue(), 0);
    }

    @Test
    public void testSell_NoWin() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        this.jdbcTemplate.update("update LK_PRIZE_STATUS set NEXT_SEQ=4");
        this.jdbcTemplate.update("delete from LK_REQUEUE_NUMBERS");

        // old credit level
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket respTicket = (Magic100Ticket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals(ticket.getGameInstance().getNumber(), respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LUCKYNUMBER, respTicket.getRawSerialNo()).getBarcode(),
                respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(3, respTicket.getTotalBets());
        assertEquals(3, respTicket.getEntries().size());

        // verify credit level
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newSaleCreditOperator.doubleValue(), 0);
        assertEquals(oldPayoutCreditOperator.doubleValue(), newPayoutCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // verify transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(respCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(respCtx.getTerminalId());
        expectTrans.setTraceMessageId(respCtx.getTraceMessageId());
        expectTrans.setType(ctx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                respTicket.getSerialNo(), false);
        Magic100Ticket expTicket = new Magic100Ticket();
        expTicket.setSerialNo(respTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setStatus(BaseTicket.STATUS_ACCEPTED);
        expTicket.setDevId(respCtx.getTerminalId());
        expTicket.setOperatorId(respCtx.getOperatorId());
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
        expTicket.setValidationCode(respTicket.getValidationCode());
        expTicket.setBarcode(respTicket.getBarcode());
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        gameInstance.setId("GII-111");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));

        // assert entry
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                respTicket.getSerialNo(), false);
        assertEquals(3, dbEntries.size());
        this.sortEntries(dbEntries);
        assertEquals(4, dbEntries.get(0).getSequenceOfNumber());
        assertEquals("4", dbEntries.get(0).getSelectNumber());
        assertEquals(5, dbEntries.get(1).getSequenceOfNumber());
        assertEquals("5", dbEntries.get(1).getSelectNumber());
        assertEquals(6, dbEntries.get(2).getSequenceOfNumber());
        assertEquals("6", dbEntries.get(2).getSelectNumber());

        // assert lucky number sequence
        LuckyNumberSequence dbLuckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());
        assertEquals(7, dbLuckyNumberSequence.getNextSequence());
        assertEquals(ticket.getUser().getMobile(), dbLuckyNumberSequence.getLastestPlayer());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(respTicket.getSerialNo());
        assertEquals(0, dbPayouts.size());
    }

    @Test
    public void testSell_1st_Win_SuspendGameInstance() throws Exception {
        printMethod();

        this.jdbcTemplate.update("update LK_GAME_INSTANCE set IS_SUSPEND_PAYOUT=1");

        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket returnedTicket = (Magic100Ticket) respCtx.getModel();

        assertEquals(SystemException.CODE_NOT_ACTIVE_DRAW, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Win_NoGameDrawProvidedByClient() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        ticket.setGameInstance(null);

        this.jdbcTemplate.update("delete from LK_GAME_INSTANCE where ID='GII-112'");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket returnedTicket = (Magic100Ticket) respCtx.getModel();

        assertEquals(200, respCtx.getResponseCode());

    }

    @Test
    public void testSell_ErrorEntryFormat() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        ticket.getEntries().get(0).setSelectNumber("XX");
        ticket.setGameInstance(null);

        this.jdbcTemplate.update("delete from LK_GAME_INSTANCE where ID='GII-112'");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket returnedTicket = (Magic100Ticket) respCtx.getModel();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

    }

    @Test
    public void testSell_GameInstanceSuspendSale() throws Exception {
        printMethod();
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        ticket.setTotalAmount(new BigDecimal("100"));
        ((Magic100Entry) ticket.getEntries().get(0)).setTotalBets(1);

        this.jdbcTemplate.update("update LK_GAME_INSTANCE set IS_SUSPEND_SALE=1");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context respCtx = doPost(this.mockRequest(ctx));
        Magic100Ticket respTicket = (Magic100Ticket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, respCtx.getResponseCode());
    }

    // --------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------
    private void sortEntries(List<Magic100Entry> entries) {
        Collections.sort(entries, new Comparator<Magic100Entry>() {

            @Override
            public int compare(Magic100Entry o1, Magic100Entry o2) {
                return (int) (o1.getSequenceOfNumber() - o2.getSequenceOfNumber());
            }

        });
    }

    // --------------------------------------------------------------
    // SPRINT DEPENDENCIES INEJCTION
    // --------------------------------------------------------------
    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public LuckyNumberSequenceDao getLuckyNumberSequenceDao() {
        return luckyNumberSequenceDao;
    }

    public void setLuckyNumberSequenceDao(LuckyNumberSequenceDao luckyNumberSequenceDao) {
        this.luckyNumberSequenceDao = luckyNumberSequenceDao;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

    public RequeuedNumbersDao getRequeuedNumbersDao() {
        return requeuedNumbersDao;
    }

    public void setRequeuedNumbersDao(RequeuedNumbersDao requeuedNumbersDao) {
        this.requeuedNumbersDao = requeuedNumbersDao;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

}
