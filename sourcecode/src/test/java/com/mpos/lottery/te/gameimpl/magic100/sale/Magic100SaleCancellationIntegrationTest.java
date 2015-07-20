package com.mpos.lottery.te.gameimpl.magic100.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
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

public class Magic100SaleCancellationIntegrationTest extends BaseServletIntegrationTest {
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

    @Rollback(true)
    @Test
    public void testCancelByTicket_WinningSale() throws Exception {
        printMethod();

        // old credit level
        BigDecimal oldSaleBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCommissionBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal oldPayoutBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        // 1. make sale first
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        Magic100Ticket soldTicket = (Magic100Ticket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        LuckyNumberSequence luckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());

        // 2. make cancel by ticket first
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(),
                (Magic100Ticket) saleRespCtx.getModel());
        cancelCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // verify credit level
        BigDecimal newSaleBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCommissionBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal newPayoutBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldSaleBalanceOperator.doubleValue(), newSaleBalanceOperator.doubleValue(), 0);
        assertEquals(oldPayoutBalanceOperator.doubleValue(), newPayoutBalanceOperator.doubleValue(), 0);
        assertEquals(oldCommissionBalanceOperator.doubleValue(), newCommissionBalanceOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert number sequence...cancellation should't rollback the sequence
        LuckyNumberSequence nowSequence = this.getLuckyNumberSequenceDao().lookup(ticket.getGameInstance().getGameId());
        assertEquals(luckyNumberSequence.getNextSequence(), nowSequence.getNextSequence());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(soldTicket.getSerialNo());
        expectTrans.setOperatorId(cancelCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectTrans.setType(cancelCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                soldTicket.getSerialNo(), false);
        assertEquals(1, dbTickets.size());
        assertEquals(BaseTicket.STATUS_CANCELED, dbTickets.get(0).getStatus());
        assertEquals(ticket.getTotalAmount().doubleValue(), dbTickets.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getMultipleDraws(), dbTickets.get(0).getMultipleDraws());
        assertEquals(ticket.getUser().getMobile(), dbTickets.get(0).getMobile());
        assertNull(dbTickets.get(0).getCreditCardSN());
        assertFalse(dbTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), dbTickets.get(0).getTransType());
        assertTrue(dbTickets.get(0).isWinning());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(soldTicket.getSerialNo());
        assertEquals(1, dbPayouts.size());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(0).getStatus());

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction(
                cancelRespCtx.getTransactionID());
        assertEquals(0, requeuedNumbers.getBeginOfValidNumbers());
        assertEquals(3, requeuedNumbers.getCountOfNumbers());
        assertEquals(3, requeuedNumbers.getCountOfValidNumbers());
        assertNotNull(requeuedNumbers.getUpdateTime());
        assertNotNull(requeuedNumbers.getCreateTime());

        List<RequeuedNumbersItem> items = requeuedNumbers.lookupValidItems(3);
        assertEquals(3, items.size());
        assertEquals(6, items.get(0).getSequenceOfNumber());
        assertEquals("6", items.get(0).getLuckyNumber());
        assertEquals(0.0, items.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(7, items.get(1).getSequenceOfNumber());
        assertEquals("7", items.get(1).getLuckyNumber());
        assertEquals(0.0, items.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(1).getTaxAmount().doubleValue(), 0);
        assertEquals(8, items.get(2).getSequenceOfNumber());
        assertEquals("8", items.get(2).getLuckyNumber());
        assertEquals(1000.0, items.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(2).getTaxAmount().doubleValue(), 0);

        // assert commission logs
        List<BalanceTransactions> commissionLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                cancelRespCtx.getTransactionID());
        assertEquals(2, commissionLogs.size());
        // cancellation of sale commission log
        BalanceTransactions commLog = this.getBalanceTransactionsDao()
                .findByTransactionAndOwnerAndGameAndOrigTransType(cancelRespCtx.getTransactionID(),
                        cancelCtx.getOperatorId(), "LK-1", TransactionType.SELL_TICKET.getRequestType());
        assertEquals(dbTrans.getOperatorId(), commLog.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), commLog.getMerchantId());
        assertEquals(dbTrans.getDeviceId(), commLog.getDeviceId());
        assertEquals(dbTrans.getOperatorId(), commLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, commLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY, commLog.getPaymentType());
        assertEquals(dbTrans.getType(), commLog.getTransactionType());
        assertEquals(-30.0, commLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.1, commLog.getCommissionRate().doubleValue(), 0);
        assertEquals(300.0, commLog.getTransactionAmount().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, commLog.getStatus());
        // cancellation of payout commission log
        commLog = this.getBalanceTransactionsDao().findByTransactionAndOwnerAndGameAndOrigTransType(
                cancelRespCtx.getTransactionID(), cancelCtx.getOperatorId(), "LK-1",
                TransactionType.PAYOUT.getRequestType());
        assertEquals(dbTrans.getOperatorId(), commLog.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), commLog.getMerchantId());
        assertEquals(dbTrans.getDeviceId(), commLog.getDeviceId());
        assertEquals(dbTrans.getOperatorId(), commLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, commLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, commLog.getPaymentType());
        assertEquals(dbTrans.getType(), commLog.getTransactionType());
        assertEquals(-200.0, commLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.2, commLog.getCommissionRate().doubleValue(), 0);
        assertEquals(1000.0, commLog.getTransactionAmount().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, commLog.getStatus());
    }

    @Rollback(true)
    @Test
    public void testCancelByTicket_WinningSale_Operator_UseParent() throws Exception {
        printMethod();

        this.jdbcTemplate.update("update operator set limit_type=" + Merchant.CREDIT_TYPE_USE_PARENT);

        // old credit level
        BigDecimal oldSaleBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCommissionBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal oldPayoutBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldSaleBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldPayoutBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l)
                .getPayoutCreditLevel();
        BigDecimal oldCommissionBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l)
                .getCommisionBalance();

        // 1. make sale first
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        Magic100Ticket soldTicket = (Magic100Ticket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        LuckyNumberSequence luckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());

        BigDecimal mSaleBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal mCommissionBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal mPayoutBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal mSaleBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal mPayoutBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal mCommissionBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l)
                .getCommisionBalance();
        assertEquals(oldSaleBalanceOperator.doubleValue(), mSaleBalanceOperator.doubleValue(), 0);
        assertEquals(oldPayoutBalanceOperator.doubleValue(), mPayoutBalanceOperator.doubleValue(), 0);
        assertEquals(oldCommissionBalanceOperator.doubleValue(), mCommissionBalanceOperator.doubleValue(), 0);
        assertEquals(oldSaleBalanceMerchant.subtract(new BigDecimal("300.0")).doubleValue(),
                mSaleBalanceMerchant.doubleValue(), 0);
        assertEquals(oldPayoutBalanceMerchant.add(new BigDecimal("1000.0")).doubleValue(),
                mPayoutBalanceMerchant.doubleValue(), 0);
        assertEquals(oldCommissionBalanceMerchant.doubleValue(), mCommissionBalanceMerchant.doubleValue(), 0);

        // 2. make cancel by ticket first
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(),
                (Magic100Ticket) saleRespCtx.getModel());
        cancelCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // verify credit level
        BigDecimal newSaleBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCommissionBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal newPayoutBalanceOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newSaleBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newPayoutBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l)
                .getPayoutCreditLevel();
        BigDecimal newCommissionBalanceMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l)
                .getCommisionBalance();
        assertEquals(oldSaleBalanceOperator.doubleValue(), newSaleBalanceOperator.doubleValue(), 0);
        assertEquals(oldPayoutBalanceOperator.doubleValue(), newPayoutBalanceOperator.doubleValue(), 0);
        assertEquals(oldCommissionBalanceOperator.doubleValue(), newCommissionBalanceOperator.doubleValue(), 0);
        assertEquals(oldSaleBalanceMerchant.doubleValue(), newSaleBalanceMerchant.doubleValue(), 0);
        assertEquals(oldPayoutBalanceMerchant.doubleValue(), newPayoutBalanceMerchant.doubleValue(), 0);
        assertEquals(oldCommissionBalanceMerchant.doubleValue(), newCommissionBalanceMerchant.doubleValue(), 0);

        // assert number sequence...cancellation should't rollback the sequence
        LuckyNumberSequence nowSequence = this.getLuckyNumberSequenceDao().lookup(ticket.getGameInstance().getGameId());
        assertEquals(luckyNumberSequence.getNextSequence(), nowSequence.getNextSequence());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(soldTicket.getSerialNo());
        expectTrans.setOperatorId(cancelCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectTrans.setType(cancelCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                soldTicket.getSerialNo(), false);
        assertEquals(1, dbTickets.size());
        assertEquals(BaseTicket.STATUS_CANCELED, dbTickets.get(0).getStatus());
        assertEquals(ticket.getTotalAmount().doubleValue(), dbTickets.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getMultipleDraws(), dbTickets.get(0).getMultipleDraws());
        assertEquals(ticket.getUser().getMobile(), dbTickets.get(0).getMobile());
        assertNull(dbTickets.get(0).getCreditCardSN());
        assertFalse(dbTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), dbTickets.get(0).getTransType());
        assertTrue(dbTickets.get(0).isWinning());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(soldTicket.getSerialNo());
        assertEquals(1, dbPayouts.size());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(0).getStatus());

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction(
                cancelRespCtx.getTransactionID());
        assertEquals(0, requeuedNumbers.getBeginOfValidNumbers());
        assertEquals(3, requeuedNumbers.getCountOfNumbers());
        assertEquals(3, requeuedNumbers.getCountOfValidNumbers());
        assertNotNull(requeuedNumbers.getUpdateTime());
        assertNotNull(requeuedNumbers.getCreateTime());

        List<RequeuedNumbersItem> items = requeuedNumbers.lookupValidItems(3);
        assertEquals(3, items.size());
        assertEquals(6, items.get(0).getSequenceOfNumber());
        assertEquals("6", items.get(0).getLuckyNumber());
        assertEquals(0.0, items.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(7, items.get(1).getSequenceOfNumber());
        assertEquals("7", items.get(1).getLuckyNumber());
        assertEquals(0.0, items.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(1).getTaxAmount().doubleValue(), 0);
        assertEquals(8, items.get(2).getSequenceOfNumber());
        assertEquals("8", items.get(2).getLuckyNumber());
        assertEquals(1000.0, items.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(2).getTaxAmount().doubleValue(), 0);

        // assert commission logs
        List<BalanceTransactions> commissionLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                cancelRespCtx.getTransactionID());
        assertEquals(4, commissionLogs.size());
        // sale commission log
        BalanceTransactions commLog = this.getBalanceTransactionsDao()
                .findByTransactionAndOwnerAndGameAndOrigTransType(cancelRespCtx.getTransactionID(),
                        cancelCtx.getOperatorId(), "LK-1", TransactionType.SELL_TICKET.getRequestType());
        assertEquals(dbTrans.getOperatorId(), commLog.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), commLog.getMerchantId());
        assertEquals(dbTrans.getDeviceId(), commLog.getDeviceId());
        assertEquals(dbTrans.getOperatorId(), commLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, commLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY, commLog.getPaymentType());
        assertEquals(dbTrans.getType(), commLog.getTransactionType());
        assertEquals(-30.0, commLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.1, commLog.getCommissionRate().doubleValue(), 0);
        assertEquals(300.0, commLog.getTransactionAmount().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, commLog.getStatus());
        // payout commission log
        commLog = this.getBalanceTransactionsDao().findByTransactionAndOwnerAndGameAndOrigTransType(
                cancelRespCtx.getTransactionID(), cancelCtx.getOperatorId(), "LK-1",
                TransactionType.PAYOUT.getRequestType());
        assertEquals(dbTrans.getOperatorId(), commLog.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), commLog.getMerchantId());
        assertEquals(dbTrans.getDeviceId(), commLog.getDeviceId());
        assertEquals(dbTrans.getOperatorId(), commLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, commLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, commLog.getPaymentType());
        assertEquals(dbTrans.getType(), commLog.getTransactionType());
        assertEquals(-200.0, commLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.2, commLog.getCommissionRate().doubleValue(), 0);
        assertEquals(1000.0, commLog.getTransactionAmount().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, commLog.getStatus());
        // sale of merchant
        commLog = this.getBalanceTransactionsDao().findByTransactionAndOwnerAndGameAndOrigTransType(
                cancelRespCtx.getTransactionID(), dbTrans.getMerchantId() + "", "LK-1",
                TransactionType.SELL_TICKET.getRequestType());
        assertEquals(dbTrans.getOperatorId(), commLog.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), commLog.getMerchantId());
        assertEquals(dbTrans.getDeviceId(), commLog.getDeviceId());
        assertEquals(BalanceTransactions.OWNER_TYPE_MERCHANT, commLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY, commLog.getPaymentType());
        assertEquals(dbTrans.getType(), commLog.getTransactionType());
        assertEquals(0.0, commLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.0, commLog.getCommissionRate().doubleValue(), 0);
        assertEquals(300.0, commLog.getTransactionAmount().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, commLog.getStatus());
        // payout commission log of merchant
        commLog = this.getBalanceTransactionsDao().findByTransactionAndOwnerAndGameAndOrigTransType(
                cancelRespCtx.getTransactionID(), dbTrans.getMerchantId() + "", "LK-1",
                TransactionType.PAYOUT.getRequestType());
        assertEquals(dbTrans.getOperatorId(), commLog.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), commLog.getMerchantId());
        assertEquals(dbTrans.getDeviceId(), commLog.getDeviceId());
        assertEquals(BalanceTransactions.OWNER_TYPE_MERCHANT, commLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, commLog.getPaymentType());
        assertEquals(dbTrans.getType(), commLog.getTransactionType());
        assertEquals(0.0, commLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.0, commLog.getCommissionRate().doubleValue(), 0);
        assertEquals(1000.0, commLog.getTransactionAmount().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, commLog.getStatus());
    }

    @Test
    public void testCancelByTicket_WinningSale_BuyAgain() throws Exception {
        printMethod();

        // remove requeued numbers, make the sale will buy 8,9,10
        this.jdbcTemplate.update("delete from LK_REQUEUE_NUMBERS");
        this.jdbcTemplate.update("delete from LK_REQUEUE_NUMBERS_ITEM");

        // old credit level
        // old sale credit level
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        // 1. make sale first
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));

        this.entityManager.flush();
        this.entityManager.clear();
        assertEquals(200, saleRespCtx.getResponseCode());

        // 2. make cancel by ticket first
        // (Magic100Ticket) saleRespCtx.getModel());
        // cancelCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        // Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        Transaction saleTrans = new Transaction();
        saleTrans.setDeviceId(saleCtx.getTerminalId());
        saleTrans.setTraceMessageId(saleCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), saleTrans);
        cancelCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();
        assertEquals(200, cancelRespCtx.getResponseCode());

        // 3. make the sale again
        Context againSaleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        againSaleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context againSaleRespCtx = doPost(this.mockRequest(againSaleCtx));
        Magic100Ticket againSoldTicket = (Magic100Ticket) againSaleRespCtx.getModel();

        // assert response
        assertEquals(200, againSaleRespCtx.getResponseCode());
        assertNotNull(againSoldTicket.getRawSerialNo());
        assertEquals(BaseTicket.TICKET_TYPE_NORMAL, againSoldTicket.getTicketType());
        assertEquals(ticket.getGameInstance().getNumber(), againSoldTicket.getLastDrawNo());
        assertEquals(new Barcoder(Game.TYPE_LUCKYNUMBER, againSoldTicket.getRawSerialNo()).getBarcode(),
                againSoldTicket.getBarcode());
        assertNotNull(againSoldTicket.getValidationCode());
        assertEquals(ticket.getTotalAmount().doubleValue(), againSoldTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(1, againSoldTicket.getMultipleDraws());
        assertEquals(3, againSoldTicket.getTotalBets());
        assertEquals(3, againSoldTicket.getEntries().size());
        List entries = againSoldTicket.getEntries();
        this.sortEntries(entries);
        assertEquals("8", ((Magic100Entry) entries.get(0)).getSelectNumber());
        assertEquals(true, ((Magic100Entry) entries.get(0)).isWinning());
        assertEquals(1000.0, ((Magic100Entry) entries.get(0)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) entries.get(0)).getTaxAmount().doubleValue(), 0);
        assertEquals("9", ((Magic100Entry) entries.get(1)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) entries.get(1)).isWinning());
        assertEquals(0.0, ((Magic100Entry) entries.get(1)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) entries.get(1)).getTaxAmount().doubleValue(), 0);
        assertEquals("10", ((Magic100Entry) entries.get(2)).getSelectNumber());
        assertEquals(false, ((Magic100Entry) entries.get(2)).isWinning());
        assertEquals(0.0, ((Magic100Entry) entries.get(2)).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, ((Magic100Entry) entries.get(2)).getTaxAmount().doubleValue(), 0);

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
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, againSaleRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(againSaleRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(againSoldTicket.getSerialNo());
        expectTrans.setOperatorId(againSaleRespCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(againSaleRespCtx.getTerminalId());
        expectTrans.setTraceMessageId(againSaleRespCtx.getTraceMessageId());
        expectTrans.setType(againSaleCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                againSoldTicket.getSerialNo(), false);
        Magic100Ticket expTicket = new Magic100Ticket();
        expTicket.setSerialNo(againSoldTicket.getSerialNo());
        expTicket.setCountInPool(true);
        expTicket.setStatus(BaseTicket.STATUS_PAID);
        expTicket.setDevId(againSaleRespCtx.getTerminalId());
        expTicket.setOperatorId(againSaleRespCtx.getOperatorId());
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
        expTicket.setValidationCode(againSoldTicket.getValidationCode());
        expTicket.setBarcode(againSoldTicket.getBarcode());
        Magic100GameInstance gameInstance = new Magic100GameInstance();
        gameInstance.setId("GII-111");
        expTicket.setGameInstance(gameInstance);
        this.assertTicket(expTicket, dbTickets.get(0));

        // assert entry
        List<Magic100Entry> dbEntries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                againSoldTicket.getSerialNo(), false);
        assertEquals(3, dbEntries.size());
        this.sortEntries(dbEntries);

        assertEquals(8, dbEntries.get(0).getSequenceOfNumber());
        assertEquals("8", dbEntries.get(0).getSelectNumber());
        assertEquals(true, dbEntries.get(0).isWinning());
        assertEquals(1000.0, dbEntries.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(9, dbEntries.get(1).getSequenceOfNumber());
        assertEquals("9", dbEntries.get(1).getSelectNumber());
        assertEquals(false, dbEntries.get(1).isWinning());
        assertEquals(0.0, dbEntries.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(1).getTaxAmount().doubleValue(), 0);
        assertEquals(10, dbEntries.get(2).getSequenceOfNumber());
        assertEquals("10", dbEntries.get(2).getSelectNumber());
        assertEquals(false, dbEntries.get(2).isWinning());
        assertEquals(0.0, dbEntries.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, dbEntries.get(2).getTaxAmount().doubleValue(), 0);

        // assert lucky number sequence
        LuckyNumberSequence dbLuckyNumberSequence = this.getLuckyNumberSequenceDao().lookup(
                ticket.getGameInstance().getGameId());
        assertEquals(1, dbLuckyNumberSequence.getNextSequence());
        // assertNull(dbLuckyNumberSequence.getLastestPlayer());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(againSoldTicket.getSerialNo());
        assertEquals(1, dbPayouts.size());
        assertEquals(1000.0, dbPayouts.get(0).getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals(1000.0, dbPayouts.get(0).getTotalAmount().doubleValue(), 0);

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction(
                cancelRespCtx.getTransactionID());
        for (RequeuedNumbersItem item : requeuedNumbers.getRequeuedNumbersItemList()) {
            assertEquals(RequeuedNumbersItem.STATE_INVALID, item.getState());
        }
    }

    @Test
    public void testCancelByTicket_WinningSale_Manually() throws Exception {
        printMethod();

        // 1. make sale first
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        Magic100Ticket soldTicket = (Magic100Ticket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancel by ticket first
        Magic100Ticket cancelTicket = (Magic100Ticket) saleRespCtx.getModel();
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(),
                (Magic100Ticket) saleRespCtx.getModel());
        cancelCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert transaction
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTrans.getType());

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                soldTicket.getSerialNo(), false);
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTickets.get(0).getTransType());
    }

    @Rollback(true)
    @Test
    public void testCancelByTrans_WinningSale() throws Exception {
        printMethod();

        // old credit level
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        // 1. make sale first
        Magic100Ticket ticket = Magic100DomainMocker.mockTicket();
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LUCKYNUMBER + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        Magic100Ticket soldTicket = (Magic100Ticket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancel by ticket first
        Transaction saleTrans = new Transaction();
        saleTrans.setDeviceId(saleCtx.getTerminalId());
        saleTrans.setTraceMessageId(saleCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), saleTrans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // Verify credit level
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCreditMerchat = this.getBaseJpaDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchat.doubleValue(), 0);

        // assert sale transaction
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // verify transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId("LK-1");
        expectTrans.setTotalAmount(ticket.getTotalAmount());
        expectTrans.setTicketSerialNo(soldTicket.getSerialNo());
        expectTrans.setOperatorId(cancelCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectTrans.setType(cancelCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // assert ticket
        List<Magic100Ticket> dbTickets = this.getBaseTicketDao().findBySerialNo(Magic100Ticket.class,
                soldTicket.getSerialNo(), false);
        assertEquals(1, dbTickets.size());
        assertEquals(BaseTicket.STATUS_CANCELED, dbTickets.get(0).getStatus());
        assertEquals(ticket.getTotalAmount().doubleValue(), dbTickets.get(0).getTotalAmount().doubleValue(), 0);
        assertEquals(ticket.getMultipleDraws(), dbTickets.get(0).getMultipleDraws());
        assertEquals(ticket.getUser().getMobile(), dbTickets.get(0).getMobile());
        assertNull(dbTickets.get(0).getCreditCardSN());
        assertFalse(dbTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), dbTickets.get(0).getTransType());
        assertTrue(dbTickets.get(0).isWinning());

        // assert payout
        List<Payout> dbPayouts = this.getPayoutDao().getByTicketSerialNo(soldTicket.getSerialNo());
        assertEquals(1, dbPayouts.size());
        assertEquals(Payout.STATUS_REVERSED, dbPayouts.get(0).getStatus());

        // assert requeued numbers
        RequeuedNumbers requeuedNumbers = this.getRequeuedNumbersDao().findByTransaction(
                cancelRespCtx.getTransactionID());
        assertEquals(0, requeuedNumbers.getBeginOfValidNumbers());
        assertEquals(3, requeuedNumbers.getCountOfNumbers());
        assertEquals(3, requeuedNumbers.getCountOfValidNumbers());
        assertNotNull(requeuedNumbers.getUpdateTime());
        assertNotNull(requeuedNumbers.getCreateTime());

        List<RequeuedNumbersItem> items = requeuedNumbers.lookupValidItems(3);
        assertEquals(3, items.size());
        assertEquals(6, items.get(0).getSequenceOfNumber());
        assertEquals("6", items.get(0).getLuckyNumber());
        assertEquals(0.0, items.get(0).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(0).getTaxAmount().doubleValue(), 0);
        assertEquals(7, items.get(1).getSequenceOfNumber());
        assertEquals("7", items.get(1).getLuckyNumber());
        assertEquals(0.0, items.get(1).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(1).getTaxAmount().doubleValue(), 0);
        assertEquals(8, items.get(2).getSequenceOfNumber());
        assertEquals("8", items.get(2).getLuckyNumber());
        assertEquals(1000.0, items.get(2).getPrizeAmount().doubleValue(), 0);
        assertEquals(0.0, items.get(2).getTaxAmount().doubleValue(), 0);
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

    public RequeuedNumbersDao getRequeuedNumbersDao() {
        return requeuedNumbersDao;
    }

    public void setRequeuedNumbersDao(RequeuedNumbersDao requeuedNumbersDao) {
        this.requeuedNumbersDao = requeuedNumbersDao;
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
