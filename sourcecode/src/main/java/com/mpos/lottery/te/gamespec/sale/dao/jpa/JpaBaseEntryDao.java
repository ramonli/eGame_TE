package com.mpos.lottery.te.gamespec.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaBaseEntryDao extends BaseJpaDao implements BaseEntryDao {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseEntry> List<T> findByTicketSerialNo(Class<T> clazz, String serialNo, boolean allowNull)
            throws ApplicationException {
        String sql = "from " + clazz.getCanonicalName() + " as t where t.ticketSerialNo=:serialNo order by t.entryNo";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNo", serialNo);
        List<T> result = (List<T>) this.findByNamedParams(sql, params);
        if (!allowNull && result.size() == 0) {
            throw new ApplicationException(SystemException.CODE_NO_ENTRIES, "No entries of type(" + clazz.getName()
                    + ") found by serialNO:" + serialNo);
        }
        return result;
    }

}
