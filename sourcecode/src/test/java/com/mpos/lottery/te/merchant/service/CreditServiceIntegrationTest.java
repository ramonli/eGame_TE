package com.mpos.lottery.te.merchant.service;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;

import java.math.BigDecimal;

import javax.annotation.Resource;

public class CreditServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "merchantService")
    private MerchantService merchantService;
    @Resource(name = "creditService")
    private CreditService creditService;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_DEFINITIVEVALUE}
     */
    @Test
    public void testReduceCredit_Sale() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldSaleCreditMerchant = this.getMerchantService().getMerchant(111).getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // sale will deduct sale credit
        this.getCreditService().credit("OPERATOR-111", 111, amount, null, false, true, false);

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.subtract(amount).doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant = this.getMerchantService().getMerchant(111).getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testReduceCredit_Cashout() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldPayoutCreditMerchant = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // sale will deduct sale credit
        this.getCreditService().credit("OPERATOR-111", 111, amount, true, false);

        // assert operator
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldPayoutCreditOperator.add(amount).doubleValue(), newPayoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newPayoutCreditMerchant = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        assertEquals(oldPayoutCreditMerchant.doubleValue(), newPayoutCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void testReduceCredit_Cashout_Reversal() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldPayoutCreditMerchant = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // sale will deduct sale credit
        this.getCreditService().credit("OPERATOR-111", 111, amount, false, false);

        // assert operator
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldPayoutCreditOperator.subtract(amount).doubleValue(), newPayoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newPayoutCreditMerchant = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        assertEquals(oldPayoutCreditMerchant.doubleValue(), newPayoutCreditMerchant.doubleValue(), 0);
    }

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_DEFINITIVEVALUE}
     */
    @Test
    public void testRestoreCredit_Sale() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldSaleCreditMerchant = this.getMerchantService().getMerchant(111).getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // cancellation of sale will topup sale credit
        this.getCreditService().credit("OPERATOR-111", 111, amount, null, true, true, false);

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.add(amount).doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant = this.getMerchantService().getMerchant(111).getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant.doubleValue(), newSaleCreditMerchant.doubleValue(), 0);
    }

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_USEPARENT}
     */
    @Test
    public void testReduceCredit_Sale_Operator() throws Exception {
        this.printMethod();

        this.jdbcTemplate.update("update OPERATOR set LIMIT_TYPE=4 where OPERATOR_ID='OPERATOR-111'");
        this.jdbcTemplate.update("update MERCHANT set LIMIT_TYPE=4 where MERCHANT_ID=111");

        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldSaleCreditMerchant111 = this.getMerchantService().getMerchant(111).getSaleCreditLevel();
        BigDecimal oldSaleCreditMerchant222 = this.getMerchantService().getMerchant(222).getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // sale will deduct sale credit
        this.getCreditService().credit("OPERATOR-111", 111, amount, null, false, true, false);

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant111 = this.getMerchantService().getMerchant(111).getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant111.doubleValue(), newSaleCreditMerchant111.doubleValue(), 0);
        BigDecimal newSaleCreditMerchant222 = this.getMerchantService().getMerchant(222).getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant222.subtract(amount).doubleValue(), newSaleCreditMerchant222.doubleValue(), 0);
    }

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_USEPARENT}
     */
    @Test
    public void testRestoreCredit_Sale_Operator() throws Exception {
        this.printMethod();

        this.jdbcTemplate.update("update OPERATOR set LIMIT_TYPE=4 where OPERATOR_ID='OPERATOR-111'");
        this.jdbcTemplate.update("update MERCHANT set LIMIT_TYPE=4 where MERCHANT_ID=111");

        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldSaleCreditMerchant111 = this.getMerchantService().getMerchant(111).getSaleCreditLevel();
        BigDecimal oldSaleCreditMerchant222 = this.getMerchantService().getMerchant(222).getSaleCreditLevel();
        BigDecimal oldSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        // sale cancellation will deduct sale credit
        this.getCreditService().credit("OPERATOR-111", 111, amount, null, true, true, false);

        // assert operator
        BigDecimal newSaleCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(oldSaleCreditOperator.doubleValue(), newSaleCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newSaleCreditMerchant111 = this.getMerchantService().getMerchant(111).getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant111.doubleValue(), newSaleCreditMerchant111.doubleValue(), 0);
        BigDecimal newSaleCreditMerchant222 = this.getMerchantService().getMerchant(222).getSaleCreditLevel();
        assertEquals(oldSaleCreditMerchant222.add(amount).doubleValue(), newSaleCreditMerchant222.doubleValue(), 0);
    }

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_DEFINITIVEVALUE}
     */
    @Test
    public void testRestoreCredit_Payout() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldPayoutCreditMerchant = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // payout will restore payout_balance
        this.getCreditService().credit("OPERATOR-111", 111, amount, null, true, false, false);

        // assert operator
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldPayoutCreditOperator.add(amount).doubleValue(), newPayoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newPayoutCreditMerchant = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        assertEquals(oldPayoutCreditMerchant.doubleValue(), newPayoutCreditMerchant.doubleValue(), 0);
    }

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_DEFINITIVEVALUE}
     */
    @Test
    public void testReduceCredit_Payout() throws Exception {
        this.printMethod();
        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldPayoutCreditMerchant = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // cancellation of payout will restore payout_balance
        this.getCreditService().credit("OPERATOR-111", 111, amount, null, false, false, false);

        // assert operator
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldPayoutCreditOperator.subtract(amount).doubleValue(), newPayoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newPayoutCreditMerchant = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        assertEquals(oldPayoutCreditMerchant.doubleValue(), newPayoutCreditMerchant.doubleValue(), 0);
    }

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_USEPARENT}
     */
    @Test
    public void testRestoreCredit_Payout_UseParent() throws Exception {
        this.printMethod();

        this.jdbcTemplate.update("update OPERATOR set LIMIT_TYPE=4 where OPERATOR_ID='OPERATOR-111'");
        this.jdbcTemplate.update("update MERCHANT set LIMIT_TYPE=4 where MERCHANT_ID=111");

        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldPayoutCreditMerchant111 = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        BigDecimal oldPayoutCreditMerchant222 = this.getMerchantService().getMerchant(222).getPayoutCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // payout will restore payout_balance
        this.getCreditService().credit("OPERATOR-111", 111, amount, null, true, false, false);

        // assert operator
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldPayoutCreditOperator.doubleValue(), newPayoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newPayoutCreditMerchant111 = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        assertEquals(oldPayoutCreditMerchant111.doubleValue(), newPayoutCreditMerchant111.doubleValue(), 0);
        BigDecimal newPayoutCreditMerchant222 = this.getMerchantService().getMerchant(222).getPayoutCreditLevel();
        assertEquals(oldPayoutCreditMerchant222.add(amount).doubleValue(), newPayoutCreditMerchant222.doubleValue(), 0);
    }

    /**
     * The credit type of operator is {@link Merchant#CREDIT_TYPE_USEPARENT}
     */
    @Test
    public void testReduceCredit_Payout_UseParent() throws Exception {
        this.printMethod();

        this.jdbcTemplate.update("update OPERATOR set LIMIT_TYPE=4 where OPERATOR_ID='OPERATOR-111'");
        this.jdbcTemplate.update("update MERCHANT set LIMIT_TYPE=4 where MERCHANT_ID=111");

        BigDecimal amount = new BigDecimal("200");
        BigDecimal oldPayoutCreditMerchant111 = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        BigDecimal oldPayoutCreditMerchant222 = this.getMerchantService().getMerchant(222).getPayoutCreditLevel();
        BigDecimal oldPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();

        // cancellation of payout will reduce payout_balance
        this.getCreditService().credit("OPERATOR-111", 111, amount, null, false, false, false);

        // assert operator
        BigDecimal newPayoutCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        assertEquals(oldPayoutCreditOperator.doubleValue(), newPayoutCreditOperator.doubleValue(), 0);
        // assert merchant
        BigDecimal newPayoutCreditMerchant111 = this.getMerchantService().getMerchant(111).getPayoutCreditLevel();
        assertEquals(oldPayoutCreditMerchant111.doubleValue(), newPayoutCreditMerchant111.doubleValue(), 0);
        BigDecimal newPayoutCreditMerchant222 = this.getMerchantService().getMerchant(222).getPayoutCreditLevel();
        assertEquals(oldPayoutCreditMerchant222.subtract(amount).doubleValue(),
                newPayoutCreditMerchant222.doubleValue(), 0);
    }

    // -------------------------------------------------------
    // SPRING DEPENDENCY INJECTION
    // -------------------------------------------------------

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

}
