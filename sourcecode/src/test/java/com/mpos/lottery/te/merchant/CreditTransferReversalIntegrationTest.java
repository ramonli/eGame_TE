package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.CreditTransferLogDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.CreditTransferLog;
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

public class CreditTransferReversalIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "operatorDao")
    private OperatorDao operatorDao;
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "creditTransferLogDao")
    private CreditTransferLogDao creditTransferLogDao;

    //@Rollback(false)
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

        // 1. make credit transfer
        Context transferReqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context transferRespCtx = doPost(this.mockRequest(transferReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        Transaction trans = new Transaction(transferReqCtx.getTerminalId(), transferReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        // assert response
        assertEquals(200, transferRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getSaleCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getSaleCreditLevel();
        assertEquals(oldCreditFromOperator.doubleValue(), newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.doubleValue(), newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(dto.getAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);

        // assert creditTransferLog
        CreditTransferLog log = this.getCreditTransferLogDao().findByTransactionId(transferRespCtx.getTransactionID());
        assertEquals(CreditTransferLog.STATUS_REVERSAL, log.getStatus());
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

        // 1. make credit transfer
        Context transferReqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context transferRespCtx = doPost(this.mockRequest(transferReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        Transaction trans = new Transaction(transferReqCtx.getTerminalId(), transferReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getSaleCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getSaleCreditLevel();
        assertEquals(oldCreditFromOperator.doubleValue(), newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.doubleValue(), newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
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

        // 1. make credit transfer
        Context transferReqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context transferRespCtx = doPost(this.mockRequest(transferReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        Transaction trans = new Transaction(transferReqCtx.getTerminalId(), transferReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();
        assertEquals(oldCreditFromOperator.doubleValue(), newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.doubleValue(), newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
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

        // 1. make credit transfer
        Context transferReqCtx = this.getDefaultContext(TransactionType.TRANSFER_CREDIT.getRequestType(), dto);
        Context transferRespCtx = doPost(this.mockRequest(transferReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        Transaction trans = new Transaction(transferReqCtx.getTerminalId(), transferReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        // assert response
        assertEquals(200, cancelRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditFromOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getPayoutCreditLevel();
        BigDecimal newCreditFromMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getPayoutCreditLevel();
        BigDecimal newCreditToOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-112")
                .getPayoutCreditLevel();
        BigDecimal newCreditToMerchant = this.getMerchantDao().findById(Merchant.class, 112l).getPayoutCreditLevel();
        assertEquals(oldCreditFromOperator.doubleValue(), newCreditFromOperator.doubleValue(), 0);
        assertEquals(oldCreditFromMerchant.doubleValue(), newCreditFromMerchant.doubleValue(), 0);
        assertEquals(oldCreditToOperator.doubleValue(), newCreditToOperator.doubleValue(), 0);
        assertEquals(oldCreditToMerchant.doubleValue(), newCreditToMerchant.doubleValue(), 0);

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(dto.getAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
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

    public CreditTransferLogDao getCreditTransferLogDao() {
        return creditTransferLogDao;
    }

    public void setCreditTransferLogDao(CreditTransferLogDao creditTransferLogDao) {
        this.creditTransferLogDao = creditTransferLogDao;
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
