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

public class CommissionBalanceServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_DEFINITIVEVALUE}
     */
    @Test
    public void testReduceCommissionBalance_Operator() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldCommissionCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCommisionBalance();
        BigDecimal oldCommissionCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();

        // Commission will deduct Commission credit
        Context respCtx = new Context();
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_COMMISSION,
                "OPERATOR-111", false);

        Operator targetOperator = (Operator) o;
        assertEquals("OPERATOR-111", targetOperator.getId());

        // assert operator
        BigDecimal newCommissionCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        assertEquals(oldCommissionCreditOperator.subtract(amount).doubleValue(),
                newCommissionCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newCommissionCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCommisionBalance();
        assertEquals(oldCommissionCreditMerchant.doubleValue(), newCommissionCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testTopupCommissionBalance_Operator() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldCommissionCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCommisionBalance();
        BigDecimal oldCommissionCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();

        // Commission will deduct Commission credit
        Context respCtx = new Context();
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_COMMISSION,
                "OPERATOR-111", true);

        Operator targetOperator = (Operator) o;
        assertEquals("OPERATOR-111", targetOperator.getId());

        // assert operator
        BigDecimal newCommissionCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        assertEquals(oldCommissionCreditOperator.add(amount).doubleValue(), newCommissionCreditOperator.doubleValue(),
                0);
        // assert merchant
        BigDecimal newCommissionCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCommisionBalance();
        assertEquals(oldCommissionCreditMerchant.doubleValue(), newCommissionCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testTopupCommissionBalance_UseParent() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");

        this.jdbcTemplate.update("update operator set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        BigDecimal oldCommissionCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCommisionBalance();
        BigDecimal oldCommissionCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();

        // Commission will deduct Commission credit
        Context respCtx = new Context();
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_COMMISSION,
                "OPERATOR-111", true);

        Merchant targetMerchant = (Merchant) o;
        assertEquals(111, targetMerchant.getId());

        // assert operator
        BigDecimal newCommissionCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        assertEquals(oldCommissionCreditOperator.doubleValue(), newCommissionCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newCommissionCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCommisionBalance();
        assertEquals(oldCommissionCreditMerchant.add(amount).doubleValue(), newCommissionCreditMerchant.doubleValue(),
                0);
    }

    @Test
    public void testReduceCommissionBalance_UseParent() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");

        this.jdbcTemplate.update("update operator set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        BigDecimal oldCommissionCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCommisionBalance();
        BigDecimal oldCommissionCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();

        // Commission will deduct Commission credit
        Context respCtx = new Context();
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_COMMISSION,
                "OPERATOR-111", false);

        Merchant targetMerchant = (Merchant) o;
        assertEquals(111, targetMerchant.getId());

        // assert operator
        BigDecimal newCommissionCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        assertEquals(oldCommissionCreditOperator.doubleValue(), newCommissionCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newCommissionCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getCommisionBalance();
        assertEquals(oldCommissionCreditMerchant.subtract(amount).doubleValue(),
                newCommissionCreditMerchant.doubleValue(), 0);
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
