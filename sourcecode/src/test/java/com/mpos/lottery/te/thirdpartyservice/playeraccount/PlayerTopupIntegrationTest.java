package com.mpos.lottery.te.thirdpartyservice.playeraccount;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerTopupDto;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class PlayerTopupIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    @Rollback(true)
    @Test
    public void test_Sell_OK() throws Exception {
        this.printMethod();
        PlayerTopupDto reqDto = PlayerDomainMocker.mockVoucherTopup();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        Context reqCtx = this.getDefaultContext(TransactionType.PLAYER_TOPUP.getRequestType(), reqDto);
        Context respCtx = doPost(this.mockRequest(reqCtx));

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // -------------------------------------
        // assert sale balance
        // -------------------------------------
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        assertEquals(newCreditOperator.add(reqDto.getAmount()).doubleValue(), oldCreditOperator.doubleValue(), 0);
        assertEquals(newCreditMerchant.doubleValue(), oldCreditMerchant.doubleValue(), 0);
        // commission balance
        assertEquals(newCommBalance.doubleValue(), oldCommBalance.add(new BigDecimal("6")).doubleValue(), 0);

        // -------------------------------------
        // assert db general transaction
        // -------------------------------------
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(null);
        expectTrans.setTotalAmount(reqDto.getAmount());
        expectTrans.setTicketSerialNo(reqDto.getVoucherSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);
        assertEquals(reqDto.getAccountId(), dbTrans.getVirn());

        // assert commission
        List<BalanceTransactions> commissionLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                respCtx.getTransactionID());
        assertEquals(1, commissionLogs.size());
        for (BalanceTransactions commissionLog : commissionLogs) {
            assertEquals(dbTrans.getId(), commissionLog.getTeTransactionId());
            assertEquals(TransactionType.PLAYER_TOPUP.getRequestType(), commissionLog.getTransactionType());
            assertEquals(TransactionType.PLAYER_TOPUP.getRequestType(), commissionLog.getOriginalTransType());
            assertEquals(dbTrans.getOperatorId(), commissionLog.getOperatorId());
            assertEquals(dbTrans.getMerchantId(), commissionLog.getMerchantId());
            assertEquals(dbTrans.getDeviceId(), commissionLog.getDeviceId());
            assertEquals(dbTrans.getOperatorId(), commissionLog.getOwnerId());
            assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, commissionLog.getOwnerType());
            assertEquals(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, commissionLog.getPaymentType());
            assertEquals(dbTrans.getType(), commissionLog.getTransactionType());
            assertEquals(6.0, commissionLog.getCommissionAmount().doubleValue(), 0);
            assertEquals(0.1, commissionLog.getCommissionRate().doubleValue(), 0);
            assertEquals(dbTrans.getTotalAmount().doubleValue(), commissionLog.getTransactionAmount().doubleValue(), 0);
            assertEquals(BalanceTransactions.STATUS_VALID, commissionLog.getStatus());
        }
    }

    @Rollback(true)
    @Test
    public void test_Cancellation() throws Exception {
        this.printMethod();
        PlayerTopupDto topupReqDto = PlayerDomainMocker.mockVoucherTopup();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        // 1. topup first
        Context topupReqCtx = this.getDefaultContext(TransactionType.PLAYER_TOPUP.getRequestType(), topupReqDto);
        Context topupRespCtx = doPost(this.mockRequest(topupReqCtx));

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // 2. cancel
        Transaction trans = new Transaction();
        trans.setDeviceId(topupReqCtx.getTerminalId());
        trans.setTraceMessageId(topupReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());

        // -------------------------------------
        // assert sale balance
        // -------------------------------------
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        assertEquals(newCreditOperator.doubleValue(), oldCreditOperator.doubleValue(), 0);
        assertEquals(newCreditMerchant.doubleValue(), oldCreditMerchant.doubleValue(), 0);
        // commission balance
        assertEquals(newCommBalance.doubleValue(), oldCommBalance.doubleValue(), 0);

        // -------------------------------------
        // assert db general transaction
        // -------------------------------------
        Transaction dbTopupTrans = this.getBaseJpaDao().findById(Transaction.class, topupRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbTopupTrans.getResponseCode());
        assertEquals(cancelRespCtx.getTransactionID(), dbTopupTrans.getCancelTransactionId());
        assertEquals(cancelReqCtx.getTransType(), dbTopupTrans.getCancelTransactionType().intValue());

        Transaction dbCancelTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId(null);
        expectTrans.setTotalAmount(topupReqDto.getAmount());
        expectTrans.setTicketSerialNo(topupReqDto.getVoucherSerialNo());
        expectTrans.setOperatorId(cancelReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelReqCtx.getTraceMessageId());
        expectTrans.setType(cancelReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbCancelTrans);
        assertEquals(topupReqCtx.getTransType(), dbCancelTrans.getCancelTransactionType().intValue());
        assertEquals(dbTopupTrans.getId(), dbCancelTrans.getCancelTransactionId());

        // -------------------------------------
        // assert db balance transaction
        // -------------------------------------
        // assert original sale commission logs
        List<BalanceTransactions> commissionLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                topupRespCtx.getTransactionID());
        assertEquals(1, commissionLogs.size());
        for (BalanceTransactions commissionLog : commissionLogs) {
            assertEquals(BalanceTransactions.STATUS_INVALID, commissionLog.getStatus());
        }
        // cancellation of sale commission log
        BalanceTransactions cancelCommLog = this.getBalanceTransactionsDao()
                .findByTransactionAndOwnerAndGameAndOrigTransType(cancelRespCtx.getTransactionID(),
                        cancelRespCtx.getOperatorId(), null, TransactionType.PLAYER_TOPUP.getRequestType());
        assertEquals(dbCancelTrans.getOperatorId(), cancelCommLog.getOperatorId());
        assertEquals(dbCancelTrans.getMerchantId(), cancelCommLog.getMerchantId());
        assertEquals(dbCancelTrans.getDeviceId(), cancelCommLog.getDeviceId());
        assertEquals(dbCancelTrans.getOperatorId(), cancelCommLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, cancelCommLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY, cancelCommLog.getPaymentType());
        assertEquals(dbCancelTrans.getType(), cancelCommLog.getTransactionType());
        assertEquals(-6.0, cancelCommLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.1, cancelCommLog.getCommissionRate().doubleValue(), 0);
        assertEquals(topupReqDto.getAmount().doubleValue(), cancelCommLog.getTransactionAmount().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, cancelCommLog.getStatus());
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
