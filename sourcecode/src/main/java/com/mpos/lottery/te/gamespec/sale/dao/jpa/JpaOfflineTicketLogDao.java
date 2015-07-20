package com.mpos.lottery.te.gamespec.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.sale.OfflineTicketLog;
import com.mpos.lottery.te.gamespec.sale.dao.OfflineTicketLogDao;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("offlineTicketLogDao")
public class JpaOfflineTicketLogDao extends BaseJpaDao implements OfflineTicketLogDao {

    @SuppressWarnings("unchecked")
    @Override
    public List<OfflineTicketLog> findByTransaction(String transactionId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transId", transactionId);
        return (List<OfflineTicketLog>) this.findByNamedParams(
                "from OfflineTicketLog t where t.transactionId=:transId", params);
    }

}
