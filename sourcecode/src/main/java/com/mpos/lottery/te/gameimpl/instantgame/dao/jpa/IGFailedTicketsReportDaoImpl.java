package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGFailedTicketsReportDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGFailedTicketsReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IGFailedTicketsReportDaoImpl extends BaseJpaDao implements IGFailedTicketsReportDao {

    @Override
    public List<IGFailedTicketsReport> findByBatchId(String operatorId, long batchId) {
        String sql = "from IGFailedTicketsReport i where i.status=1 and i.batchId=:batchId and i.operatorId=:operatorId";
        Map param = new HashMap();
        param.put("batchId", batchId);
        param.put("operatorId", operatorId);
        List<IGFailedTicketsReport> list = this.findByNamedParams(sql, param);
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    @Override
    public void deleteDataByOperatorId(long batchNumber, String operatorId) {
        String sql = "delete from ig_failed_tickets_report t where t.operator_id = :operatorId and t.batch_id = :batchNumber";
        int records = this.getEntityManager().createNativeQuery(sql).setParameter("operatorId", operatorId)
                .setParameter("batchNumber", batchNumber).executeUpdate();
        logger.info("delete ig_failed_tickets_report of  " + records + " records under batchNumber(" + batchNumber
                + ") operator id(" + operatorId + ") in ig_failed_tickets_report  table.");
    }

}
