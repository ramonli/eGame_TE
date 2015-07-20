package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.web.IncomeBalanceDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class IncomeBalanceCancellationIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;
    @Resource(name = "operatorDao")
    private OperatorDao operatorDao;
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;

    @Test
    public void testCancellationIncomeBalanceTransfer_operator() throws Exception {
        printMethod();
        IncomeBalanceDto dto = new IncomeBalanceDto();
        dto.setAmount(new BigDecimal("100"));

        this.jdbcTemplate
                .update("update operator m set m.commision_balance=50,m.cashout_balance=30,m.payout_balance=40,m.sale_balance=40 where m.operator_id='OPERATOR-111'");
        this.jdbcTemplate.update("delete from BALANCE_TRANSACTIONS");

        Context reqCtx = this.getDefaultContext(TransactionType.INCOME_BALANCE_TRANSFER.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        IncomeBalanceDto respDto = (IncomeBalanceDto) respCtx.getModel();
        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        assertEquals(0.0, respDto.getPayoutBalance().doubleValue(), 0);
        assertEquals(20, respDto.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(0.0, respDto.getCommissionBalance().doubleValue(), 0.0);
        assertEquals(140, respDto.getSaleBalance().doubleValue(), 0.0);

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(reqCtx.getTerminalId());
        trans.setTraceMessageId(reqCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();
        // check operator
        Operator operator = operatorDao.findById(Operator.class, reqCtx.getOperatorId());
        assertEquals(30, operator.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(50.0, operator.getCommisionBalance().doubleValue(), 0.0);
        assertEquals(40, operator.getSaleCreditLevel().doubleValue(), 0.0);
        assertEquals(40, operator.getPayoutCreditLevel().doubleValue(), 0.0);
        // check Balance Transactions
        List<BalanceTransactions> cancellationList = balanceTransactionsDao.findBalanceTransactions(cancelRespCtx
                .getTransactionID());
        List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
        BalanceTransactions balanceTransactions = list.get(0);
        BalanceTransactions cancellationBalanceTransactions = cancellationList.get(0);

        assertEquals(1, cancellationList.size());
        assertEquals(BalanceTransactions.STATUS_VALID, cancellationBalanceTransactions.getStatus());
        assertEquals(100, cancellationBalanceTransactions.getTransactionAmount().doubleValue(), 0.0);
        assertEquals("OPERATOR-111", cancellationBalanceTransactions.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, cancellationBalanceTransactions.getOwnerType());
        assertEquals(0.0, cancellationBalanceTransactions.getCommissionAmount().doubleValue(), 0.0);

        assertEquals(1, list.size());
        assertEquals(BalanceTransactions.STATUS_INVALID, balanceTransactions.getStatus());
    }

    @Test
    public void testCancellationIncomeBalanceTransfer_merchant() throws Exception {
        printMethod();
        IncomeBalanceDto dto = new IncomeBalanceDto();
        dto.setAmount(new BigDecimal("100"));

        this.jdbcTemplate.update("update operator m set m.limit_type=4 where m.operator_id='OPERATOR-111'");
        this.jdbcTemplate
                .update("update merchant m set m.commision_balance=50,m.cashout_balance=30,m.payout_balance=40,m.sale_balance=40,m.limit_type=1 where merchant_id='111'");
        this.jdbcTemplate.update("delete from BALANCE_TRANSACTIONS");

        Context reqCtx = this.getDefaultContext(TransactionType.INCOME_BALANCE_TRANSFER.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        IncomeBalanceDto respDto = (IncomeBalanceDto) respCtx.getModel();
        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        assertEquals(0.0, respDto.getPayoutBalance().doubleValue(), 0);
        assertEquals(20, respDto.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(0.0, respDto.getCommissionBalance().doubleValue(), 0.0);
        assertEquals(140, respDto.getSaleBalance().doubleValue(), 0.0);

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(reqCtx.getTerminalId());
        trans.setTraceMessageId(reqCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();
        // check operator
        Merchant merchant = merchantDao.findById(Merchant.class, reqCtx.getMerchant().getId());
        assertEquals(30, merchant.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(50.0, merchant.getCommisionBalance().doubleValue(), 0.0);
        assertEquals(40, merchant.getSaleCreditLevel().doubleValue(), 0.0);
        assertEquals(40, merchant.getPayoutCreditLevel().doubleValue(), 0.0);

        List<BalanceTransactions> cancellationList = balanceTransactionsDao.findBalanceTransactions(cancelRespCtx
                .getTransactionID());
        List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
        BalanceTransactions balanceTransactions = list.get(0);
        BalanceTransactions cancellationBalanceTransactions = cancellationList.get(0);

        assertEquals(1, cancellationList.size());
        assertEquals(BalanceTransactions.STATUS_VALID, cancellationBalanceTransactions.getStatus());
        assertEquals(100, cancellationBalanceTransactions.getTransactionAmount().doubleValue(), 0.0);
        assertEquals("111", cancellationBalanceTransactions.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_MERCHANT, cancellationBalanceTransactions.getOwnerType());
        assertEquals(0.0, cancellationBalanceTransactions.getCommissionAmount().doubleValue(), 0.0);

        assertEquals(1, list.size());
        assertEquals(BalanceTransactions.STATUS_INVALID, balanceTransactions.getStatus());
    }

    @Test
    public void testCancellationIncomeBalanceTransfer_parentMerchant() throws Exception {
        printMethod();
        IncomeBalanceDto dto = new IncomeBalanceDto();
        dto.setAmount(new BigDecimal("100"));

        this.jdbcTemplate.update("update operator m set m.limit_type=4 where m.operator_id='OPERATOR-111'");
        this.jdbcTemplate.update("update merchant m set m.LIMIT_TYPE=4 where m.merchant_id='111'");
        this.jdbcTemplate
                .update("update merchant m set m.commision_balance=50,m.cashout_balance=30,m.payout_balance=40,m.sale_balance=40,m.limit_type=1 where merchant_id='222'");
        this.jdbcTemplate.update("delete from BALANCE_TRANSACTIONS");

        Context reqCtx = this.getDefaultContext(TransactionType.INCOME_BALANCE_TRANSFER.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        IncomeBalanceDto respDto = (IncomeBalanceDto) respCtx.getModel();
        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        assertEquals(0.0, respDto.getPayoutBalance().doubleValue(), 0);
        assertEquals(20, respDto.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(0.0, respDto.getCommissionBalance().doubleValue(), 0.0);
        assertEquals(140, respDto.getSaleBalance().doubleValue(), 0.0);

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(reqCtx.getTerminalId());
        trans.setTraceMessageId(reqCtx.getTraceMessageId());
        Context cancelCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelCtx));

        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();
        List<BalanceTransactions> cancellationList = balanceTransactionsDao.findBalanceTransactions(cancelRespCtx
                .getTransactionID());
        List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
        BalanceTransactions balanceTransactions = list.get(0);
        BalanceTransactions cancellationBalanceTransactions = cancellationList.get(0);

        assertEquals(1, cancellationList.size());
        assertEquals(BalanceTransactions.STATUS_VALID, cancellationBalanceTransactions.getStatus());
        assertEquals(100, cancellationBalanceTransactions.getTransactionAmount().doubleValue(), 0.0);
        assertEquals("222", cancellationBalanceTransactions.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_MERCHANT, cancellationBalanceTransactions.getOwnerType());
        assertEquals(0.0, cancellationBalanceTransactions.getCommissionAmount().doubleValue(), 0.0);

        assertEquals(1, list.size());
        assertEquals(BalanceTransactions.STATUS_INVALID, balanceTransactions.getStatus());
    }

    /**
     * @return balanceTransactionsDao
     */
    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    /**
     * @param balanceTransactionsDao
     */
    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }
}
