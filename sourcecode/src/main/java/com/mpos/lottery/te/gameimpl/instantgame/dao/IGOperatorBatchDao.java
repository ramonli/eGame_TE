package com.mpos.lottery.te.gameimpl.instantgame.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGOperatorBatch;

public interface IGOperatorBatchDao extends DAO {

    /**
     * According to operatorId query batct number.
     * <p/>
     * if no data is returned, just insert a data to the table and return the batch number = 1
     * <p/>
     * if returned data,so batch number + 1 and return batch number
     * 
     * @param operatorId
     * @return IGOperatorBatch
     */
    IGOperatorBatch getIGOperatorBatch(String operatorId) throws ApplicationException;

}
