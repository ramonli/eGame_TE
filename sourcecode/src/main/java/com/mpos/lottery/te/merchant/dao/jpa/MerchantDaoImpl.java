package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.merchant.domain.Merchant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

public class MerchantDaoImpl extends BaseJpaDao implements MerchantDao {
    private Log logger = LogFactory.getLog(MerchantDaoImpl.class);

    public Merchant findByIdForUpdate(final long id) throws DataAccessException {
        /**
         * The association between Operator and Merchant is ManyToOne. When multiple operator update the credit level of
         * the merchant, 'Non-repeatable read' should be avoided. So lock the merchant for update, until the transaction
         * committed/rollbacked.
         * 
         * By default, the transaction waits until the requested row lock is acquired. If the wait for a row lock is too
         * long, you can code logic into your application to cancel the lock operation and try again later.
         */
        long waitLockTime = MLotteryContext.getInstance().getWaitLockTime();

        String sql = "select * from MERCHANT where MERCHANT_ID=? for update";
        if (waitLockTime > 0) {
            sql += " wait " + waitLockTime;
        }
        // String sql = "select * from MERCHANT where MERCHANT_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql, Merchant.class);
        query.setParameter(1, id);
        Object merchant = query.getSingleResult();
        // Merchant merchant = em.find(Merchant.class, id);
        // em.lock(merchant, LockModeType.WRITE);
        return (Merchant) merchant;
    }

    public List<Merchant> getByParent(Long parentId) {
        String sql = "from Merchant m where m.parentMerchant=:parentMerchant";
        Map params = new HashMap();
        Merchant parentMerchant = this.getEntityManager().getReference(Merchant.class, parentId);
        params.put("parentMerchant", parentMerchant);
        return this.findByNamedParams(sql, params);
    }

    @Override
    public Merchant getByCode(String code) throws DataAccessException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("code", code);
        List result = this.findByNamedParams("from Merchant m where m.code=:code", params);
        return (Merchant) (result.size() > 0 ? result.get(0) : null);
    }

    @Override
    public Merchant findByTaxNo(String taxNo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("taxNo", taxNo);
        List result = this.findByNamedParams("from Merchant m where m.taxNo=:taxNo", params);
        return (Merchant) this.single(result, false);
    }

    @Override
    public Merchant findDistributeMerchantByMerchantId(long merchantId) {
        Query query = this
                .getEntityManager()
                .createNativeQuery(
                        "select t.* from Merchant t where t.is_distribute=1 start with merchant_Id=:merchantId connect by prior parent_Id=merchant_Id",
                        Merchant.class);
        query.setParameter("merchantId", merchantId);
        Merchant merchant = (Merchant) query.getSingleResult();
        return merchant;
    }

    @Override
    public void deductBalanceByMerchant(BigDecimal commissionDeducted, BigDecimal payoutDeducted,
            BigDecimal cashoutDeducted, long merchantid) throws DataAccessException {
        String sql = "update MERCHANT set COMMISION_BALANCE=COMMISION_BALANCE - " + commissionDeducted.doubleValue()
                + "," + " PAYOUT_BALANCE=PAYOUT_BALANCE - " + payoutDeducted.doubleValue() + ","
                + " CASHOUT_BALANCE=CASHOUT_BALANCE - " + cashoutDeducted.doubleValue() + " where MERCHANT_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, merchantid);
        query.executeUpdate();
    }

    @Override
    public void deductBalanceByMerchantCancel(BigDecimal commissionDeducted, BigDecimal payoutDeducted,
            BigDecimal cashoutDeducted, long merchantid) throws DataAccessException {
        String sql = "update MERCHANT set COMMISION_BALANCE=COMMISION_BALANCE + " + commissionDeducted.doubleValue()
                + "," + " PAYOUT_BALANCE=PAYOUT_BALANCE + " + payoutDeducted.doubleValue() + ","
                + " CASHOUT_BALANCE=CASHOUT_BALANCE + " + cashoutDeducted.doubleValue() + " where MERCHANT_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, merchantid);
        query.executeUpdate();
    }

    @Override
    public void addCashoutAndCommissionToMerchant(BigDecimal cashoutAmount, BigDecimal commissionAmount, long merchantid)
            throws DataAccessException {
        String sql = "update MERCHANT set " + " CASHOUT_BALANCE=CASHOUT_BALANCE + " + cashoutAmount.doubleValue()
                + " where MERCHANT_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, merchantid);
        query.executeUpdate();
    }

    @Override
    public void addCashoutAndCommissionToMerchantCancel(BigDecimal cashoutAmount, BigDecimal commissionAmount,
            long merchantid) throws DataAccessException {
        String sql = "update MERCHANT set " + " CASHOUT_BALANCE=CASHOUT_BALANCE - " + cashoutAmount.doubleValue()
                + " where MERCHANT_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, merchantid);
        query.executeUpdate();

    }

}
