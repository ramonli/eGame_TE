package com.mpos.lottery.te.gameimpl.lfn.sale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lfn.LfnDomainMocker;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
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
import java.util.List;

import javax.annotation.Resource;

public class LfnSaleCancellationIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    @Rollback(true)
    @Test
    public void testCancelByTicket_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        LfnTicket ticketDto = (LfnTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        LfnTicket cancelTicket = new LfnTicket();
        cancelTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(Game.TYPE_LFN + "");
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
        assertEquals(cancelRespCtx.getTransactionID(), dbSaleTrans.getCancelTransactionId());
        assertEquals(cancelCtx.getTransType(), dbSaleTrans.getCancelTransactionType().intValue());

        // assert transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(cancelRespCtx.getTransactionID());
        expectedTrans.setDeviceId(cancelCtx.getTerminalId());
        expectedTrans.setOperatorId(cancelCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setType(cancelCtx.getTransType());
        expectedTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectedTrans.setGameId("LFN-1");
        expectedTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectedTrans.setTotalAmount(ticket.getTotalAmount());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction cancelTrans = this.getTransactionDao()
                .findById(Transaction.class, cancelRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, cancelTrans);
        assertEquals(saleCtx.getTransType(), cancelTrans.getCancelTransactionType().intValue());
        assertEquals(dbSaleTrans.getId(), cancelTrans.getCancelTransactionId());

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), hostTickets.get(0).getTransType());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(1).getStatus());
        assertFalse(hostTickets.get(1).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TICKET.getRequestType(), hostTickets.get(1).getTransType());
    }

    @Test
    public void testCancelByTicket_Manually_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        LfnTicket ticketDto = (LfnTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        LfnTicket cancelTicket = new LfnTicket();
        cancelTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        cancelTicket.setManualCancel(true);
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getTransactionDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());
        assertEquals(cancelRespCtx.getTransactionID(), dbSaleTrans.getCancelTransactionId());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbSaleTrans.getCancelTransactionType()
                .intValue());

        // assert transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), dbTrans.getType());
        assertEquals(saleCtx.getTransType(), dbTrans.getCancelTransactionType().intValue());
        assertEquals(dbSaleTrans.getId(), dbTrans.getCancelTransactionId());

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), hostTickets.get(0).getTransType());
        assertEquals(TransactionType.CANCEL_BY_CLIENT_MANUALLY.getRequestType(), hostTickets.get(1).getTransType());
    }

    @Test
    public void testCancelByTicket_Repeatedly() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        LfnTicket ticketDto = (LfnTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation after freezing time
        LfnTicket cancelTicket = new LfnTicket();
        cancelTicket.setRawSerialNo(ticketDto.getRawSerialNo());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));
        // this.setComplete();
        assertEquals(200, cancelRespCtx.getResponseCode());

        this.entityManager.flush();
        this.entityManager.clear();

        // 3.cancel repeatedly
        cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TICKET.getRequestType(), cancelTicket);
        cancelCtx.setGameTypeId(Game.TYPE_LFN + "");
        cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // assert response
        assertEquals(SystemException.CODE_CANCELLED_TRANS, cancelRespCtx.getResponseCode());

        // assert sale transaction
        Transaction dbSaleTrans = this.getTransactionDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());

        // assert the credit level
        BigDecimal newCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldCreditOperator.doubleValue(), newCreditOperator.doubleValue(), 0);
        assertEquals(oldCreditMerchant.doubleValue(), newCreditMerchant.doubleValue(), 0);

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(((LfnTicket) hostTickets.get(0)).isCountInPool());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(1).getStatus());
        assertFalse(((LfnTicket) hostTickets.get(1)).isCountInPool());
    }

    @Test
    public void testCancelByTransaction_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        // old credit level
        BigDecimal oldCreditMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getMerchantDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        // this.setComplete();
        assertEquals(200, saleRespCtx.getResponseCode());
        LfnTicket ticketDto = (LfnTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(saleCtx.getTerminalId());
        trans.setTraceMessageId(saleCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        cancelCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

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
        assertEquals(cancelRespCtx.getTransactionID(), dbSaleTrans.getCancelTransactionId());
        assertEquals(cancelCtx.getTransType(), dbSaleTrans.getCancelTransactionType().intValue());

        // assert transaction
        Transaction expectedTrans = new Transaction();
        expectedTrans.setId(cancelRespCtx.getTransactionID());
        expectedTrans.setDeviceId(cancelCtx.getTerminalId());
        expectedTrans.setOperatorId(cancelCtx.getOperatorId());
        expectedTrans.setMerchantId(111);
        expectedTrans.setType(cancelCtx.getTransType());
        expectedTrans.setTraceMessageId(cancelCtx.getTraceMessageId());
        expectedTrans.setGameId("LFN-1");
        expectedTrans.setTicketSerialNo(BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()));
        expectedTrans.setTotalAmount(ticket.getTotalAmount());
        expectedTrans.setResponseCode(SystemException.CODE_OK);
        Transaction cancelDbTrans = this.getTransactionDao().findById(Transaction.class,
                cancelRespCtx.getTransactionID());
        this.assertTransaction(expectedTrans, cancelDbTrans);
        assertEquals(saleCtx.getTransType(), cancelDbTrans.getCancelTransactionType().intValue());
        assertEquals(dbSaleTrans.getId(), cancelDbTrans.getCancelTransactionId());

        // assert ticket
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(LfnTicket.class,
                BaseTicket.encryptSerialNo(ticketDto.getRawSerialNo()), false);
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(0).getStatus());
        assertFalse(hostTickets.get(0).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), hostTickets.get(0).getTransType());
        assertEquals(BaseTicket.STATUS_CANCELED, hostTickets.get(1).getStatus());
        assertFalse(hostTickets.get(1).isCountInPool());
        assertEquals(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), hostTickets.get(1).getTransType());
    }

    @Rollback(true)
    @Test
    public void testCancel_DefiniteCredit_VerifyBalanceLog_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        this.jdbcTemplate.update("update sys_configuration set SUPPORT_CIMMISSION_CALCULATION=1");

        BigDecimal beforeCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(saleCtx.getTerminalId());
        trans.setTraceMessageId(saleCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        cancelCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        BigDecimal afterCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        assertEquals(beforeCommBalance.doubleValue(), afterCommBalance.doubleValue(), 0);

        List<BalanceTransactions> saleBalanceLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                saleRespCtx.getTransactionID());
        for (BalanceTransactions saleOperatorLog : saleBalanceLogs) {
            assertEquals(BalanceTransactions.STATUS_INVALID, saleOperatorLog.getStatus());
        }
        Transaction dbCancelTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());

        this.sortBalanceTransactions(saleBalanceLogs);
        BalanceTransactions saleOperatorBalanceLog = saleBalanceLogs.get(0);
        List<BalanceTransactions> balanceLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                cancelRespCtx.getTransactionID());
        assertEquals(1, balanceLogs.size());
        BalanceTransactions operatorBalanceLog = balanceLogs.get(0);
        assertEquals(dbCancelTrans.getOperatorId(), operatorBalanceLog.getOperatorId());
        assertEquals(dbCancelTrans.getMerchantId(), operatorBalanceLog.getMerchantId());
        assertEquals(dbCancelTrans.getDeviceId(), operatorBalanceLog.getDeviceId());
        assertEquals(saleOperatorBalanceLog.getOperatorId(), operatorBalanceLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, operatorBalanceLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY, operatorBalanceLog.getPaymentType());
        assertEquals(cancelCtx.getTransType(), operatorBalanceLog.getTransactionType());
        assertEquals(saleCtx.getTransType(), operatorBalanceLog.getOriginalTransType());
        assertEquals(saleOperatorBalanceLog.getTransactionAmount().doubleValue(), operatorBalanceLog
                .getTransactionAmount().doubleValue(), 0);
        assertEquals(saleOperatorBalanceLog.getCommissionAmount().doubleValue() * -1, operatorBalanceLog
                .getCommissionAmount().doubleValue(), 0);
        assertEquals(saleOperatorBalanceLog.getCommissionRate().doubleValue(), operatorBalanceLog.getCommissionRate()
                .doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, operatorBalanceLog.getStatus());
    }

    @Test
    public void testCancel_UseParentCredit_VerifyBalanceLog_OK() throws Exception {
        printMethod();
        LfnTicket ticket = LfnDomainMocker.mockTicket();

        this.jdbcTemplate.update("update sys_configuration set SUPPORT_CIMMISSION_CALCULATION=1");
        this.jdbcTemplate.update("update operator set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT
                + " where OPERATOR_ID='OPERATOR-111'");

        BigDecimal beforeOperatorCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal beforeMerchantCommBalance = this.getBaseJpaDao().findById(Merchant.class, 111l)
                .getCommisionBalance();

        // 1. make sale
        Context saleCtx = this.getDefaultContext(TransactionType.SELL_TICKET.getRequestType(), ticket);
        saleCtx.setGameTypeId(Game.TYPE_LFN + "");
        Context saleRespCtx = doPost(this.mockRequest(saleCtx));
        LfnTicket ticketDto = (LfnTicket) saleRespCtx.getModel();

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(saleCtx.getTerminalId());
        trans.setTraceMessageId(saleCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        cancelCtx.setGameTypeId(Game.TYPE_UNDEF + "");
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, cancelRespCtx.getResponseCode());

        // merchant commission
        // merchant commission
        BigDecimal commission = SimpleToolkit.mathMultiple(ticket.getTotalAmount(), new BigDecimal("0.2"));
        BigDecimal afterOperatorCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal afterMerchantCommBalance = this.getBaseJpaDao().findById(Merchant.class, 111l).getCommisionBalance();
        assertEquals(beforeOperatorCommBalance.doubleValue(), afterOperatorCommBalance.doubleValue(), 0);
        assertEquals(beforeMerchantCommBalance.doubleValue(), afterMerchantCommBalance.doubleValue(), 0);

        List<BalanceTransactions> saleBalanceLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                saleRespCtx.getTransactionID());
        for (BalanceTransactions saleOperatorLog : saleBalanceLogs) {
            assertEquals(BalanceTransactions.STATUS_INVALID, saleOperatorLog.getStatus());
        }
        Transaction dbCancelTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());

        this.sortBalanceTransactions(saleBalanceLogs);
        BalanceTransactions saleOperatorBalanceLog = saleBalanceLogs.get(0);
        List<BalanceTransactions> balanceLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                cancelRespCtx.getTransactionID());
        assertEquals(2, balanceLogs.size());
        BalanceTransactions operatorBalanceLog = balanceLogs.get(0);
        assertEquals(dbCancelTrans.getOperatorId(), operatorBalanceLog.getOperatorId());
        assertEquals(dbCancelTrans.getMerchantId(), operatorBalanceLog.getMerchantId());
        assertEquals(dbCancelTrans.getDeviceId(), operatorBalanceLog.getDeviceId());
        assertEquals(saleOperatorBalanceLog.getOperatorId(), operatorBalanceLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, operatorBalanceLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY, operatorBalanceLog.getPaymentType());
        assertEquals(cancelCtx.getTransType(), operatorBalanceLog.getTransactionType());
        assertEquals(saleOperatorBalanceLog.getTransactionAmount().doubleValue(), operatorBalanceLog
                .getTransactionAmount().doubleValue(), 0);
        assertEquals(saleOperatorBalanceLog.getCommissionAmount().doubleValue() * -1, operatorBalanceLog
                .getCommissionAmount().doubleValue(), 0);
        assertEquals(saleOperatorBalanceLog.getCommissionRate().doubleValue(), operatorBalanceLog.getCommissionRate()
                .doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, operatorBalanceLog.getStatus());

        // assert parent merchant
        BalanceTransactions saleMerchantBalanceLog = saleBalanceLogs.get(1);
        BalanceTransactions merchantBalanceLog = balanceLogs.get(1);
        assertEquals(dbCancelTrans.getOperatorId(), merchantBalanceLog.getOperatorId());
        assertEquals(dbCancelTrans.getMerchantId(), merchantBalanceLog.getMerchantId());
        assertEquals(dbCancelTrans.getDeviceId(), merchantBalanceLog.getDeviceId());
        // merchant 111
        assertEquals(saleMerchantBalanceLog.getOwnerId(), merchantBalanceLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_MERCHANT, merchantBalanceLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY, merchantBalanceLog.getPaymentType());
        assertEquals(dbCancelTrans.getType(), merchantBalanceLog.getTransactionType());
        assertEquals(saleMerchantBalanceLog.getTransactionAmount().doubleValue(), merchantBalanceLog
                .getTransactionAmount().doubleValue(), 0);
        assertEquals(0.0, merchantBalanceLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.0, merchantBalanceLog.getCommissionRate().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, merchantBalanceLog.getStatus());
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

}
