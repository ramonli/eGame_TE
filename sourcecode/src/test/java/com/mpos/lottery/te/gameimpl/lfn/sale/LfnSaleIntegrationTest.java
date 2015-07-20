package com.mpos.lottery.te.gameimpl.lfn.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lfn.LfnDomainMocker;
import com.mpos.lottery.te.gameimpl.lfn.game.LfnGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.gamespec.sale.web.QPEnquiryDto;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

public class LfnSaleIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    @Rollback(true)
    @Test
    public void testSell_MultiDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals("11003", ticketDto.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LFN, ticketDto.getRawSerialNo()).getBarcode(), ticketDto.getBarcode());
        assertNotNull(ticketDto.getValidationCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), ticketDto.getTotalAmount().doubleValue(), 0);
        assertEquals(2, ticketDto.getMultipleDraws());
        assertEquals(42, ticketDto.getTotalBets());
        assertEquals(2, ticketDto.getEntries().size());

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
        expectTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(2, hostTickets.size());
        LfnTicket expectTicket = new LfnTicket();
        expectTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
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
        expectTicket.setTotalBets(21);
        expectTicket.setValidationCode(ticketDto.getValidationCode());
        expectTicket.setBarcode(ticketDto.getBarcode());
        LfnGameInstance gameInstance = new LfnGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        assertEquals("8f906b772beefb22b47ba05c0eee78b2", ((LfnTicket) hostTickets.get(0)).getExtendText());
        expectTicket.setMultipleDraws(0);
        gameInstance.setId("GII-113");
        this.assertTicket(expectTicket, hostTickets.get(1));
        assertEquals("8f906b772beefb22b47ba05c0eee78b2", ((LfnTicket) hostTickets.get(0)).getExtendText());

        // asert entries
        List<LfnEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(LfnEntry.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(2, dbEntries.size());
        LfnEntry dbEntry0 = dbEntries.get(0);
        assertEquals(ticket.getEntries().get(0).getEntryAmount().doubleValue(),
                dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        LfnEntry dbEntry1 = dbEntries.get(1);
        assertEquals(ticket.getEntries().get(1).getEntryAmount().doubleValue(),
                dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(20, dbEntry1.getTotalBets());
    }

    @Test
    public void testSell_MultiDraw_QP_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.getEntries().get(0).setSelectNumber(null);
        ticket.getEntries().get(0).setInputChannel(BaseEntry.INPUT_CHANNEL_QP_OMR);
        ticket.getEntries().get(1).setSelectNumber(null);
        ticket.getEntries().get(1).setInputChannel(BaseEntry.INPUT_CHANNEL_QP_OMR);
        ticket.getEntries().get(1).setCountOfQPNumber(6);

        this.jdbcTemplate.update("delete from SELECTED_NUMBER_STAT where ID like 'DIG-%'");

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals("11003", ticketDto.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LFN, ticketDto.getRawSerialNo()).getBarcode(), ticketDto.getBarcode());
        assertNotNull(ticketDto.getValidationCode());
        assertEquals(2, ticketDto.getMultipleDraws());
        assertEquals(42, ticketDto.getTotalBets());
        assertEquals(2, ticketDto.getEntries().size());

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
        expectTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(2, hostTickets.size());
        LfnTicket expectTicket = new LfnTicket();
        expectTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
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
        expectTicket.setTotalBets(21);
        expectTicket.setValidationCode(ticketDto.getValidationCode());
        expectTicket.setBarcode(ticketDto.getBarcode());
        LfnGameInstance gameInstance = new LfnGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        this.assertTicket(expectTicket, hostTickets.get(0));
        // assertEquals("8f906b772beefb22b47ba05c0eee78b2",
        // ((LfnTicket)hostTickets.get(0)).getExtendText());
        expectTicket.setMultipleDraws(0);
        gameInstance.setId("GII-113");
        this.assertTicket(expectTicket, hostTickets.get(1));
        // assertEquals("8f906b772beefb22b47ba05c0eee78b2",
        // ((LfnTicket)hostTickets.get(0)).getExtendText());

        // asert entries
        List<LfnEntry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(LfnEntry.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(2, dbEntries.size());
        LfnEntry dbEntry0 = dbEntries.get(0);
        assertEquals(ticket.getEntries().get(0).getEntryAmount().doubleValue(),
                dbEntry0.getEntryAmount().doubleValue(), 0);
        assertEquals(1, dbEntry0.getTotalBets());
        LfnEntry dbEntry1 = dbEntries.get(1);
        assertEquals(ticket.getEntries().get(1).getEntryAmount().doubleValue(),
                dbEntry1.getEntryAmount().doubleValue(), 0);
        assertEquals(20, dbEntry1.getTotalBets());
    }

    @Test
    public void testSell_SingleDraw_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("1300.0"));

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();
        // this.setComplete();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        assertNotNull(ticketDto.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, ticketDto.getTicketType());
        assertEquals("11002", ticketDto.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LFN, ticketDto.getRawSerialNo()).getBarcode(), ticketDto.getBarcode());
        assertNotNull(ticketDto.getValidationCode());
        assertEquals(1, ticketDto.getMultipleDraws());
        assertEquals(21, ticketDto.getTotalBets());
        assertEquals(2, ticketDto.getEntries().size());

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
        expectTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(1, hostTickets.size());
        LfnTicket expectTicket = new LfnTicket();
        expectTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        expectTicket.setCountInPool(true);
        expectTicket.setDevId(reqCtx.getTerminalId());
        expectTicket.setOperatorId(reqCtx.getOperatorId());
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
        expectTicket.setTotalBets(21);
        expectTicket.setValidationCode(ticketDto.getValidationCode());
        expectTicket.setBarcode(ticketDto.getBarcode());
        LfnGameInstance gameInstance = new LfnGameInstance();
        gameInstance.setId("GII-112");
        expectTicket.setGameInstance(gameInstance);
        expectTicket.setExtendText("8f906b772beefb22b47ba05c0eee78b2");
        this.assertTicket(expectTicket, hostTickets.get(0));
    }

    @Test
    public void testSell_MultiDraw_DefiniteCredit_VerifyBalanceLog_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        this.jdbcTemplate.update("update sys_configuration set SUPPORT_CIMMISSION_CALCULATION=1");

        BigDecimal beforeCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());

        BigDecimal commission = SimpleToolkit.mathMultiple(ticket.getTotalAmount(), new BigDecimal("0.2"));
        BigDecimal afterCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        assertEquals(beforeCommBalance.add(commission).doubleValue(), afterCommBalance.doubleValue(), 0);

        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());

        List<BalanceTransactions> balanceLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                respCtx.getTransactionID());
        assertEquals(1, balanceLogs.size());
        BalanceTransactions operatorBalanceLog = balanceLogs.get(0);
        assertEquals(dbSaleTrans.getOperatorId(), operatorBalanceLog.getOperatorId());
        assertEquals(dbSaleTrans.getMerchantId(), operatorBalanceLog.getMerchantId());
        assertEquals(dbSaleTrans.getDeviceId(), operatorBalanceLog.getDeviceId());
        assertEquals(dbSaleTrans.getOperatorId(), operatorBalanceLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, operatorBalanceLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, operatorBalanceLog.getPaymentType());
        assertEquals(dbSaleTrans.getType(), operatorBalanceLog.getTransactionType());
        assertEquals(dbSaleTrans.getType(), operatorBalanceLog.getOriginalTransType());
        assertEquals(dbSaleTrans.getTotalAmount().doubleValue(), operatorBalanceLog.getTransactionAmount()
                .doubleValue(), 0);
        // 2600*0.2
        assertEquals(commission.doubleValue(), operatorBalanceLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.2, operatorBalanceLog.getCommissionRate().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, operatorBalanceLog.getStatus());
    }

    @Test
    public void testSell_MultiDraw_UseParentCredit_VerifyBalanceLog_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        this.jdbcTemplate.update("update sys_configuration set SUPPORT_CIMMISSION_CALCULATION=1");
        this.jdbcTemplate.update("update operator set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT
                + " where OPERATOR_ID='OPERATOR-111'");

        BigDecimal beforeOperatorCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal beforeMerchantCommBalance = this.getBaseJpaDao().findById(Merchant.class, 111l)
                .getCommisionBalance();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());

        // merchant commission
        BigDecimal commission = SimpleToolkit.mathMultiple(ticket.getTotalAmount(), new BigDecimal("0.2"));
        BigDecimal afterOperatorCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal afterMerchantCommBalance = this.getBaseJpaDao().findById(Merchant.class, 111l).getCommisionBalance();
        assertEquals(beforeOperatorCommBalance.doubleValue(), afterOperatorCommBalance.doubleValue(), 0);
        assertEquals(beforeMerchantCommBalance.doubleValue(), afterMerchantCommBalance.doubleValue(), 0);

        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());

        List<BalanceTransactions> balanceLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                respCtx.getTransactionID());
        this.sortBalanceTransactions(balanceLogs);
        assertEquals(2, balanceLogs.size());
        BalanceTransactions operatorBalanceLog = balanceLogs.get(0);
        assertEquals(dbSaleTrans.getOperatorId(), operatorBalanceLog.getOperatorId());
        assertEquals(dbSaleTrans.getMerchantId(), operatorBalanceLog.getMerchantId());
        assertEquals(dbSaleTrans.getDeviceId(), operatorBalanceLog.getDeviceId());
        assertEquals(dbSaleTrans.getOperatorId(), operatorBalanceLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, operatorBalanceLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, operatorBalanceLog.getPaymentType());
        assertEquals(dbSaleTrans.getType(), operatorBalanceLog.getTransactionType());
        assertEquals(dbSaleTrans.getTotalAmount().doubleValue(), operatorBalanceLog.getTransactionAmount()
                .doubleValue(), 0);
        // 2600*0.2
        assertEquals(commission.doubleValue(), operatorBalanceLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.2, operatorBalanceLog.getCommissionRate().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, operatorBalanceLog.getStatus());

        // assert parent merchant
        BalanceTransactions merchantBalanceLog = balanceLogs.get(1);
        assertEquals(dbSaleTrans.getOperatorId(), merchantBalanceLog.getOperatorId());
        assertEquals(dbSaleTrans.getMerchantId(), merchantBalanceLog.getMerchantId());
        assertEquals(dbSaleTrans.getDeviceId(), merchantBalanceLog.getDeviceId());
        // merchant 111
        assertEquals("111", merchantBalanceLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_MERCHANT, merchantBalanceLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, merchantBalanceLog.getPaymentType());
        assertEquals(dbSaleTrans.getType(), merchantBalanceLog.getTransactionType());
        assertEquals(dbSaleTrans.getTotalAmount().doubleValue(), merchantBalanceLog.getTransactionAmount()
                .doubleValue(), 0);
        assertEquals(0.0, merchantBalanceLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.0, merchantBalanceLog.getCommissionRate().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, merchantBalanceLog.getStatus());
    }

    @Test
    public void testSell_Exceed_MerchantAllowedMaxMultiple() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.getEntries().get(1).setSelectNumber("1,2,3,4,5,6,7,8,9,10,11,12");
        ticket.setTotalAmount(new BigDecimal("79300.0"));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_EXCEED_MAX_MULTIPLE, respCtx.getResponseCode());

        // TODO assert database output
    }

    @Test
    public void testSell_Exceed_MerchantAllowedMutiDraw() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.setMultipleDraws(4);
        ticket.setTotalAmount(new BigDecimal("5200.0"));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, respCtx.getResponseCode());

        // TODO assert database output
    }

    @Test
    public void testEnquiryQP() throws Exception {
        printMethod();
        QPEnquiryDto dto = new QPEnquiryDto();
        dto.setCountOfEntries(3);
        dto.setCountOfNumbers(4);
        dto.setGameInstance(LfnDomainMocker.mockTicket().getGameInstance());

        Context ctx = this.getDefaultContext(TransactionType.ENQUIRY_QP_NUMBERS.getRequestType(), dto);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        QPEnquiryDto respDto = (QPEnquiryDto) respCtx.getModel();
        assertEquals(3, respDto.getEntries().size());
    }

    @Test
    public void testSell_GameInstanceSuspendSale() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        this.jdbcTemplate.update("update LFN_GAME_INSTANCE set IS_SUSPEND_SALE=1");

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_SUSPENDED_GAME_INSTANCE, respCtx.getResponseCode());
    }

    @Test
    public void testSell_NBetoptionGreaterThanK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        LfnEntry entry = (LfnEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6");
        entry.setBetOption(6);

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_UNSUPPORTED_BETOPTION, respCtx.getResponseCode());
    }

    @Test
    public void testSell_PBetoptionGreaterThanK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        LfnEntry entry = (LfnEntry) ticket.getEntries().get(0);
        entry.setSelectNumber("1,2,3,4,5,6,7,8");
        entry.setBetOption(56);

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_UNSUPPORTED_BETOPTION, respCtx.getResponseCode());
    }

    @Test
    public void testSell_Under_EntryAmountLimit() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.getEntries().get(0).setEntryAmount(new BigDecimal("99.0"));

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, respCtx.getResponseCode());

        // TODO assert database output
    }

    @Test
    public void testSell_UnsupportedInputChannel() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.getEntries().get(0).setInputChannel(4, false);

        Context ctx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        ctx.setGameTypeId(Game.TYPE_LFN + "");
        // ctx.setGameTypeId("-1");
        Context respCtx = doPost(this.mockRequest(ctx));

        assertEquals(SystemException.CODE_WRONG_MESSAGEBODY, respCtx.getResponseCode());

        // TODO assert database output
    }

    @Test
    public void testSell_AfterSettlement() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();
        ticket.setMultipleDraws(1);
        ticket.setTotalAmount(new BigDecimal("1300.0"));

        String todayStr = SimpleToolkit.formatDate(new Date(), "MM/dd/yyyy HH:mm:ss");
        this.jdbcTemplate.update("update CARD_PAYOUT_HISTORY_ITEM t set t.PAYOUT_TIME=to_timestamp('" + todayStr
                + "', 'mm/dd/yyyy hh24:mi:ss')");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        reqCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        LfnTicket ticketDto = (LfnTicket) respCtx.getModel();
        // this.setComplete();

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_NOTRANS_ALLOWED_AFTER_SETTLEMNT, respCtx.getResponseCode());
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

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

}
