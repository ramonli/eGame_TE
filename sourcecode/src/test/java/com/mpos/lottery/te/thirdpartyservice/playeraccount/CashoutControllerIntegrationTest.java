package com.mpos.lottery.te.thirdpartyservice.playeraccount;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.Operator;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseServletIntegrationTest;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutRequest;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.CashoutResponse;
import com.mpos.lottery.te.trans.dao.TransactionDao;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CashoutControllerIntegrationTest extends BaseServletIntegrationTest {
    @Resource(name = "transactionDao")
    private TransactionDao transactionDao;
    @Resource(name = "operatorDao")
    private OperatorDao operatorDao;
    @PersistenceContext
    private EntityManager entityManager;

    //@Rollback(false)
    @Test
    public void testCashout_ByAmount_OK() throws Exception {
        printMethod();
        CashoutRequest reqDto = PlayerDomainMocker.cashoutRequest();
        Context ctx = this.getDefaultContext(TransactionType.PLAYER_CASH_OUT.getRequestType(), reqDto);
        ctx.setGameTypeId(Game.TYPE_UNDEF + "");

        Operator operator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111");
        BigDecimal originaCashoutlLevel = operator.getDailyCashoutLevel();
        BigDecimal originaPayoutlLevel = operator.getPayoutCreditLevel();

        Context respCtx = doPost(this.mockRequest(ctx));
        this.entityManager.flush();
        this.entityManager.clear();

        assertEquals(200, respCtx.getResponseCode());
        CashoutResponse respDto = (CashoutResponse) respCtx.getModel();
        assertEquals(12000.0, respDto.getLeftCashoutAmount().doubleValue(), 0);
        assertEquals(reqDto.getMobile(), respDto.getMobile());
        assertEquals(11000.0, respDto.getCashoutAmount().doubleValue(), 0);

        // assert DB
        operator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111");
        // this.getEntityManager().refresh(operator);
        BigDecimal newLevel = operator.getDailyCashoutLevel();
        assertEquals(originaCashoutlLevel.add(respDto.getCashoutAmount()).doubleValue(), newLevel.doubleValue(), 0);
        assertEquals(originaPayoutlLevel.doubleValue(), operator.getPayoutCreditLevel().doubleValue(), 0);

        Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
        assertEquals(11000.0, dbTrans.getTotalAmount().doubleValue(), 0);
        assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
    }

    // @Test
    // public void testCashout_ByReferenceNo_OK() throws Exception {
    // printMethod();
    // CashoutRequest reqDto = PlayerDomainMocker.cashoutRequest();
    // reqDto.setCashoutAmount(null);
    // reqDto.setReferenceNum("1978");
    // Context ctx = this.getDefaultContext(TransactionType.PLAYER_CASH_OUT.getRequestType(), reqDto);
    // ctx.setGameTypeId(Game.TYPE_UNDEF + "");
    //
    // Operator operator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111");
    // BigDecimal originalLevel = operator.getDailyCashoutLevel();
    // BigDecimal originaPayoutlLevel = operator.getPayoutCreditLevel();
    //
    // Context respCtx = doPost(this.mockRequest(ctx));
    // this.entityManager.flush();
    // this.entityManager.clear();
    // // this.setComplete();
    //
    // assertEquals(200, respCtx.getResponseCode());
    // CashoutResponse respDto = (CashoutResponse) respCtx.getModel();
    // assertEquals(12000.0, respDto.getLeftCashoutAmount().doubleValue(), 0);
    // assertEquals(reqDto.getMobile(), respDto.getMobile());
    // assertEquals(11000.0, respDto.getCashoutAmount().doubleValue(), 0);
    //
    // // assert DB
    // operator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111");
    // BigDecimal newLevel = operator.getDailyCashoutLevel();
    // assertEquals(originalLevel.add(respDto.getCashoutAmount()).doubleValue(), newLevel.doubleValue(), 0);
    // assertEquals(originaPayoutlLevel.add(respDto.getCashoutAmount()).doubleValue(), operator.getPayoutCreditLevel()
    // .doubleValue(), 0);
    //
    // Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
    // assertEquals(11000.0, dbTrans.getTotalAmount().doubleValue(), 0);
    // assertEquals(SystemException.CODE_OK, dbTrans.getResponseCode());
    // }
    //
    // @Test
    // public void testCashout_Fail() throws Exception {
    // printMethod();
    //
    // CashoutRequest reqDto = PlayerDomainMocker.cashoutRequest();
    //
    // Operator operator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111");
    // BigDecimal originalLevel = operator.getDailyCashoutLevel();
    // BigDecimal originaPayoutlLevel = operator.getPayoutCreditLevel();
    //
    // reqDto.setMobile("13800138000");
    //
    // Context ctx = this.getDefaultContext(TransactionType.PLAYER_CASH_OUT.getRequestType(), reqDto);
    // ctx.setGameTypeId(Game.TYPE_UNDEF + "");
    // Context respCtx = doPost(this.mockRequest(ctx));
    // this.entityManager.flush();
    // this.entityManager.clear();
    //
    // assertEquals(201, respCtx.getResponseCode());
    //
    // // assert DB
    // operator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111");
    // BigDecimal newLevel = operator.getDailyCashoutLevel();
    // assertEquals(originalLevel.doubleValue(), newLevel.doubleValue(), 0);
    // assertEquals(originaPayoutlLevel.doubleValue(), operator.getPayoutCreditLevel().doubleValue(), 0);
    //
    // Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, respCtx.getTransactionID());
    // assertEquals(0, dbTrans.getTotalAmount().doubleValue(), 0);
    // assertEquals(201, dbTrans.getResponseCode());
    // }
    //
    // /**
    // * Timeout to cashout, TE will issue reversal automatically.
    // */
    // @Test
    // public void testReverse_Automatically() throws Exception {
    // printMethod();
    //
    // // issue cashout request
    // CashoutRequest reqDto = PlayerDomainMocker.cashoutRequest();
    // // trigger the timout of 3rd party dummy service
    // reqDto.setMobile("123456");
    //
    // Operator operator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111");
    // BigDecimal originalLevel = operator.getDailyCashoutLevel();
    // BigDecimal originaPayoutlLevel = operator.getPayoutCreditLevel();
    //
    // Context cashoutCtx = this.getDefaultContext(TransactionType.PLAYER_CASH_OUT.getRequestType(), reqDto);
    // cashoutCtx.setGameTypeId(Game.TYPE_UNDEF + "");
    // Context cashoutRespCtx = doPost(this.mockRequest(cashoutCtx));
    // this.entityManager.flush();
    // this.entityManager.clear();
    // // this.setComplete();
    //
    // assertEquals(500, cashoutRespCtx.getResponseCode());
    //
    // // assert DB
    // operator = this.getOperatorDao().findById(Operator.class, "OPERATOR-111");
    // BigDecimal newLevel = operator.getDailyCashoutLevel();
    // assertEquals(originalLevel.doubleValue(), newLevel.doubleValue(), 0);
    // assertEquals(originaPayoutlLevel.doubleValue(), operator.getPayoutCreditLevel().doubleValue(), 0);
    //
    // Transaction dbTrans = this.getTransactionDao().findById(Transaction.class, cashoutRespCtx.getTransactionID());
    // assertEquals(0, dbTrans.getTotalAmount().doubleValue(), 0);
    // assertEquals(500, dbTrans.getResponseCode());
    // }

    public TransactionDao getTransactionDao() {
        return transactionDao;
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public OperatorDao getOperatorDao() {
        return operatorDao;
    }

    public void setOperatorDao(OperatorDao operatorDao) {
        this.operatorDao = operatorDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
