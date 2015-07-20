package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.WinningStatistics;
import com.mpos.lottery.te.gamespec.prize.BaseWinningStatistics;
import com.mpos.lottery.te.gamespec.prize.dao.BaseWinningStatisticsDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaBaseWinningStatisticsDao extends BaseJpaDao implements BaseWinningStatisticsDao {

    @Override
    public <T extends BaseWinningStatistics> T getByGameDrawAndPrizeLevelAndVersion(Class<T> clazz, String gameDrawId,
            int prizeLevel, long lastSuccessVersion) {
        Map params = new HashMap();
        params.put("gameInstanceId", gameDrawId);
        params.put("prizeLevel", prizeLevel);
        params.put("version", lastSuccessVersion);

        List<WinningStatistics> stats = this.findByNamedParams("from " + clazz.getCanonicalName()
                + " w where w.gameInstanceId=:gameInstanceId and w.prizeLevel=:prizeLevel " + "and w.version=:version",
                params);
        if (stats.size() > 0) {
            return (T) stats.get(0);
        }
        return null;
    }

}
