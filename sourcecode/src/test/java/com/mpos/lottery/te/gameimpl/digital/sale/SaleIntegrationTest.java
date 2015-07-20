package com.mpos.lottery.te.gameimpl.digital.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.DigitalDomainMocker;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.gamespec.sale.web.QPEnquiryDto;
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

public class SaleIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;

    @Test
    public void testSell_Single_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.setMultipleDraws(1);
        clientTicket.setTotalAmount(new BigDecimal("200.0"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("11002", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_DIGITAL, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(clientTicket.getTotalAmount().doubleValue(), respTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(1, respTicket.getMultipleDraws());
        assertEquals(2, respTicket.getTotalBets());
        assertEquals(2, respTicket.getEntries().size());

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
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(1, hostTickets.size());
        DigitalTicket expectTicket = new DigitalTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(clientTicket.getTotalAmount(),
                new BigDecimal(clientTicket.getMultipleDraws())));
        expectTicket.setMultipleDraws(clientTicket.getMultipleDraws());
        expectTicket.setUserId(clientTicket.getUser() != null ? clientTicket.getUser().getId() : null);
        expectTicket.setMobile(clientTicket.getUser() != null ? clientTicket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(clientTicket.getUser() != null ? clientTicket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
        expectTicket.setTotalBets(2);
        expectTicket.setValidationCode(respTicket.getValidationCode());
        expectTicket.setBarcode(respTicket.getBarcode());
        DigitalGameInstance gameInstance = new DigitalGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));

        // asert entries
        List<DigitalEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(DigitalEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        DigitalEntry dbEntry0 = dbEntries.get(0);
        assertEquals(60.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        assertEquals("0,3,2,8", dbEntry0.getSelectNumber());

        DigitalEntry dbEntry1 = dbEntries.get(1);
        assertEquals(140.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry1.getTotalBets());
        // formatted selected number
        assertEquals("25", dbEntry1.getSelectNumber());
    }

    @Test
    public void testSell_MultiDraw_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("11003", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_DIGITAL, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(2, respTicket.getMultipleDraws());
        assertEquals(4, respTicket.getTotalBets());
        assertEquals(2, respTicket.getEntries().size());

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
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        DigitalTicket expectTicket = new DigitalTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(clientTicket.getTotalAmount(),
                new BigDecimal(clientTicket.getMultipleDraws())));
        expectTicket.setMultipleDraws(clientTicket.getMultipleDraws());
        expectTicket.setUserId(clientTicket.getUser() != null ? clientTicket.getUser().getId() : null);
        expectTicket.setMobile(clientTicket.getUser() != null ? clientTicket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(clientTicket.getUser() != null ? clientTicket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
        expectTicket.setTotalBets(2);
        expectTicket.setValidationCode(respTicket.getValidationCode());
        expectTicket.setBarcode(respTicket.getBarcode());
        DigitalGameInstance gameInstance = new DigitalGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        expectTicket.setMultipleDraws(0);
        gameInstance.setId("GII-113");
        this.assertTicket(expectTicket, hostTickets.get(1));

        // asert entries
        List<DigitalEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(DigitalEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        DigitalEntry dbEntry0 = dbEntries.get(0);
        assertEquals(60.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        assertEquals("0,3,2,8", dbEntry0.getSelectNumber());

        DigitalEntry dbEntry1 = dbEntries.get(1);
        assertEquals(140.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry1.getTotalBets());
        // formatted selected number
        assertEquals("25", dbEntry1.getSelectNumber());
    }

    @Test
    public void testSell_ExceedMaxAllowedMultiDraw() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();

        this.jdbcTemplate.update("update FD_OPERATION_PARAMETERS set MAX_MULTI_DRAW=1");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Single_QP_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber(null);
        clientTicket.getEntries().get(0).setInputChannel(BaseEntry.INPUT_CHANNEL_QP_OMR);
        clientTicket.getEntries().get(0).setCountOfQPNumber(4);

        this.jdbcTemplate.update("delete from SELECTED_NUMBER_STAT where ID like 'LFN-%'");

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(respTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, respTicket.getTicketType());
        assertEquals("11003", respTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_DIGITAL, respTicket.getRawSerialNo()).getBarcode(), respTicket.getBarcode());
        assertNotNull(respTicket.getValidationCode());
        assertEquals(2, respTicket.getMultipleDraws());
        assertEquals(4, respTicket.getTotalBets());
        assertEquals(2, respTicket.getEntries().size());

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
        expectTrans.setTicketSerialNo(respTicket.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(DigitalTicket.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, hostTickets.size());
        DigitalTicket expectTicket = new DigitalTicket();
        expectTicket.setSerialNo(respTicket.getSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setMerchantId(expectTrans.getMerchantId());
        expectTicket.setTotalAmount(SimpleToolkit.mathDivide(clientTicket.getTotalAmount(),
                new BigDecimal(clientTicket.getMultipleDraws())));
        expectTicket.setMultipleDraws(clientTicket.getMultipleDraws());
        expectTicket.setUserId(clientTicket.getUser() != null ? clientTicket.getUser().getId() : null);
        expectTicket.setMobile(clientTicket.getUser() != null ? clientTicket.getUser().getMobile() : null);
        expectTicket.setCreditCardSN(clientTicket.getUser() != null ? clientTicket.getUser().getCreditCardSN() : null);
        expectTicket.setTicketFrom(BaseTicket.TICKET_FROM_POS);
        expectTicket.setTicketType(BaseTicket.TICKET_TYPE_NORMAL);
        expectTicket.setTransType(TransactionType.SELL_TICKET.getRequestType());
        expectTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
        expectTicket.setTotalBets(2);
        expectTicket.setValidationCode(respTicket.getValidationCode());
        expectTicket.setBarcode(respTicket.getBarcode());
        DigitalGameInstance gameInstance = new DigitalGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        expectTicket.setMultipleDraws(0);
        gameInstance.setId("GII-113");
        this.assertTicket(expectTicket, hostTickets.get(1));

        // asert entries
        List<DigitalEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(DigitalEntry.class,
                respTicket.getSerialNo(), false);
        assertEquals(2, dbEntries.size());
        DigitalEntry dbEntry0 = dbEntries.get(0);
        assertEquals(60.0, dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        assertNotNull(dbEntry0.getSelectNumber());

        DigitalEntry dbEntry1 = dbEntries.get(1);
        assertEquals(140.0, dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry1.getTotalBets());
        // formatted selected number
        assertEquals("25", dbEntry1.getSelectNumber());
    }

    @Test
    public void testSell_AllBetOptions_OK() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().clear();
        // assemble entries
        DigitalEntry entry1 = new DigitalEntry();
        entry1.setSelectNumber("00,03,02,08");
        entry1.setBetOption(DigitalEntry.DIGITAL_BETOPTION_4D);
        entry1.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry1.setEntryAmount(new BigDecimal("100"));
        clientTicket.getEntries().add(entry1);

        DigitalEntry entry2 = new DigitalEntry();
        entry2.setSelectNumber("9,03,00");
        entry2.setBetOption(DigitalEntry.DIGITAL_BETOPTION_3D);
        entry2.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry2.setEntryAmount(new BigDecimal("100"));
        clientTicket.getEntries().add(entry2);

        DigitalEntry entry3 = new DigitalEntry();
        entry3.setSelectNumber("9,03");
        entry3.setBetOption(DigitalEntry.DIGITAL_BETOPTION_2D);
        entry3.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry3.setEntryAmount(new BigDecimal("90"));
        clientTicket.getEntries().add(entry3);

        DigitalEntry entry4 = new DigitalEntry();
        entry4.setSelectNumber("09");
        entry4.setBetOption(DigitalEntry.DIGITAL_BETOPTION_1D);
        entry4.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry4.setEntryAmount(new BigDecimal("110"));
        clientTicket.getEntries().add(entry4);

        DigitalEntry entry5 = new DigitalEntry();
        entry5.setSelectNumber("EVEN");
        entry5.setBetOption(DigitalEntry.DIGITAL_BETOPTION_EVEN);
        entry5.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry5.setEntryAmount(new BigDecimal("80"));
        clientTicket.getEntries().add(entry5);

        DigitalEntry entry6 = new DigitalEntry();
        entry6.setSelectNumber("ODD");
        entry6.setBetOption(DigitalEntry.DIGITAL_BETOPTION_ODD);
        entry6.setInputChannel(BaseEntry.INPUT_CHANNEL_NOTQP_OMR);
        entry6.setEntryAmount(new BigDecimal("120"));
        clientTicket.getEntries().add(entry6);

        clientTicket.setTotalAmount(new BigDecimal("1200"));

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        assertEquals(200, respCtx.getResponseCode());
    }

    @Test
    public void testEnquiryQP() throws Exception {
        printMethod();
        QPEnquiryDto dto = new QPEnquiryDto();
        dto.setCountOfEntries(3);
        dto.setCountOfNumbers(4);
        dto.setGameInstance(DigitalDomainMocker.mockGameInstance());

        Context ctx = this.getDefaultContext(TransactionType.ENQUIRY_QP_NUMBERS.getRequestType(), dto);
        ctx.setGameTypeId(Game.TYPE_DIGITAL + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        QPEnquiryDto respDto = (QPEnquiryDto) respCtx.getModel();
        assertEquals(3, respDto.getEntries().size());
    }

    // ----------------------------------------------------------------------
    // Exception Test Cases
    // ----------------------------------------------------------------------

    /**
     * give incorrect selected number
     */
    @Test
    public void testSell_Single_ErrorSelectedNumber1() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("0,1,2");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Single_ErrorSelectedNumber2() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("0,11,2,4");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Single_ErrorSelectedNumber3() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("even");
        clientTicket.getEntries().get(0).setBetOption(DigitalEntry.DIGITAL_BETOPTION_EVEN);

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, respCtx.getResponseCode());
    }

    /**
     * Total entry amount doesn't equal with ticket total amount.
     */
    @Test
    public void testSell_Single_UnmatchedAmount() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.setTotalAmount(new BigDecimal("360"));

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, respCtx.getResponseCode());
    }

    /**
     * Entry amount isn't in range
     */
    @Test
    public void testSell_Single_OutOfRange1() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setEntryAmount(new BigDecimal("40"));
        clientTicket.setTotalAmount(new BigDecimal("360"));

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, respCtx.getResponseCode());
    }

    /**
     * Entry amount isn't in range
     */
    @Test
    public void testSell_Single_OutOfRange2() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setEntryAmount(new BigDecimal("6000"));
        clientTicket.setTotalAmount(new BigDecimal("12280"));

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, respCtx.getResponseCode());
    }

    @Test
    public void testSell_No_EvenOdd_Supported() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("EVEN");
        clientTicket.getEntries().get(0).setBetOption(DigitalEntry.DIGITAL_BETOPTION_EVEN);

        this.jdbcTemplate.update("update FD_OPERATION_PARAMETERS set IS_ODD_EVEN=0");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_UNSUPPORTED_BETOPTION, respCtx.getResponseCode());
    }

    @Test
    public void testSell_No_Sum_Supported() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("24");
        clientTicket.getEntries().get(0).setBetOption(DigitalEntry.DIGITAL_BETOPTION_SUM);

        this.jdbcTemplate.update("update FD_OPERATION_PARAMETERS set IS_SUPPORT_SUM=0");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_UNSUPPORTED_BETOPTION, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Zero_MulteDraw() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.setMultipleDraws(0);

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_WRONG_MESSAGEBODY, respCtx.getResponseCode());
    }

    @Test
    public void testSell_GameInstanceSuspendSale() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();

        this.jdbcTemplate.update("update FD_GAME_INSTANCE set IS_SUSPEND_SALE=1");

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, respCtx.getResponseCode());
    }

    @Test
    public void testSell_SUM_exceedMax() throws Exception {
        printMethod();
        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(1).setSelectNumber("10000");
        clientTicket.setMultipleDraws(1);
        clientTicket.setTotalAmount(new BigDecimal("200.0"));

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, respCtx.getResponseCode());
    }

    @Test
    public void testSell_TypeE_6D() throws Exception {
        printMethod();

        this.jdbcTemplate.update("update FD_FUN_TYPE set NNN=6");
        this.jdbcTemplate.update("update FD_GAME_INSTANCE set PRIZE_LOGIC_ID='PL-DIG-E-111'");

        DigitalTicket clientTicket = DigitalDomainMocker.mockTicket();
        clientTicket.getEntries().get(0).setSelectNumber("1,2,3,4,5,6");
        clientTicket.getEntries().get(0).setBetOption(6);
        // remove sum entry
        clientTicket.getEntries().remove(1);
        clientTicket.setMultipleDraws(1);
        clientTicket.setTotalAmount(new BigDecimal("60.0"));

        // make sale request
        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), clientTicket);
        reqCtx.setGameTypeId(GameType.DIGITAL.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        DigitalTicket respTicket = (DigitalTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
    } // ---------------------------------------------------------------------------

    // SPRING DEPENDENCIES INJECTION
    // ---------------------------------------------------------------------------

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
