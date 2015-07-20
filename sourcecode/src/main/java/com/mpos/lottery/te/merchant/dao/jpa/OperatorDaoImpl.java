package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.merchant.dao.OperatorDao;
import com.mpos.lottery.te.merchant.domain.Operator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Query;

public class OperatorDaoImpl extends BaseJpaDao implements OperatorDao {
    private Log logger = LogFactory.getLog(OperatorDaoImpl.class);

    public Operator findByIdForUpdate(final String id) throws DataAccessException {
        /**
         * The association between Operator and Merchant is ManyToOne. When multiple operator update the credit level of
         * the merchant, 'Non-repeatable read' should be avoided. So lock the merchant for update, until the transaction
         * committed/rollbacked.
         * 
         * By default, the transaction waits until the requested row lock is acquired. If the wait for a row lock is too
         * long, you can code logic into your application to cancel the lock operation and try again later.
         */
        long waitLockTime = MLotteryContext.getInstance().getWaitLockTime();

        String sql = "select * from OPERATOR where OPERATOR_ID=? for update";
        if (waitLockTime > 0) {
            sql += " wait " + waitLockTime;
        }
        // String sql = "select * from MERCHANT where MERCHANT_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql, Operator.class);
        query.setParameter(1, id);
        Object operator = query.getSingleResult();
        // Merchant merchant = em.find(Merchant.class, id);
        // em.lock(merchant, LockModeType.WRITE);
        return (Operator) operator;
    }

    @Override
    public Operator findByLoginName(final String loginName) throws DataAccessException {

        String sql = "select * from OPERATOR where LOGIN_NAME=? ";

        Query query = this.getEntityManager().createNativeQuery(sql, Operator.class);
        query.setParameter(1, loginName);
        Operator operator = null;
        List<Operator> listoperator = query.getResultList();

        if (listoperator != null && listoperator.size() > 0) {
            operator = (Operator) listoperator.get(0);
        }
        return operator;
    }

    @Override
    public Operator findByLoginNameForUpdate(final String loginName) throws DataAccessException {
        /**
         * The association between Operator and Merchant is ManyToOne. When multiple operator update the credit level of
         * the merchant, 'Non-repeatable read' should be avoided. So lock the merchant for update, until the transaction
         * committed/rollbacked.
         * 
         * By default, the transaction waits until the requested row lock is acquired. If the wait for a row lock is too
         * long, you can code logic into your application to cancel the lock operation and try again later.
         */
        long waitLockTime = MLotteryContext.getInstance().getWaitLockTime();

        String sql = "select * from OPERATOR where LOGIN_NAME=? for update";
        if (waitLockTime > 0) {
            sql += " wait " + waitLockTime;
        }
        // String sql = "select * from MERCHANT where MERCHANT_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql, Operator.class);
        query.setParameter(1, loginName);
        Object operator = query.getSingleResult();
        // Merchant merchant = em.find(Merchant.class, id);
        // em.lock(merchant, LockModeType.WRITE);
        return (Operator) operator;
    }

    @Override
    public void deductBalanceByOperator(BigDecimal commissionDeducted, BigDecimal payoutDeducted,
            BigDecimal cashoutDeducted, String operatorid) throws DataAccessException {
        String sql = "update OPERATOR set COMMISION_BALANCE=COMMISION_BALANCE - " + commissionDeducted.doubleValue()
                + "," + " PAYOUT_BALANCE=PAYOUT_BALANCE - " + payoutDeducted.doubleValue() + ","
                + " CASHOUT_BALANCE=CASHOUT_BALANCE - " + cashoutDeducted.doubleValue() + " where OPERATOR_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, operatorid);
        query.executeUpdate();
    }

    @Override
    public void deductBalanceByOperatorCancel(BigDecimal commissionDeducted, BigDecimal payoutDeducted,
            BigDecimal cashoutDeducted, String operatorid) throws DataAccessException {
        String sql = "update OPERATOR set COMMISION_BALANCE=COMMISION_BALANCE + " + commissionDeducted.doubleValue()
                + "," + " PAYOUT_BALANCE=PAYOUT_BALANCE + " + payoutDeducted.doubleValue() + ","
                + " CASHOUT_BALANCE=CASHOUT_BALANCE + " + cashoutDeducted.doubleValue() + " where OPERATOR_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, operatorid);
        query.executeUpdate();
    }

    @Override
    public void addCashoutAndCommissionToOperator(BigDecimal cashoutAmount, BigDecimal commissionAmount,
            String operatorid) throws DataAccessException {
        String sql = "update OPERATOR set " + " CASHOUT_BALANCE=CASHOUT_BALANCE + " + cashoutAmount.doubleValue();
        if (commissionAmount != null) {
            sql = sql + ", COMMISION_BALANCE=COMMISION_BALANCE + " + commissionAmount.doubleValue();
        }
        sql = sql + " where OPERATOR_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, operatorid);
        query.executeUpdate();
    }

    @Override
    public void addCashoutAndCommissionToOperatorCancel(BigDecimal cashoutAmount, BigDecimal commissionAmount,
            String operatorid) throws DataAccessException {
        String sql = "update OPERATOR set " + " CASHOUT_BALANCE=CASHOUT_BALANCE - " + cashoutAmount.doubleValue();
        if (commissionAmount != null) {
            sql = sql + ", COMMISION_BALANCE=COMMISION_BALANCE - " + commissionAmount.doubleValue();
        }
        sql = sql + " where OPERATOR_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, operatorid);
        query.executeUpdate();
    }

}
