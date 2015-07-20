package com.mpos.lottery.te.valueaddservice.airtime;

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

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

public class SmartAirtimeTopupIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    @Rollback(true)
    @Test
    public void test_Topup_OK() throws Exception {
        printMethod();
        AirtimeTopup reqDto = AirtimeDomainMocker.mockSmartAirtimeTopup();

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal oldCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        Context reqCtx = this.getDefaultContext(TransactionType.AIRTIME_TOPUP.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.AIRTIME.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        AirtimeTopup respDto = (AirtimeTopup) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        assertEquals(SystemException.CODE_OK, respCtx.getResponseCode());
        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(reqDto.getMobileNo(), respDto.getMobileNo());
        assertEquals(reqDto.getAmount(), respDto.getAmount());
        assertEquals(AirtimeTopup.STATUS_SUCCESS, respDto.getStatus());
        assertEquals(reqDto.getGame().getId(), respDto.getGame().getId());

        // -------------------------------------
        // assert sale balance
        // -------------------------------------
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        BigDecimal newCommBalance = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111").getCommisionBalance();

        assertEquals(newCreditOperator.add(reqDto.getAmount()).doubleValue(), oldCreditOperator.doubleValue(), 0);
        assertEquals(newCreditMerchant.doubleValue(), oldCreditMerchant.doubleValue(), 0);
        // commission balance
        assertEquals(newCommBalance.doubleValue(),
                oldCommBalance.add(reqDto.getAmount().multiply(new BigDecimal("0.05"))).doubleValue(), 0);

        // -------------------------------------
        // assert db general trarnsaction
        // -------------------------------------
        Transaction dbTrans = this.getBaseJpaDao().findById(Transaction.class, respCtx.getTransactionID());
        Transaction expectTrans = new Transaction();
        expectTrans.setId(respCtx.getTransactionID());
        expectTrans.setGameId(reqDto.getGame().getId());
        expectTrans.setTotalAmount(reqDto.getAmount());
        expectTrans.setTicketSerialNo(respCtx.getTransactionID());
        expectTrans.setOperatorId(reqCtx.getOperatorId());
        expectTrans.setMerchantId(111);
        expectTrans.setDeviceId(reqCtx.getTerminalId());
        expectTrans.setTraceMessageId(reqCtx.getTraceMessageId());
        expectTrans.setType(reqCtx.getTransType());
        expectTrans.setResponseCode(SystemException.CODE_OK);
        this.assertTransaction(expectTrans, dbTrans);

        // -------------------------------------
        // assert db airtime topup transaction
        // -------------------------------------
        AirtimeTopup dbTopup = this.getBaseJpaDao().findById(AirtimeTopup.class, respCtx.getTransactionID());
        assertTrue(dbTopup.getSerialNo() != dbTrans.getId());
        assertEquals(reqDto.getAmount().doubleValue(), dbTopup.getAmount().doubleValue(), 0);
        assertEquals(reqDto.getGame().getId(), dbTopup.getGame().getId());
        assertEquals(AirtimeTopup.STATUS_SUCCESS, dbTopup.getStatus());
        assertEquals(dbTrans.getDeviceId(), dbTopup.getDevId());
        assertEquals(dbTrans.getOperatorId(), dbTopup.getOperatorId());
        assertEquals(dbTrans.getMerchantId(), dbTopup.getMerchantId());
        assertEquals(dbTrans.getId(), dbTopup.getTransaction().getId());
        // refer to AirtimePortServlet.
        assertTrue(dbTopup.getTelcCommTransId() != null);
        assertEquals(reqDto.getMobileNo(), dbTopup.getMobileNo());

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
        // 0.01*0.05
        assertEquals(0.0005, operatorBalanceLog.getCommissionAmount().doubleValue(), 0);
        assertEquals(0.05, operatorBalanceLog.getCommissionRate().doubleValue(), 0);
        assertEquals(BalanceTransactions.STATUS_VALID, operatorBalanceLog.getStatus());
    }

    @Test
    public void test_Topup_Remote_Fail() throws Exception {
        printMethod();
        AirtimeTopup reqDto = AirtimeDomainMocker.mockSmartAirtimeTopup();
        reqDto.setMobileNo("13800138999");

        // old credit level
        BigDecimal oldCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal oldCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();

        Context reqCtx = this.getDefaultContext(TransactionType.AIRTIME_TOPUP.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.AIRTIME.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        AirtimeTopup respDto = (AirtimeTopup) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_REMOTE_SERVICE_FAILUER, respCtx.getResponseCode());
        assertTrue(respDto == null);

        // -------------------------------------
        // assert sale balance
        // -------------------------------------
        BigDecimal newCreditMerchant = this.getBaseJpaDao().findById(Merchant.class, 111L).getSaleCreditLevel();
        BigDecimal newCreditOperator = this.getBaseJpaDao().findById(Operator.class, "OPERATOR-111")
                .getSaleCreditLevel();
        assertEquals(newCreditOperator.doubleValue(), oldCreditOperator.doubleValue(), 0);
        assertEquals(newCreditMerchant.doubleValue(), oldCreditMerchant.doubleValue(), 0);
    }

    @Test
    public void test_Topup_InactiveGame() throws Exception {
        printMethod();
        AirtimeTopup reqDto = AirtimeDomainMocker.mockSmartAirtimeTopup();

        this.jdbcTemplate.execute("update game set status=" + Game.STATUS_INACTIVE);

        Context reqCtx = this.getDefaultContext(TransactionType.AIRTIME_TOPUP.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.AIRTIME.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        AirtimeTopup respDto = (AirtimeTopup) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_GAME_INACTIVE, respCtx.getResponseCode());
    }

    @Test
    public void test_Topup_IllegalAmount_NotInRange() throws Exception {
        printMethod();
        AirtimeTopup reqDto = AirtimeDomainMocker.mockSmartAirtimeTopup();
        reqDto.setAmount(new BigDecimal("101.0"));

        Context reqCtx = this.getDefaultContext(TransactionType.AIRTIME_TOPUP.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.AIRTIME.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        AirtimeTopup respDto = (AirtimeTopup) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, respCtx.getResponseCode());
    }

    @Test
    public void test_Topup_IllegalAmount_NoStep() throws Exception {
        printMethod();
        AirtimeTopup reqDto = AirtimeDomainMocker.mockSmartAirtimeTopup();
        reqDto.setAmount(new BigDecimal("71.0"));

        this.jdbcTemplate.execute("update AIRTIME_PARAMETERS set BET_AMOUNT_STEPPING=3");

        Context reqCtx = this.getDefaultContext(TransactionType.AIRTIME_TOPUP.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.AIRTIME.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        AirtimeTopup respDto = (AirtimeTopup) respCtx.getModel();

        this.getEntityManager().flush();
        this.getEntityManager().clear();

        // -------------------------------------
        // assert response DTO
        // -------------------------------------
        assertEquals(SystemException.CODE_UNMATCHED_SALEAMOUNT, respCtx.getResponseCode());
    }

    @Test
    public void test_Topup_IllegalAmount_NoEnoughSaleBalance() throws Exception {
        printMethod();
        AirtimeTopup reqDto = AirtimeDomainMocker.mockSmartAirtimeTopup();

        this.jdbcTemplate.execute("update operator set SALE_BALANCE=0");

        Context reqCtx = this.getDefaultContext(TransactionType.AIRTIME_TOPUP.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.AIRTIME.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        AirtimeTopup respDto = (AirtimeTopup) respCtx.getModel();

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
        AirtimeTopup reqDto = AirtimeDomainMocker.mockSmartAirtimeTopup();

        this.jdbcTemplate.execute("delete from GAME_MERCHANT");

        Context reqCtx = this.getDefaultContext(TransactionType.AIRTIME_TOPUP.getRequestType(), reqDto);
        reqCtx.setGameTypeId(GameType.AIRTIME.getType() + "");
        Context respCtx = doPost(this.mockRequest(reqCtx));
        AirtimeTopup respDto = (AirtimeTopup) respCtx.getModel();

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

}
