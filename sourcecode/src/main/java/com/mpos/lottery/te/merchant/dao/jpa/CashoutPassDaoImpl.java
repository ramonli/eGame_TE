package com.mpos.lottery.te.merchant.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.merchant.dao.CashoutPassDao;
import com.mpos.lottery.te.merchant.domain.CashoutPass;

import org.springframework.dao.DataAccessException;

import javax.persistence.Query;

public class CashoutPassDaoImpl extends BaseJpaDao implements CashoutPassDao {

    @Override
    public CashoutPass findByBarcodeAndPassword(String barcode, String password) throws DataAccessException {
        Query query = this.getEntityManager().createNativeQuery(
                "select * from cashout_pass t where t.cashout_barcode=:barcode and t.cashout_password=:password ",
                CashoutPass.class);
        query.setParameter("barcode", barcode);
        query.setParameter("password", password);
        CashoutPass cashout = null;
        if (query.getResultList() != null && query.getResultList().size() > 0) {
            cashout = (CashoutPass) query.getSingleResult();
        }
        return cashout;
    }

    @Override
    public CashoutPass findByBarcode(String barcode) throws DataAccessException {
        Query query = this.getEntityManager().createNativeQuery(
                "select * from cashout_pass t where t.cashout_barcode=:barcode ", CashoutPass.class);
        query.setParameter("barcode", barcode);
        CashoutPass cashout = null;
        if (query.getResultList() != null && query.getResultList().size() > 0) {
            cashout = (CashoutPass) query.getSingleResult();
        }
        return cashout;
    }

    @Override
    public void increaseTriedTimes(String barcode) throws DataAccessException {
        String sql = "update CASHOUT_PASS set TRIED_TIMES=TRIED_TIMES + 1" + " where CASHOUT_BARCODE=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, barcode);
        query.executeUpdate();
    }

}
