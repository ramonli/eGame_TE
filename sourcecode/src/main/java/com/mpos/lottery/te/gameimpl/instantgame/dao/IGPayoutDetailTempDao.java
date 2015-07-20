package com.mpos.lottery.te.gameimpl.instantgame.dao;

import com.mpos.lottery.te.common.dao.DAO;

public interface IGPayoutDetailTempDao extends DAO {

    /**
     * move temporary payout data to payout table.
     * */
    void movePayoutDetailData(long batchNumber, String operatorId);

    /**
     * delete data by operatorid.
     */
    void deleteDataByOperatorId(long batchNumber, String operatorId);
}
