package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGBatchReportDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGBatchReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IGBatchReportDaoImpl extends BaseJpaDao implements IGBatchReportDao {

    @Override
    public IGBatchReport getByBatchId(String operatorId, long batchId) {
        String sql = "from IGBatchReport i where batchId=:batchId and operatorId=:operatorId";
        Map param = new HashMap();
        param.put("batchId", batchId);
        param.put("operatorId", operatorId);
        List<IGBatchReport> list = this.findByNamedParams(sql, param);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public void deleteDataByOperatorId(long batchNumber, String operatorId) {
        String sql = "delete from ig_batch_report t where t.operator_id = :operatorId and t.batch_id =:batchNumber";
        int records = this.getEntityManager().createNativeQuery(sql).setParameter("operatorId", operatorId)
                .setParameter("batchNumber", batchNumber).executeUpdate();
        logger.info("delete ig_batch_report of  " + records + " records under batchNumber(" + batchNumber
                + ") operator id(" + operatorId + ") in ig_batch_report  table.");
    }

}
