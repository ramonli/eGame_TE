package com.mpos.lottery.te.gameimpl.instantgame.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGFailedTicketsReport;

import java.util.List;

public interface IGFailedTicketsReportDao extends DAO {

    /**
     * Lookup a set of IG Failed Tickets Report.
     * 
     * @param batchId
     *            batch id
     * @return List of type IGFailedTicketsReport Lookup a set of IG Failed Tickets Report.
     */
    List<IGFailedTicketsReport> findByBatchId(String operatorId, long batchId);

    /**
     * delete data by operatorid.
     */
    void deleteDataByOperatorId(long batchNumber, String operatorId);
}
