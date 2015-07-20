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

public class SaleBalanceServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_DEFINITIVEVALUE}
     */
    @Test
    public void testReduceSaleBalance_Operator() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // sale will deduct sale credit
        Context respCtx = new Context();
        respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, false);
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_SALE, "OPERATOR-111", false);

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.subtract(amount).doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
    }

    /**
     * No need to calculate balance if ticket is sold by credit card
     */
    @Test
    public void testReduceSaleBalance_Operator_SaleByCreditCard() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");

        // this SQL must be executed before findById(Mercant.class...), as JPA
        // entity manager will cache those entities found by ID
        this.jdbcTemplate.update("update merchant set credit_card_need=0");

        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // sale will deduct sale credit
        Context respCtx = new Context();
        respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, true);
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_SALE,
                "OPERATOR-111", false);

        Operator targetOperator = (Operator) o;
        assertEquals("OPERATOR-111", targetOperator.getId());

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testTopupSaleBalance_Operator() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // sale will deduct sale credit
        Context respCtx = new Context();
        respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, false);
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_SALE, "OPERATOR-111", true);

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.add(amount).doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testTopupSaleBalance_UseParent() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");

        this.jdbcTemplate.update("update operator set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // sale will deduct sale credit
        Context respCtx = new Context();
        respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, false);
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_SALE,
                "OPERATOR-111", true);

        Merchant targetMerchant = (Merchant) o;
        assertEquals(111, targetMerchant.getId());

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant.add(amount).doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testReduceSaleBalance_UseParent() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");

        this.jdbcTemplate.update("update operator set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        BigDecimal oldSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // sale will deduct sale credit
        Context respCtx = new Context();
        respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, false);
        Transaction targetTrans = new Transaction();
        targetTrans.setTotalAmount(amount);
        Object o = this.getBalanceService().balance(respCtx, targetTrans, BalanceService.BALANCE_TYPE_SALE,
                "OPERATOR-111", false);

        Merchant targetMerchant = (Merchant) o;
        assertEquals(111, targetMerchant.getId());

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111l, false)
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant.subtract(amount).doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
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
