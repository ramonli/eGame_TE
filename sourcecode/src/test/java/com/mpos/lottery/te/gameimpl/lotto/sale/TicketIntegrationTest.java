package com.mpos.lottery.te.gameimpl.lotto.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.game.Game;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

public class TicketIntegrationTest extends BaseServletIntegrationTest {
    private Log logger = LogFactory.getLog(TicketIntegrationTest.class);
    @Resource(name = "merchantDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;

    @Rollback(true)
    @Test
    public void testSell_Single_OK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("800"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("20090408", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LOTT, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(8, respTicket.getTotalBets());
        assertEquals(2, respTicket.getEntries().size());

        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), respEntry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), respEntry0.getBetOption());
        assertEquals(ticket.getEntries().get(0).getInputChannel(), respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, ((LottoEntry) respEntry0).getBoostAmount().doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LottoTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        LottoTicket expectTicket = new LottoTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                new BigDecimal(ticket.getMultipleDraws())));
        expectTicket.setMultipleDraws(ticket.getMultipleDraws());
        expectTicket.setMobile(ticket.getUser() != null ? ticket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(ticket.getUser() != null ? ticket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expectTicket.setTotalBets(8);
        expectTicket.setValidationCode(respTicket.getValidationCode());
        expectTicket.setBarcode(respTicket.getBarcode());
        LottoGameInstance gameInstance = new LottoGameInstance();
        gameInstance.setId("GII-113");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertEquals("8416b3b4a2aa49c58cc60d706cfbf82b", ((LottoTicket) hostTickets.get(0)).getExtendText());

        // asert entries
        List<LottoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(LottoEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        LottoEntry dbEntry0 = dbEntries.get(0);
        assertEquals(100.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, dbEntry0.getBoostAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        assertEquals(1, dbEntry0.getMultipleCount());
        LottoEntry dbEntry1 = dbEntries.get(1);
        assertEquals(700.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(7, dbEntry1.getTotalBets());
        assertEquals(1, dbEntry1.getMultipleCount());
    }

    @Rollback(true)
    @Test
    public void testSell_Single_PlayerSpecifyEntryAmount_OK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        BaseEntry customAmountSingleEntry = (BaseEntry) ticket.getEntries().get(0).clone();
        customAmountSingleEntry.setEntryAmount(new BigDecimal("300.0"));
        ticket.getEntries().add(customAmountSingleEntry);
        BaseEntry customAmountMultiEntry = (BaseEntry) ticket.getEntries().get(1).clone();
        customAmountMultiEntry.setEntryAmount(new BigDecimal("1400.0"));
        ticket.getEntries().add(customAmountMultiEntry);
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("2500"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("20090408", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LOTT, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(25, respTicket.getTotalBets());
        assertEquals(4, respTicket.getEntries().size());

        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), respEntry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), respEntry0.getBetOption());
        assertEquals(ticket.getEntries().get(0).getInputChannel(), respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, ((LottoEntry) respEntry0).getBoostAmount().doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LottoTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        LottoTicket expectTicket = new LottoTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                new BigDecimal(ticket.getMultipleDraws())));
        expectTicket.setMultipleDraws(ticket.getMultipleDraws());
        expectTicket.setMobile(ticket.getUser() != null ? ticket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(ticket.getUser() != null ? ticket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expectTicket.setTotalBets(25);
        expectTicket.setValidationCode(respTicket.getValidationCode());
        expectTicket.setBarcode(respTicket.getBarcode());
        LottoGameInstance gameInstance = new LottoGameInstance();
        gameInstance.setId("GII-113");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertEquals("e21bd03ffb7b0507d5e4e35b9b336871", ((LottoTicket) hostTickets.get(0)).getExtendText());

        // asert entries
        List<LottoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(LottoEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(7, dbEntries.size());
        for (LottoEntry dbEntry : dbEntries) {
            logger.debug("entry.multipleCount=" + dbEntry.getMultipleCount());
            // assertTrue(dbEntry.getMultipleCount());
        }
        // hard to sort entries, can't consistently assert the db entries.
        // LottoEntry dbEntry0 = dbEntries.get(0);
        // assertEquals(100.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        // assertEquals(100.0, dbEntry0.getBoostAmount().doubleValue(), 0);
        // assertEquals(1, dbEntry0.getTotalBets());
        // LottoEntry dbEntry1 = dbEntries.get(1);
        // assertEquals(700.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        // assertEquals(7, dbEntry1.getTotalBets());
    }

    @Rollback(true)
    @Test
    public void testSell_Single_PlayerSpecifyEntryAmount_NotIntegerTimes_OK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        BaseEntry customAmountSingleEntry = (BaseEntry) ticket.getEntries().get(0).clone();
        customAmountSingleEntry.setEntryAmount(new BigDecimal("300.0"));
        ticket.getEntries().add(customAmountSingleEntry);
        BaseEntry customAmountMultiEntry = (BaseEntry) ticket.getEntries().get(1).clone();
        customAmountMultiEntry.setEntryAmount(new BigDecimal("1400.1"));
        ticket.getEntries().add(customAmountMultiEntry);
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("2500.1"));

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, respCtx.getResponseCode());
    }

    @Rollback(true)
    @Test
    public void testSell_Single_PlayerSpecifyEntryAmount_ExceedMaxMultipleCount_OK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        BaseEntry customAmountSingleEntry = (BaseEntry) ticket.getEntries().get(0).clone();
        customAmountSingleEntry.setEntryAmount(new BigDecimal("300.0"));
        ticket.getEntries().add(customAmountSingleEntry);
        BaseEntry customAmountMultiEntry = (BaseEntry) ticket.getEntries().get(1).clone();
        customAmountMultiEntry.setEntryAmount(new BigDecimal("1400.0"));
        ticket.getEntries().add(customAmountMultiEntry);
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("2500.0"));

        this.jdbcTemplate.update("update LOTTO_OPERATION_PARAMETERS l set l.MULTIPLE_COUNT=2");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, respCtx.getResponseCode());
    }

    @Rollback(true)
    @Test
    public void testSell_MultiDraw_OK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("20090416", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LOTT, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(2, respTicket.getMultipleDraws());
        assertEquals(16, respTicket.getTotalBets());
        assertEquals(2, respTicket.getEntries().size());

        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), respEntry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), respEntry0.getBetOption());
        assertEquals(ticket.getEntries().get(0).getInputChannel(), respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, ((LottoEntry) respEntry0).getBoostAmount().doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LottoTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        LottoTicket expectTicket = new LottoTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                new BigDecimal(ticket.getMultipleDraws())));
        expectTicket.setMultipleDraws(ticket.getMultipleDraws());
        expectTicket.setMobile(ticket.getUser() != null ? ticket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(ticket.getUser() != null ? ticket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expectTicket.setTotalBets(8);
        expectTicket.setValidationCode(respTicket.getValidationCode());
        expectTicket.setBarcode(respTicket.getBarcode());
        LottoGameInstance gameInstance = new LottoGameInstance();
        gameInstance.setId("GII-113");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertEquals("8416b3b4a2aa49c58cc60d706cfbf82b", ((LottoTicket) hostTickets.get(0)).getExtendText());
        expectTicket.setMultipleDraws(0);
        gameInstance.setId("GII-114");
        this.assertTicket(expectTicket, hostTickets.get(1));
        assertEquals("8416b3b4a2aa49c58cc60d706cfbf82b", ((LottoTicket) hostTickets.get(1)).getExtendText());

        // asert entries
        List<LottoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(LottoEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        LottoEntry dbEntry0 = dbEntries.get(0);
        assertEquals(100.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, dbEntry0.getBoostAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        LottoEntry dbEntry1 = dbEntries.get(1);
        assertEquals(700.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(7, dbEntry1.getTotalBets());
    }

    /**
     * No need to generate random validation code.
     */
    @Test
    public void testSell_Single_NoValidationCode_OK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update SYS_CONFIGURATION set IS_DISPLAY_VALIDATION_CODE=0");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals("FFFFFF", respTicket.getValidationCode());
    }

    @Test
    public void testSell_ROLL_OK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        List<BaseEntry> entries = new ArrayList<BaseEntry>(0);
        LottoEntry entry = new LottoEntry();
        entry.setBetOption(BaseEntry.BETOPTION_ROLL);
        entry.setSelectNumber("1,2,3,4,5");
        entries.add(entry);
        ticket.setEntries(entries);
        ticket.setTotalAmount(new BigDecimal("3100"));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
        // verify database output.
    }

    @Test
    public void testSell_Multiple_Max_Fail() throws Exception {
        this.jdbcTemplate.update("update merchant set MAX_MULTIPLE=10 where merchant_id=111");

        this.initializeMLotteryContext();
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        LottoEntry entry = (LottoEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8,9,11,12");
        entry.setBetOption(LottoEntry.BETOPTION_MULTIPLE);
        entry.setInputChannel(0);
        ticket.getEntries().clear();
        ticket.getEntries().add(entry);
        ticket.setTotalAmount(new BigDecimal("199"));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(348, respCtx.getResponseCode());
    }

    /**
     * Exceed the max alloed multi-draw of operation parameter
     */
    @Test
    public void testSell_Multiple_ExceedAllowedMaxDraw_1() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update LOTTO_OPERATION_PARAMETERS set MAX_MULTI_DRAW=1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, respCtx.getResponseCode());
    }

    /**
     * Exceed the max allowed multi-draw of merchant
     */
    @Test
    public void testSell_Multiple_ExceedAllowedMaxDraw_2() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update merchant set MULTI_DRAW=1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Multiple_Max_NoLimit() throws Exception {
        this.initializeMLotteryContext();
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        LottoEntry entry = (LottoEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8,9,11,12");
        entry.setBetOption(LottoEntry.BETOPTION_MULTIPLE);
        entry.setInputChannel(0);
        ticket.getEntries().clear();
        ticket.getEntries().add(entry);
        ticket.setTotalAmount(new BigDecimal("46200"));

        this.jdbcTemplate.update("update merchant set MAX_MULTIPLE=1000");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Multiple_Max_OK() throws Exception {
        this.initializeMLotteryContext();
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        LottoEntry entry = (LottoEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8,9,11");
        entry.setBetOption(LottoEntry.BETOPTION_MULTIPLE);
        entry.setInputChannel(0);
        ticket.getEntries().clear();
        ticket.getEntries().add(entry);
        ticket.setTotalAmount(new BigDecimal("21000"));

        this.jdbcTemplate.update("update merchant set MAX_MULTIPLE=1000");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
    }

    @Test
    public void testSell_GameInstanceSuspendSale() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update GAME_INSTANCE set IS_SUSPEND_SALE=1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, respCtx.getResponseCode());
    }

    @Test
    public void testSell_NoSaleAllowed() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update GAME_MERCHANT set ALLOWED_SELLING=0");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OPERATOR_SELL_NOPRIVILEDGE, respCtx.getResponseCode());
    }

    @Rollback(true)
    @Test
    public void testSell_Single_SaleOnNewGameInstance_Fail() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.getGameInstance().setNumber("20090416");

        this.jdbcTemplate.update("update LOTTO_OPERATION_PARAMETERS set ALLOW_SKIP_DRAW=0");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_NOT_ACTIVE_DRAW, respCtx.getResponseCode());
    }
    
    @Rollback(true)
    @Test
    public void testSell_Single_SaleOnNewGameInstance_OK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.getGameInstance().setNumber("20090416");

        this.jdbcTemplate.update("update LOTTO_OPERATION_PARAMETERS set ALLOW_SKIP_DRAW=1");

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("20090416", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LOTT, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(8, respTicket.getTotalBets());
        assertEquals(2, respTicket.getEntries().size());

        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), respEntry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), respEntry0.getBetOption());
        assertEquals(ticket.getEntries().get(0).getInputChannel(), respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, ((LottoEntry) respEntry0).getBoostAmount().doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LottoTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        LottoTicket expectTicket = new LottoTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                new BigDecimal(ticket.getMultipleDraws())));
        expectTicket.setMultipleDraws(ticket.getMultipleDraws());
        expectTicket.setMobile(ticket.getUser() != null ? ticket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(ticket.getUser() != null ? ticket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expectTicket.setTotalBets(8);
        expectTicket.setValidationCode(respTicket.getValidationCode());
        expectTicket.setBarcode(respTicket.getBarcode());
        LottoGameInstance gameInstance = new LottoGameInstance();
        gameInstance.setId("GII-114");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertEquals("8416b3b4a2aa49c58cc60d706cfbf82b", ((LottoTicket) hostTickets.get(0)).getExtendText());

        // asert entries
        List<LottoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(LottoEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        LottoEntry dbEntry0 = dbEntries.get(0);
        assertEquals(100.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, dbEntry0.getBoostAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        assertEquals(1, dbEntry0.getMultipleCount());
        LottoEntry dbEntry1 = dbEntries.get(1);
        assertEquals(700.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(7, dbEntry1.getTotalBets());
        assertEquals(1, dbEntry1.getMultipleCount());
    }

    @Rollback(true)
    @Test
    public void testSell_MultiDraw_SaleOnNewGameInstanceOK() throws Exception {
        printMethod();
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ticket.getGameInstance().setNumber("20090416");
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update LOTTO_OPERATION_PARAMETERS set ALLOW_SKIP_DRAW=1");

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LOTT + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LottoTicket respTicket = (LottoTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("20090508", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LOTT, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(2, respTicket.getMultipleDraws());
        assertEquals(16, respTicket.getTotalBets());
        assertEquals(2, respTicket.getEntries().size());

        this.sortTicketEntries(respTicket.getEntries());
        BaseEntry respEntry0 = respTicket.getEntries().get(0);
        assertEquals(ticket.getEntries().get(0).getSelectNumber(), respEntry0.getSelectNumber());
        assertEquals(ticket.getEntries().get(0).getBetOption(), respEntry0.getBetOption());
        assertEquals(ticket.getEntries().get(0).getInputChannel(), respEntry0.getInputChannel());
        assertEquals(100.0, respEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, ((LottoEntry) respEntry0).getBoostAmount().doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.subtract(ticket.getTotalAmount()).doubleValue(),
                newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(ticket.getGameInstance().getGameId());
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LottoTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        LottoTicket expectTicket = new LottoTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                new BigDecimal(ticket.getMultipleDraws())));
        expectTicket.setMultipleDraws(ticket.getMultipleDraws());
        expectTicket.setMobile(ticket.getUser() != null ? ticket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(ticket.getUser() != null ? ticket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(ticket.getPIN()));
        expectTicket.setTotalBets(8);
        expectTicket.setValidationCode(respTicket.getValidationCode());
        expectTicket.setBarcode(respTicket.getBarcode());
        LottoGameInstance gameInstance = new LottoGameInstance();
        gameInstance.setId("GII-114");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertEquals("8416b3b4a2aa49c58cc60d706cfbf82b", ((LottoTicket) hostTickets.get(0)).getExtendText());
        expectTicket.setMultipleDraws(0);
        gameInstance.setId("GII-115");
        this.assertTicket(expectTicket, hostTickets.get(1));
        assertEquals("8416b3b4a2aa49c58cc60d706cfbf82b", ((LottoTicket) hostTickets.get(1)).getExtendText());

        // asert entries
        List<LottoEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(LottoEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        LottoEntry dbEntry0 = dbEntries.get(0);
        assertEquals(100.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(100.0, dbEntry0.getBoostAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        LottoEntry dbEntry1 = dbEntries.get(1);
        assertEquals(700.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(7, dbEntry1.getTotalBets());
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
