package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.web.IncomeBalanceDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class IncomeBalanceIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    @Test
    public void testIncomeBalanceTransfer_operator() throws Exception {
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
        List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
        BalanceTransactions balanceTransactions = list.get(0);
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        assertEquals(0.0, respDto.getPayoutBalance().doubleValue(), 0);
        assertEquals(20, respDto.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(0.0, respDto.getCommissionBalance().doubleValue(), 0.0);
        assertEquals(140, respDto.getSaleBalance().doubleValue(), 0.0);

        assertEquals(1, list.size());
        assertEquals(TransactionType.INCOME_BALANCE_TRANSFER.getRequestType(), balanceTransactions.getTransactionType());
        assertEquals(100, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
        assertEquals("OPERATOR-111", balanceTransactions.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, balanceTransactions.getOwnerType());
        assertEquals(0.0, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
    }

    @Test
    public void testIncomeBalanceTransfer_operator_commissionBalanceLessThanZero() throws Exception {
        printMethod();
        IncomeBalanceDto dto = new IncomeBalanceDto();
        dto.setAmount(new BigDecimal("100"));

        this.jdbcTemplate
                .update("update operator m set m.commision_balance=-50,m.cashout_balance=70,m.payout_balance=40,m.sale_balance=40 where m.operator_id='OPERATOR-111'");
        this.jdbcTemplate.update("delete from BALANCE_TRANSACTIONS");

        Context reqCtx = this.getDefaultContext(TransactionType.INCOME_BALANCE_TRANSFER.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        IncomeBalanceDto respDto = (IncomeBalanceDto) respCtx.getModel();
        // force to flush to underlying database to avoid FALSE POSITIVE
        this.entityManager.flush();
        this.entityManager.clear();
        List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
        BalanceTransactions balanceTransactions = list.get(0);
        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        assertEquals(0.0, respDto.getPayoutBalance().doubleValue(), 0);
        assertEquals(10.0, respDto.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(-50.0, respDto.getCommissionBalance().doubleValue(), 0.0);
        assertEquals(140.0, respDto.getSaleBalance().doubleValue(), 0.0);

        assertEquals(1, list.size());
        assertEquals(TransactionType.INCOME_BALANCE_TRANSFER.getRequestType(), balanceTransactions.getTransactionType());
        assertEquals(100, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
        assertEquals("OPERATOR-111", balanceTransactions.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, balanceTransactions.getOwnerType());
        assertEquals(0.0, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
    }

    @Test
    public void testIncomeBalanceTransfer_merchant() throws Exception {
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

        List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
        BalanceTransactions balanceTransactions = list.get(0);

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        assertEquals(0.0, respDto.getPayoutBalance().doubleValue(), 0);
        assertEquals(20, respDto.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(0.0, respDto.getCommissionBalance().doubleValue(), 0.0);
        assertEquals(140, respDto.getSaleBalance().doubleValue(), 0.0);

        assertEquals(1, list.size());
        assertEquals(100, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
        assertEquals("111", balanceTransactions.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_MERCHANT, balanceTransactions.getOwnerType());
        assertEquals(0.0, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
    }

    @Test
    public void testIncomeBalanceTransfer_parentMerchant() throws Exception {
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

        List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(respCtx.getTransactionID());
        BalanceTransactions balanceTransactions = list.get(0);

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        assertEquals(0.0, respDto.getPayoutBalance().doubleValue(), 0);
        assertEquals(20, respDto.getCashoutBalance().doubleValue(), 0.0);
        assertEquals(0.0, respDto.getCommissionBalance().doubleValue(), 0.0);
        assertEquals(140, respDto.getSaleBalance().doubleValue(), 0.0);

        assertEquals(1, list.size());
        assertEquals(100, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
        assertEquals("222", balanceTransactions.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_MERCHANT, balanceTransactions.getOwnerType());
        assertEquals(0.0, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
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
}
