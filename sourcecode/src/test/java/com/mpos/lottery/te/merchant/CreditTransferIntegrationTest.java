package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.web.CreditTransferDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;

import javax.annotation.Resource;

public class CreditTransferIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "operatorDao")
    private OperatorDao operatorDao;
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;

    @Test
    public void testTransferSaleCredit_Operator() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getSaleCreditLevel();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertEquals(oldCreditFromOperator.subtract(dto.getAmount()).doubleValue(), respDto
                .getCreditBalanceOfFromOperator().doubleValue(), 0);
        assertEquals(oldCreditToOperator.add(dto.getAmount()).doubleValue(), respDto.getCreditBalanceOfToOperator()
                .doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getSaleCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getSaleCreditLevel();
        assertEquals(oldCreditFromOperator.subtract(dto.getAmount()).doubleValue(),
                newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.doubleValue(), newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.add(dto.getAmount()).doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(TransactionType.TRANSFER_CREDIT.getRequestType(), dbTrans.getType());
        assertEquals(dto.getAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
        // virn
        assertEquals(String.valueOf(dto.getCreditType()), dbTrans.getVirn());
    }

    @Test
    public void testTransferSaleCredit_Operator_UseParent() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();

        this.jdbcTemplate.update("update OPERATOR set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getSaleCreditLevel();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertEquals(oldCreditFromMerchant.subtract(dto.getAmount()).doubleValue(), respDto
                .getCreditBalanceOfFromOperator().doubleValue(), 0);
        assertEquals(oldCreditToMerchant.add(dto.getAmount()).doubleValue(), respDto.getCreditBalanceOfToOperator()
                .doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getSaleCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getSaleCreditLevel();
        assertEquals(oldCreditFromOperator.doubleValue(), newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.subtract(dto.getAmount()).doubleValue(),
                newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.add(dto.getAmount()).doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(TransactionType.TRANSFER_CREDIT.getRequestType(), dbTrans.getType());
        assertEquals(dto.getAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
    }

    @Test
    public void testTransferPayoutCredit_Operator() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();
        dto.setCreditType(CreditTransferDto.CREDITTYPE_PAYOUT);

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertEquals(oldCreditFromOperator.subtract(dto.getAmount()).doubleValue(), respDto
                .getCreditBalanceOfFromOperator().doubleValue(), 0);
        assertEquals(oldCreditToOperator.add(dto.getAmount()).doubleValue(), respDto.getCreditBalanceOfToOperator()
                .doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();
        assertEquals(oldCreditFromOperator.subtract(dto.getAmount()).doubleValue(),
                newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.doubleValue(), newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.add(dto.getAmount()).doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(TransactionType.TRANSFER_CREDIT.getRequestType(), dbTrans.getType());
        assertEquals(dto.getAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
    }

    @Test
    public void testTransferPayoutCredit_Operator_UseParent() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();
        dto.setCreditType(CreditTransferDto.CREDITTYPE_PAYOUT);

        this.jdbcTemplate.update("update OPERATOR set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        assertEquals(200, respCtx.getResponseCode());
        assertEquals(oldCreditFromMerchant.subtract(dto.getAmount()).doubleValue(), respDto
                .getCreditBalanceOfFromOperator().doubleValue(), 0);
        assertEquals(oldCreditToMerchant.add(dto.getAmount()).doubleValue(), respDto.getCreditBalanceOfToOperator()
                .doubleValue(), 0);

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();
        assertEquals(oldCreditFromOperator.doubleValue(), newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.subtract(dto.getAmount()).doubleValue(),
                newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.add(dto.getAmount()).doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(TransactionType.TRANSFER_CREDIT.getRequestType(), dbTrans.getType());
        assertEquals(dto.getAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
    }

    /**
     * For example, payout balance of source operator is 1000, however client request to transfer 1001.
     */
    @Test
    public void testTransferPayoutCredit_Operator_ExceedsLimit() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();
        dto.setAmount(new BigDecimal("1001"));
        dto.setCreditType(CreditTransferDto.CREDITTYPE_PAYOUT);

        this.jdbcTemplate.update("update operator set PAYOUT_BALANCE=-999");

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        // assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();
        assertEquals(oldCreditFromOperator.subtract(dto.getAmount()).doubleValue(),
                newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.doubleValue(), newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.add(dto.getAmount()).doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        // assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(TransactionType.TRANSFER_CREDIT.getRequestType(), dbTrans.getType());
    }

    @Test
    public void testTransferCommissionCredit_Operator() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();
        dto.setCreditType(CreditTransferDto.CREDITTYPE_COMMISSION);

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        assertEquals(SystemException.CODE_INSUFFICIENT_BALANCE, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getCommisionBalance();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getCommisionBalance();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getCommisionBalance();

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(TransactionType.TRANSFER_CREDIT.getRequestType(), dbTrans.getType());
    }

    @Test
    public void testTransferCommissionCredit_Operator_UseParent() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();
        dto.setCreditType(CreditTransferDto.CREDITTYPE_COMMISSION);

        this.jdbcTemplate.update("update OPERATOR set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getCommisionBalance();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getCommisionBalance();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getCommisionBalance();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        assertEquals(SystemException.CODE_INSUFFICIENT_BALANCE, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getCommisionBalance();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getCommisionBalance();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getCommisionBalance();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getCommisionBalance();

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(TransactionType.TRANSFER_CREDIT.getRequestType(), dbTrans.getType());
    }

    @Test
    public void testTransferCashoutCredit_Operator() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();
        dto.setCreditType(CreditTransferDto.CREDITTYPE_CASHOUT);

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getCashoutBalance();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getCashoutBalance();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getCashoutBalance();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        assertEquals(SystemException.CODE_INSUFFICIENT_BALANCE, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getCashoutBalance();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getCashoutBalance();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getCashoutBalance();

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
    }

    @Test
    public void testTransferCashoutCredit_Operator_UseParent() throws Exception {
        printMethod();
        CreditTransferDto dto = mockDto();
        dto.setCreditType(CreditTransferDto.CREDITTYPE_CASHOUT);

        this.jdbcTemplate.update("update OPERATOR set LIMIT_TYPE=" + Merchant.CREDIT_TYPE_USE_PARENT);

        // old credit level
        BigDecimal oldCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();
        BigDecimal oldCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getCashoutBalance();
        BigDecimal oldCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getCashoutBalance();
        BigDecimal oldCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getCashoutBalance();

        Context reqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context respCtx = doPost(this.mockRequest(reqCtx));
        CreditTransferDto respDto = (CreditTransferDto) respCtx.getModel();

        // assert response
        assertEquals(SystemException.CODE_INSUFFICIENT_BALANCE, respCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getCashoutBalance();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getCashoutBalance();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getCashoutBalance();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getCashoutBalance();

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(TransactionType.TRANSFER_CREDIT.getRequestType(), dbTrans.getType());
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public static CreditTransferDto mockDto() {
        CreditTransferDto dto = new CreditTransferDto();
        dto.setFromOperatorLoginName("OPERATOR-LOGIN");
        dto.setToOperatorLoginName("OPERATOR-LOGIN-2");
        dto.setCreditType(CreditTransferDto.CREDITTYPE_SALE);
        dto.setAmount(new BigDecimal("500"));
        return dto;
    }
}
