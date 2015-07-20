package com.mpos.lottery.te.gameimpl.union.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.union.UnionDomainMocker;
import com.mpos.lottery.te.gameimpl.union.game.UnionGameInstance;
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

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

public class UnionSaleIntegrationTest extends BaseServletIntegrationTest {
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
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("800"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        UnionTicket respTicket = (UnionTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("20090408", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_UNION, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
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
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(UnionTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        UnionTicket expectTicket = new UnionTicket();
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
        UnionGameInstance gameInstance = new UnionGameInstance();
        gameInstance.setId("GII-113");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertEquals("0aac71d5ebef192ada11751c53b3851a", ((UnionTicket) hostTickets.get(0)).getExtendText());

        // asert entries
        List<UnionEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(UnionEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        UnionEntry dbEntry0 = dbEntries.get(0);
        assertEquals(100.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        UnionEntry dbEntry1 = dbEntries.get(1);
        assertEquals(700.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(7, dbEntry1.getTotalBets());
    }

    @Rollback(true)
    @Test
    public void testSell_MultiDraw_OK() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        UnionTicket respTicket = (UnionTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("20090416", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_UNION, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
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
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(UnionTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        UnionTicket expectTicket = new UnionTicket();
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
        UnionGameInstance gameInstance = new UnionGameInstance();
        gameInstance.setId("GII-113");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertEquals("0aac71d5ebef192ada11751c53b3851a", ((UnionTicket) hostTickets.get(0)).getExtendText());
        expectTicket.setMultipleDraws(0);
        gameInstance.setId("GII-114");
        this.assertTicket(expectTicket, hostTickets.get(1));
        assertEquals("0aac71d5ebef192ada11751c53b3851a", ((UnionTicket) hostTickets.get(1)).getExtendText());

        // asert entries
        List<UnionEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(UnionEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        UnionEntry dbEntry0 = dbEntries.get(0);
        assertEquals(100.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        UnionEntry dbEntry1 = dbEntries.get(1);
        assertEquals(700.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(7, dbEntry1.getTotalBets());
    }

    /**
     * No need to generate random validation code.
     */
    @Test
    public void testSell_Single_NoValidationCode_OK() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update SYS_CONFIGURATION set IS_DISPLAY_VALIDATION_CODE=0");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        UnionTicket respTicket = (UnionTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals("FFFFFF", respTicket.getValidationCode());
    }

    @Test
    public void testSell_ROLL_OK() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        List<BaseEntry> entries = new ArrayList<BaseEntry>(0);
        UnionEntry entry = new UnionEntry();
        entry.setBetOption(BaseEntry.BETOPTION_ROLL);
        entry.setSelectNumber("1,2,3,4,5");
        entries.add(entry);
        ticket.setEntries(entries);
        ticket.setTotalAmount(new BigDecimal("3100"));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_UNSUPPORTED_BETOPTION, respCtx.getResponseCode());
        // verify database output.
    }

    @Test
    public void testSell_Multiple_Max_Fail() throws Exception {
        this.jdbcTemplate.update("update merchant set MAX_MULTIPLE=10 where merchant_id=111");

        this.initializeMLotteryContext();
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        UnionEntry entry = (UnionEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8,9,11,12-2,8,7");
        entry.setBetOption(UnionEntry.BETOPTION_MULTIPLE);
        entry.setInputChannel(0);
        ticket.getEntries().clear();
        ticket.getEntries().add(entry);
        ticket.setTotalAmount(new BigDecimal("199"));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(348, respCtx.getResponseCode());
    }

    /**
     * Exceed the max alloed multi-draw of operation parameter
     */
    @Test
    public void testSell_Multiple_ExceedAllowedMaxDraw_1() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update UN_OPERATION_PARAMETERS set MAX_MULTI_DRAW=1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        UnionTicket respTicket = (UnionTicket) respCtx.getModel();

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
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update merchant set MULTI_DRAW=1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        UnionTicket respTicket = (UnionTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Multiple_Max_NoLimit() throws Exception {
        this.initializeMLotteryContext();
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        UnionEntry entry = (UnionEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8,9,11,12-2,7,8");
        entry.setBetOption(UnionEntry.BETOPTION_MULTIPLE);
        entry.setInputChannel(0);
        ticket.getEntries().clear();
        ticket.getEntries().add(entry);
        ticket.setTotalAmount(new BigDecimal("138600"));

        this.jdbcTemplate.update("update merchant set MAX_MULTIPLE=1500");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Multiple_Max_OK() throws Exception {
        this.initializeMLotteryContext();
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        UnionEntry entry = (UnionEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8,9,11-2,7,8");
        entry.setBetOption(UnionEntry.BETOPTION_MULTIPLE);
        entry.setInputChannel(0);
        ticket.getEntries().clear();
        ticket.getEntries().add(entry);
        ticket.setTotalAmount(new BigDecimal("63000"));

        this.jdbcTemplate.update("update merchant set MAX_MULTIPLE=1500");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(200, respCtx.getResponseCode());
    }

    @Test
    public void testSell_NOTripleDoubleSetting() throws Exception {
        this.initializeMLotteryContext();
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        UnionEntry entry = (UnionEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8,9,11-2,7,8");
        entry.setBetOption(UnionEntry.BETOPTION_MULTIPLE);
        entry.setInputChannel(0);
        ticket.getEntries().clear();
        ticket.getEntries().add(entry);
        ticket.setTotalAmount(new BigDecimal("63000"));

        this.jdbcTemplate.update("update merchant set MAX_MULTIPLE=1500");
        this.jdbcTemplate.update("delete from UN_C_OPERATION_PARAMETERS where ID in ('6','7')");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(500, respCtx.getResponseCode());
    }

    @Test
    public void testSell_UnmatchedTripleDoubleSetting() throws Exception {
        this.initializeMLotteryContext();
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        UnionEntry entry = (UnionEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8,9,11-2,7,8");
        entry.setBetOption(UnionEntry.BETOPTION_MULTIPLE);
        entry.setInputChannel(0);
        ticket.getEntries().clear();
        ticket.getEntries().add(entry);
        ticket.setTotalAmount(new BigDecimal("63000"));

        this.jdbcTemplate.update("update merchant set MAX_MULTIPLE=1500");
        this.jdbcTemplate.update("update UN_C_OPERATION_PARAMETERS set MAX_DOUBLE=2");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, respCtx.getResponseCode());
    }

    @Test
    public void testSell_GameInstanceSuspendSale() throws Exception {
        printMethod();
        UnionTicket ticket = UnionDomainMocker.mockTicket();
        ticket.setMultipleDraws(2);
        ticket.setTotalAmount(new BigDecimal("1600"));

        this.jdbcTemplate.update("update UN_GAME_INSTANCE set IS_SUSPEND_SALE=1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_UNION + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        UnionTicket respTicket = (UnionTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, respCtx.getResponseCode());
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
