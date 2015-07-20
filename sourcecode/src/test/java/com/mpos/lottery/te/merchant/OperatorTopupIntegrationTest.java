package com.mpos.lottery.te.merchant;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.merchant.web.OperatorTopupDto;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class OperatorTopupIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "operatorDao")
    private OperatorDao operatorDao;
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    @Test
    public void testTransferSaleCredit_Operator() throws Exception {

        this.jdbcTemplate.update("delete from BALANCE_TRANSACTIONS");
        printMethod();
        OperatorTopupDto dto = mockDto();
        // old credit level
        BigDecimal oldCreditOfOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCreditOfMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        // 1. make topup
        Context transferReqCtx = this.getDefaultContext(TransactionType.OPERATOR_TOPUP_VOUCHER.getRequestType(), dto);
        Context topupRespCtx = doPost(this.mockRequest(transferReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, topupRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditOfOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCreditOfMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldCreditOfOperator.add(dto.getAmount()).doubleValue(), newCreditOfOperator.doubleValue(), 0);
        assertEquals(oldCreditOfMerchant.doubleValue(), newCreditOfMerchant.doubleValue(), 0);

        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            // balane_transaction
            List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(topupRespCtx
                    .getTransactionID());
            BalanceTransactions balanceTransactions = list.get(0);
            assertEquals(1, list.size());
            assertEquals(500, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
            assertEquals("OPERATOR-111", balanceTransactions.getOwnerId());
            assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, balanceTransactions.getOwnerType());
            assertEquals(0.0, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
        }

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, topupRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(dto.getAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
        assertEquals(dto.getVoucherSerialNo(), dbTrans.getTicketSerialNo());
    }

    @Test
    public void testTransferSaleCredit_Head_Operator() throws Exception {

        this.jdbcTemplate.update("delete from BALANCE_TRANSACTIONS");
        printMethod();
        OperatorTopupDto dto = mockDto();
        dto.setOperatorId(null);

        // old credit level
        BigDecimal oldCreditOfOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCreditOfMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        // 1. make topup
        Context transferReqCtx = this.getDefaultContext(TransactionType.OPERATOR_TOPUP_VOUCHER.getRequestType(), dto);
        Context topupRespCtx = doPost(this.mockRequest(transferReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, topupRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditOfOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCreditOfMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldCreditOfOperator.add(dto.getAmount()).doubleValue(), newCreditOfOperator.doubleValue(), 0);
        assertEquals(oldCreditOfMerchant.doubleValue(), newCreditOfMerchant.doubleValue(), 0);

        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            // balane_transaction
            List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(topupRespCtx
                    .getTransactionID());
            BalanceTransactions balanceTransactions = list.get(0);
            assertEquals(1, list.size());
            assertEquals(500, balanceTransactions.getTransactionAmount().doubleValue(), 0.0);
            assertEquals("OPERATOR-111", balanceTransactions.getOwnerId());
            assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, balanceTransactions.getOwnerType());
            assertEquals(0.0, balanceTransactions.getCommissionAmount().doubleValue(), 0.0);
        }

        // assert Transaction
        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, topupRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
        assertEquals(dto.getAmount().doubleValue(), dbTrans.getTotalAmount().doubleValue(), 0);
        assertEquals(dto.getVoucherSerialNo(), dbTrans.getTicketSerialNo());
    }

    @Test
    public void testTransferSaleCredit_Operator_Cancel() throws Exception {
        printMethod();
        OperatorTopupDto dto = mockDto();

        this.jdbcTemplate.update("delete from BALANCE_TRANSACTIONS");
        // old credit level
        BigDecimal oldCreditOfOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCreditOfMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();

        // 1. make topup
        Context topupReqCtx = this.getDefaultContext(TransactionType.OPERATOR_TOPUP_VOUCHER.getRequestType(), dto);
        Context topupRespCtx = doPost(this.mockRequest(topupReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // 2. cancel
        Transaction trans = new Transaction(topupReqCtx.getTerminalId(), topupReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        this.entityManager.flush();
        this.entityManager.clear();

        // assert response
        assertEquals(200, topupRespCtx.getResponseCode());

        // assert the credit level
        BigDecimal newCreditOfOperator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCreditOfMerchant = this.getMerchantDao().findById(Merchant.class, 111l).getSaleCreditLevel();
        assertEquals(oldCreditOfOperator.doubleValue(), newCreditOfOperator.doubleValue(), 0);
        assertEquals(oldCreditOfMerchant.doubleValue(), newCreditOfMerchant.doubleValue(), 0);

        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            List<BalanceTransactions> cancellationList = balanceTransactionsDao.findBalanceTransactions(cancelRespCtx
                    .getTransactionID());
            List<BalanceTransactions> list = balanceTransactionsDao.findBalanceTransactions(topupRespCtx
                    .getTransactionID());
            BalanceTransactions balanceTransactions = list.get(0);
            BalanceTransactions cancellationBalanceTransactions = cancellationList.get(0);

            assertEquals(1, cancellationList.size());
            assertEquals(BalanceTransactions.STATUS_VALID, cancellationBalanceTransactions.getStatus());
            assertEquals(-500, cancellationBalanceTransactions.getTransactionAmount().doubleValue(), 0.0);
            assertEquals("OPERATOR-111", cancellationBalanceTransactions.getOwnerId());
            assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, cancellationBalanceTransactions.getOwnerType());
            assertEquals(0.0, cancellationBalanceTransactions.getCommissionAmount().doubleValue(), 0.0);

            assertEquals(1, list.size());
            assertEquals(BalanceTransactions.STATUS_INVALID, balanceTransactions.getStatus());
        }

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

    public static OperatorTopupDto mockDto() {
        OperatorTopupDto dto = new OperatorTopupDto();
        dto.setOperatorId("OPERATOR-111");
        dto.setAmount(new BigDecimal("500"));
        dto.setVoucherSerialNo("987654321");
        return dto;
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
