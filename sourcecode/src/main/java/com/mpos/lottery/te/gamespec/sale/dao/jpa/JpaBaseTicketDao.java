package com.mpos.lottery.te.gamespec.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JpaBaseTicketDao extends BaseJpaDao implements BaseTicketDao {

    @Override
    public <T extends BaseTicket> List<T> findBySerialNo(Class<T> clazz, String seiralNo, boolean allowNull)
            throws ApplicationException {
        // "order by t.id" makes sure that the ticket records are sorted by game
        // instance.
        String sql = "from " + clazz.getCanonicalName() + " as t where t.serialNo = :serialNo ORDER BY t.id";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNo", seiralNo);
        List<T> result = (List<T>) this.findByNamedParams(sql, params);
        if (!allowNull && result.size() == 0) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET, "can NOT find ticket(serialNO=" + seiralNo
                    + ", type=" + clazz.getName() + ") from underlying database.");
        }
        return result;
    }

    @Override
    public <T extends BaseTicket> List<T> findByTransaction(Class<T> clazz, String transId) {
        String sql = "from " + clazz.getCanonicalName() + " as t where t.transaction.id=:transId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("transId", transId);
        return (List<T>) this.findByNamedParams(sql, params);
    }
}
