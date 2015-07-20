package com.mpos.lottery.te.trans.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.trans.dao.SettlementLogItemDao;
import com.mpos.lottery.te.trans.domain.SettlementLogItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

@Repository("jpaSettlementLogItemDao")
public class JpaSettlementLogItemDao extends BaseJpaDao implements SettlementLogItemDao {
    private Log logger = LogFactory.getLog(JpaSettlementLogItemDao.class);

    @Override
    public SettlementLogItem findByOperator(String operatorId, int status, Date settlementDay) {
        // String sql =
        // "from SettlementLogItem l where l.operatorId=:operatorId and l.settlementLog.status=:status and "
        // + "l.checkDay between :beginOfDay and :endOfDay";
        String settlementDayStr = SimpleToolkit.formatDate(settlementDay, "yyyyMMdd");
        String sql = "select i.ID,i.PAYOUT_TIME from " + "CARD_PAYOUT_HISTORY g, CARD_PAYOUT_HISTORY_ITEM i where "
                + "g.ID=i.CARD_PAYOUT_HISTORY_ID and g.STATUS=:status and i.OPERATOR_ID=:operatorId and "
                + "TO_CHAR(i.PAYOUT_TIME,'YYYYMMDD')='" + settlementDayStr + "'";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter("operatorId", operatorId);
        query.setParameter("status", status);
        List rows = query.getResultList();
        if (rows.size() == 0) {
            return null;
        } else {
            Object[] columns = (Object[]) rows.get(0);
            SettlementLogItem item = new SettlementLogItem();
            item.setId((String) columns[0]);
            item.setCheckDay(new Date(((Timestamp) columns[1]).getTime()));
            return item;
        }
    }
}
