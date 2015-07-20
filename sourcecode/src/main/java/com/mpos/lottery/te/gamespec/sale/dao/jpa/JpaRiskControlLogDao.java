package com.mpos.lottery.te.gamespec.sale.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.sale.RiskControlLog;
import com.mpos.lottery.te.gamespec.sale.dao.RiskControlLogDao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

@SuppressWarnings({ "unchecked" })
public class JpaRiskControlLogDao extends BaseJpaDao implements RiskControlLogDao {

    @Override
    public RiskControlLog findByGameInstanceAndSelectedNumber(String gameInstanceId, String selectedNumber,
            int prizeLevelType) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        params.put("selectedNumber", selectedNumber);
        params.put("prizeLevelType", prizeLevelType);
        List<RiskControlLog> riskLogs = this.findByNamedParams(
                "from RiskControlLog r where r.gameInstanceId=:gameInstanceId and "
                        + "r.selectedNumber=:selectedNumber and r.prizeLevelType=:prizeLevelType", params);
        return this.single(riskLogs, false);
    }

    @Override
    public void updateWithAmount(final String id, final BigDecimal amount) {
        final String sql = "update BD_RISK_BETTING set TOTAL_AMOUNT=TOTAL_AMOUNT+" + amount + " where ID=?";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter(1, id);
        query.executeUpdate();
    }
}
