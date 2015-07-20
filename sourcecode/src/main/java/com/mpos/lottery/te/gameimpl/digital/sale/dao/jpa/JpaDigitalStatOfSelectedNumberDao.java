package com.mpos.lottery.te.gameimpl.digital.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.digital.sale.dao.DigitalStatOfSelectedNumberDao;
import com.mpos.lottery.te.gamespec.sale.StatOfSelectedNumber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

import javax.persistence.Query;

public class JpaDigitalStatOfSelectedNumberDao extends BaseJpaDao implements DigitalStatOfSelectedNumberDao {
    private Log logger = LogFactory.getLog(JpaDigitalStatOfSelectedNumberDao.class);

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public List<StatOfSelectedNumber> findByGameInstance(final String gameInstanceId) {
        String sql = "from StatOfSelectedNumber s where s.gameInstanceId=:gameInstanceId order by s.count";
        Query query = this.getEntityManager().createQuery(sql);
        query.setParameter("gameInstanceId", gameInstanceId);
        List result = query.getResultList();
        if (logger.isDebugEnabled()) {
            logger.debug("Total " + result.size() + " StatOfSelectedNumber entities found(gameInstanceId="
                    + gameInstanceId + ").");
        }
        return result;
    }
}
