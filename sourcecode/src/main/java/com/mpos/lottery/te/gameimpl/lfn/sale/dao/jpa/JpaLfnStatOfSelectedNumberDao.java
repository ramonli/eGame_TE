package com.mpos.lottery.te.gameimpl.lfn.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.lfn.sale.dao.LfnStatOfSelectedNumberDao;
import com.mpos.lottery.te.gamespec.sale.StatOfSelectedNumber;

import java.util.List;

import javax.persistence.Query;

public class JpaLfnStatOfSelectedNumberDao extends BaseJpaDao implements LfnStatOfSelectedNumberDao {

    @SuppressWarnings({ "unchecked" })
    @Override
    public List<StatOfSelectedNumber> findByGameInstance(final String gameInstanceId, final int rows) {
        String sql = "from StatOfSelectedNumber s where s.gameInstanceId=:gameInstanceId order by s.count";
        Query query = this.getEntityManager().createQuery(sql);
        query.setParameter("gameInstanceId", gameInstanceId);
        query.setMaxResults(rows);
        return query.getResultList();
    }
}
