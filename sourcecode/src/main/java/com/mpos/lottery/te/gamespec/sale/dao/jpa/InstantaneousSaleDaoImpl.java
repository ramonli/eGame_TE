package com.mpos.lottery.te.gamespec.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.sale.InstantaneousSale;
import com.mpos.lottery.te.gamespec.sale.dao.InstantaneousSaleDao;

import java.util.List;

import javax.persistence.Query;

public class InstantaneousSaleDaoImpl extends BaseJpaDao implements InstantaneousSaleDao {

    @SuppressWarnings({ "rawtypes" })
    @Override
    public InstantaneousSale findByGameDraw(final String gameDrawId) {
        String sql = "select * from INSTANTANEOUS_SALES where GAME_INSTANCE_ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql, InstantaneousSale.class);
        query.setParameter(1, gameDrawId);
        List sales = query.getResultList();
        // InstantaneousSale sale = em.find(InstantaneousSale, id);
        // em.lock(sale, LockModeType.WRITE);
        if (sales.size() > 0) {
            return (InstantaneousSale) sales.get(0);
        } else {
            return null;
        }
    }

}
