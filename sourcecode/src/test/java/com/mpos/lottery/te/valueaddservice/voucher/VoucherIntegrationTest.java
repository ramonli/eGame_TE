package com.mpos.lottery.te.valueaddservice.voucher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherSaleDao;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherStatisticsDao;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class VoucherIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "jpaVoucherSaleDao")
    private VoucherSaleDao voucherSaleDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;
    @Resource(name = "jpaVoucherStatDao")
    private VoucherStatisticsDao voucherStatDao;

    @Rollback(true)
    @Test
    public void test_Sell_OK() throws Exception {
        printMethod();
        Voucher reqDto = VoucherDomainMocker.mockVoucherTopup();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        Voucher respDto = (Voucher) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(reqDto.getFaceAmount().doubleValue(), respDto.getFaceAmount().doubleValue(), 0);
        assertEquals("V-1", respDto.getSerialNo());
        assertEquals("V-1-PIN", respDto.getPlainPin());
        assertTrue(respDto.getExpireDate() != null);
        assertEquals(reqDto.getGame().getId(), respDto.getGame().getId());
        assertEquals(reqDto.getGame().getId(), respDto.getGame().getId());

        // -------------------------------------
        // assert sale balance
        // -------------------------------------
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        assertEquals(newCreditOperator.add(reqDto.getFaceAmount()).doubleValue(), oldCreditOperator.doubleValue(), 0);
        assertEquals(newCreditMerchant.doubleValue(), oldCreditMerchant.doubleValue(), 0);
        // commission balance
        assertEquals(newCommBalance.doubleValue(), oldCommBalance.add(new BigDecimal("3")).doubleValue(), 0);

        // -------------------------------------
        // assert db general transaction
        // -------------------------------------
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(reqDto.getGame().getId());
        expectTrans.setTotalAmount(reqDto.getFaceAmount());
        expectTrans.setTicketSerialNo(respDto.getSerialNo());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // -------------------------------------
        // assert remaining voucher
        // -------------------------------------
        VoucherStatistics voucherStat = this.getVoucherStatDao().findByGameAndFaceAmount(reqDto.getFaceAmount(),
                reqDto.getGame().getId());
        assertEquals(99, voucherStat.getRemainCount());

        // -------------------------------------
        // assert db voucher and sale transaction
        // -------------------------------------
        VoucherSale dbSale = this.getVoucherSaleDao().findByTransaction(respCtx.getTransactionID());
        assertTrue(dbSale.getId() != null);
        assertEquals(reqDto.getFaceAmount().doubleValue(), dbSale.getVoucherFaceAmount().doubleValue(), 0);
        assertEquals(reqDto.getGame().getId(), dbSale.getGame().getId());
        assertEquals(VoucherSale.STATUS_SUCCESS, dbSale.getStatus());
        assertEquals(dbTrans.getDeviceId(), dbSale.getDevId());
        assertEquals(dbTrans.getOperatorId(), dbSale.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), dbSale.getMerchantId());
        assertEquals(dbTrans.getId(), dbSale.getTransaction().getId());
        assertEquals(respDto.getSerialNo(), dbSale.getVoucherSerialNo());

        Voucher dbVoucher = this.getBaseJpaDao().findById(Voucher.class, dbSale.getVoucherId());
        assertEquals(Voucher.STATUS_SOLD, dbVoucher.getStatus());
        assertEquals(reqDto.getFaceAmount().doubleValue(), dbVoucher.getFaceAmount().doubleValue(), 0);
        assertEquals(reqDto.getGame().getId(), dbVoucher.getGame().getId());

        // -------------------------------------
        // assert db balance transaction
        // -------------------------------------
        List<BalanceTransactions> balanceLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                respCtx.getTransactionID());
        assertEquals(1, balanceLogs.size());
        BalanceTransactions operatorBalanceLog = balanceLogs.get(0);
        assertEquals(dbTrans.getOperatorId(), operatorBalanceLog.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), operatorBalanceLog.getMerchantId());
        assertEquals(dbTrans.getDeviceId(), operatorBalanceLog.getDeviceId());
        assertEquals(dbTrans.getOperatorId(), operatorBalanceLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, operatorBalanceLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_DEDUCTING_MONEY, operatorBalanceLog.getPaymentType());
        assertEquals(dbTrans.getType(), operatorBalanceLog.getTransactionType());
        assertEquals(dbTrans.getType(), operatorBalanceLog.getOriginalTransType());
        assertEquals(dbTrans.getTotalAmount().doubleValue(), operatorBalanceLog.getTransactionAmount().doubleValue(), 0);
        // 60*0.05
        assertEquals(3.0, operatorBalanceLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.05, operatorBalanceLog.getCommissionRate().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, operatorBalanceLog.getStatus());
    }

    @Rollback(true)
    @Test
    public void test_Cancellation() throws Exception {
        printMethod();
        Voucher saleReqDto = VoucherDomainMocker.mockVoucherTopup();
        
        VoucherStatistics voucherStat = this.getVoucherStatDao().findByGameAndFaceAmount(saleReqDto.getFaceAmount(),
                saleReqDto.getGame().getId());
        int oldRemainCount = voucherStat.getRemainCount();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        // 1. make vouche sale
        Context saleReqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), saleReqDto);
        saleReqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context saleRespCtx = doPost(this.mockRequest(saleReqCtx));
        Voucher saleRespDto = (Voucher) saleRespCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // 2. make cancellation
        Transaction trans = new Transaction();
        trans.setDeviceId(saleReqCtx.getTerminalId());
        trans.setTraceMessageId(saleReqCtx.getTraceMessageId());
        Context cancelReqCtx = this.getDefaultContext(TransactionType.CANCEL_BY_TRANSACTION.getRequestType(), trans);
        Context cancelRespCtx = doPost(this.mockRequest(cancelReqCtx));

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        assertEquals(SystemException.CODE_OK, cancelRespCtx.getResponseCode());
        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        
        // -------------------------------------
        // assert remaining voucher
        // -------------------------------------
        voucherStat = this.getVoucherStatDao().findByGameAndFaceAmount(saleReqDto.getFaceAmount(),
                saleReqDto.getGame().getId());
        assertEquals(oldRemainCount, voucherStat.getRemainCount());

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
        Transaction dbSaleTrans = this.getBaseJpaDao().findById(Transaction.class, saleRespCtx.getTransactionID());
        assertEquals(SystemException.CODE_CANCELLED_TRANS, dbSaleTrans.getResponseCode());
        assertEquals(cancelRespCtx.getTransactionID(), dbSaleTrans.getCancelTransactionId());
        assertEquals(cancelReqCtx.getTransType(), dbSaleTrans.getCancelTransactionType().intValue());

        Transaction dbCancelTrans = this.getBaseJpaDao().findById(Transaction.class, cancelRespCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(cancelRespCtx.getTransactionID());
        expectTrans.setGameId(saleReqDto.getGame().getId());
        expectTrans.setTotalAmount(saleReqDto.getFaceAmount());
        expectTrans.setTicketSerialNo(saleRespDto.getSerialNo());
        expectTrans.setOperatorId(cancelReqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(cancelReqCtx.getTerminalId());
        expectTrans.setTraceMessageId(cancelReqCtx.getTraceMessageId());
        expectTrans.setType(cancelReqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbCancelTrans);
        assertEquals(saleReqCtx.getTransType(), dbCancelTrans.getCancelTransactionType().intValue());
        assertEquals(dbSaleTrans.getId(), dbCancelTrans.getCancelTransactionId());

        // -------------------------------------
        // assert db voucher and sale transaction
        // -------------------------------------
        VoucherSale dbSale = this.getVoucherSaleDao().findByTransaction(saleRespCtx.getTransactionID());
        assertTrue(dbSale.getId() != null);
        assertEquals(VoucherSale.STATUS_CANCEL, dbSale.getStatus());

        Voucher dbVoucher = this.getBaseJpaDao().findById(Voucher.class, dbSale.getVoucherId());
        assertEquals(Voucher.STATUS_IMPORTED, dbVoucher.getStatus());

        // -------------------------------------
        // assert db balance transaction
        // -------------------------------------
        // assert original sale commission logs
        List<BalanceTransactions> commissionLogs = this.getBalanceTransactionsDao().findBalanceTransactions(
                saleRespCtx.getTransactionID());
        assertEquals(1, commissionLogs.size());
        for (BalanceTransactions commissionLog : commissionLogs) {
            assertEquals(BalanceTransactions.STATUS_INVALID, commissionLog.getStatus());
        }
        // cancellation of sale commission log
        BalanceTransactions cancelCommLog = this.getBalanceTransactionsDao()
                .findByTransactionAndOwnerAndGameAndOrigTransType(cancelRespCtx.getTransactionID(),
                        cancelRespCtx.getOperatorId(), "VOUCHER-1",
                        TransactionType.SELL_TELECO_VOUCHER.getRequestType());
        assertEquals(dbCancelTrans.getOperatorId(), cancelCommLog.getOperatorId());
        assertEquals(dbCancelTrans.getMerchantId(), cancelCommLog.getMerchantId());
        assertEquals(dbCancelTrans.getDeviceId(), cancelCommLog.getDeviceId());
        assertEquals(dbCancelTrans.getOperatorId(), cancelCommLog.getOwnerId());
        assertEquals(BalanceTransactions.OWNER_TYPE_OPERATOR, cancelCommLog.getOwnerType());
        assertEquals(BalanceTransactions.PAYMENT_TYPE_PLUSING_MONEY, cancelCommLog.getPaymentType());
        assertEquals(dbCancelTrans.getType(), cancelCommLog.getTransactionType());
        assertEquals(-3.0, cancelCommLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.05, cancelCommLog.getCommissionRate().doubleValue(), 0);
        assertEquals(saleReqDto.getFaceAmount().doubleValue(), cancelCommLog.getTransactionAmount().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, cancelCommLog.getStatus());
    }

    @Test
    public void test_Sell_NoEnoughBufferExpireDay() throws Exception {
        printMethod();
        Voucher reqDto = VoucherDomainMocker.mockVoucherTopup();

        this.jdbcTemplate.execute("update VAS_VOUCHER_OPERATOR_PARA set EXPIRED_DAY=10");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        Voucher respDto = (Voucher) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_NO_TICKET, respCtx.getResponseCode());
    }

    @Test
    public void test_Sell_InactiveGame() throws Exception {
        printMethod();
        Voucher reqDto = VoucherDomainMocker.mockVoucherTopup();

        this.jdbcTemplate.execute("update game set status=" + Game.STATUS_INACTIVE);

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        Voucher respDto = (Voucher) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_GAME_INACTIVE, respCtx.getResponseCode());
    }

    public void test_Sell_SoldVoucher() throws Exception {
        printMethod();
        Voucher reqDto = VoucherDomainMocker.mockVoucherTopup();

        this.jdbcTemplate.execute("update vas_vouchers set status=" + Voucher.STATUS_SOLD);

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        Voucher respDto = (Voucher) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_NO_TICKET, respCtx.getResponseCode());
    }

    @Test
    public void test_Sell_IllegalAmount_NotInRange() throws Exception {
        printMethod();
        Voucher reqDto = VoucherDomainMocker.mockVoucherTopup();
        // refer to TELECO_VOUCHERPortServlet.
        reqDto.setFaceAmount(new BigDecimal("101.0"));

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        Voucher respDto = (Voucher) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_NO_TICKET, respCtx.getResponseCode());
    }

    @Test
    public void test_Sell_IllegalAmount_NoEnoughSaleBalance() throws Exception {
        printMethod();
        Voucher reqDto = VoucherDomainMocker.mockVoucherTopup();

        this.jdbcTemplate.execute("update operator set SALE_BALANCE=1");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        Voucher respDto = (Voucher) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_EXCEED_CREDITLIMIT, respCtx.getResponseCode());
    }

    @Test
    public void test_Topup_NoGameAllocated() throws Exception {
        printMethod();
        Voucher reqDto = VoucherDomainMocker.mockVoucherTopup();

        this.jdbcTemplate.execute("delete from GAME_MERCHANT");

        Context reqCtx = this.getDefaultContext(TransactionType.SELL_TELECO_VOUCHER.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.TELECO_VOUCHER.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        Voucher respDto = (Voucher) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_OPERATOR_SELL_NOPRIVILEDGE, respCtx.getResponseCode());
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

    public VoucherSaleDao getVoucherSaleDao() {
        return voucherSaleDao;
    }

    public void setVoucherSaleDao(VoucherSaleDao voucherSaleDao) {
        this.voucherSaleDao = voucherSaleDao;
    }

    public VoucherStatisticsDao getVoucherStatDao() {
        return voucherStatDao;
    }

    public void setVoucherStatDao(VoucherStatisticsDao voucherStatDao) {
        this.voucherStatDao = voucherStatDao;
    }

}
