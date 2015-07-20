package com.mpos.lottery.te.merchant.service.balance;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.junit.Test;

import java.math.BigDecimal;

import javax.annotation.Resource;

public class CashoutBalanceServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_DEFINITIVEVALUE}
     */
    @Test
    public void testReduceCashoutBalance_Operator() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldCashoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCashoutBalance();
        BigDecimal oldCashoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();

        // Cashout will deduct Cashout credit
        Context respCtx = new Context();
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_CASHOUT, "OPERATOR-111",
                false);

        // assert operator
        BigDecimal newCashoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();
        assertEquals(oldCashoutCreditOperator.subtract(amount).doubleValue(), newCashoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newCashoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCashoutBalance();
        assertEquals(oldCashoutCreditMerchant.doubleValue(), newCashoutCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testTopupCashoutBalance_Operator() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldCashoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCashoutBalance();
        BigDecimal oldCashoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();

        // Cashout will deduct Cashout credit
        Context respCtx = new Context();
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_CASHOUT, "OPERATOR-111",
                true);

        // assert operator
        BigDecimal newCashoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();
        assertEquals(oldCashoutCreditOperator.add(amount).doubleValue(), newCashoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newCashoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCashoutBalance();
        assertEquals(oldCashoutCreditMerchant.doubleValue(), newCashoutCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testTopupCashoutBalance_UseParent() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");

        this.jdbcTemplate.update("update operator set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        BigDecimal oldCashoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCashoutBalance();
        BigDecimal oldCashoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();

        // Cashout will deduct Cashout credit
        Context respCtx = new Context();
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_CASHOUT,
                "OPERATOR-111", true);

        Merchant targetMerchant = (Merchant) o;
        assertEquals(111, targetMerchant.getId());

        // assert operator
        BigDecimal newCashoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();
        assertEquals(oldCashoutCreditOperator.doubleValue(), newCashoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newCashoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCashoutBalance();
        assertEquals(oldCashoutCreditMerchant.add(amount).doubleValue(), newCashoutCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testReduceCashoutBalance_UseParent() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");

        this.jdbcTemplate.update("update operator set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        BigDecimal oldCashoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCashoutBalance();
        BigDecimal oldCashoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();

        // Cashout will deduct Cashout credit
        Context respCtx = new Context();
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_CASHOUT,
                "OPERATOR-111", false);

        Merchant targetMerchant = (Merchant) o;
        assertEquals(111, targetMerchant.getId());

        // assert operator
        BigDecimal newCashoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();
        assertEquals(oldCashoutCreditOperator.doubleValue(), newCashoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newCashoutCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCashoutBalance();
        assertEquals(oldCashoutCreditMerchant.subtract(amount).doubleValue(), newCashoutCreditMerchant.doubleValue(), 0);
    }

    // -------------------------------------------------------
    // SPRING DEPENDENCY INJECTION
    // -------------------------------------------------------

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

}
