package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.IGOperatorBatchDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IGOperatorBatch;

import java.util.HashMap;
import java.util.Map;

public class IGOperatorBatchDaoImpl extends BaseJpaDao implements IGOperatorBatchDao {
    // private UUIDService uuidService;

    @Override
    public IGOperatorBatch getIGOperatorBatch(String operatorId) throws ApplicationException {
        IGOperatorBatch igOperatorBatch = null;
        String sql = "from IGOperatorBatch i where  i.operatorId=:operatorId";
        Map param = new HashMap();
        param.put("operatorId", operatorId);
        return (IGOperatorBatch) this.findSingleByNamedParams(sql, param);

        /*
         * if (list.size() == 0) {
         * 
         * 
         * } else { igOperatorBatch = list.get(0); igOperatorBatch.setBatchNumber(igOperatorBatch.getBatchNumber()+1);
         * this.update(igOperatorBatch); //delete temporary payout records //1,delete payout records //2,delete payout
         * detail records //3,update status of all IG tickets from 'processing'to 'active' } return igOperatorBatch;
         */
    }
    /*
     * public UUIDService getUuidService() { return uuidService; }
     * 
     * public void setUuidService(UUIDService uuidService) { this.uuidService = uuidService; }
     */

}
