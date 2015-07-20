package com.mpos.lottery.te.gameimpl.instantgame.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGBatchReport;

public interface IGBatchReportDao extends DAO {

    /**
     * query a batch report data information by batch id.
     * 
     * @return a IGBatchReport
     */
    IGBatchReport getByBatchId(String operatorId, long batchId);

    /**
     * delete data by operatorid.
     */
    void deleteDataByOperatorId(long batchNumber, String operatorId);

}
