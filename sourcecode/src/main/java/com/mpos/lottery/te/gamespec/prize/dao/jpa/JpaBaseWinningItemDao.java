package com.mpos.lottery.te.gamespec.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.bingo.prize.support.second.BingoLuckyPrizeResult;
import com.mpos.lottery.te.gameimpl.bingo.prize.support.second.BingoWinningLuckyItem;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.dao.BaseWinningItemDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaBaseWinningItemDao extends BaseJpaDao implements BaseWinningItemDao {

    @Override
    public <T extends BaseWinningItem> List<T> findByGameInstanceAndSerialNoAndVersion(Class<T> clazz,
            String gameInstanceId, String ticketSerialNo, long version) {
        String sql = "from " + clazz.getCanonicalName() + " as t where t.gameInstanceId = :gameInstanceId and "
                + "t.ticketSerialNo=:ticketSerialNo and t.version=:version";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        params.put("ticketSerialNo", ticketSerialNo);
        params.put("version", version);
        return (List<T>) this.findByNamedParams(sql, params);
    }

    @Override
    public <T extends BaseWinningItem> List<T> findBySerialNo(Class<T> clazz, String ticketSerialNo) {
        String sql = "from " + clazz.getCanonicalName()
                + " as t where t.ticketSerialNo=:ticketSerialNo and t.valid=:isValid";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ticketSerialNo", ticketSerialNo);
        params.put("isValid", Boolean.TRUE);
        return (List<T>) this.findByNamedParams(sql, params);
    }

    @Override
    public <T extends BaseWinningItem> List<T> findAllBySerialNo(Class<T> clazz, String ticketSerialNo) {
        String sql = "from " + clazz.getCanonicalName() + " as t where t.ticketSerialNo=:ticketSerialNo ";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ticketSerialNo", ticketSerialNo);
        // params.put("isValid", Boolean.TRUE);
        return (List<T>) this.findByNamedParams(sql, params);
    }

    @Override
    public List<BingoWinningLuckyItem> findSecondPrizeBySerialNo(String ticketSerialNo) {
        String sql = "from BingoWinningLuckyItem as t where t.ticketSerialNo=:ticketSerialNo ";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ticketSerialNo", ticketSerialNo);

        return (List<BingoWinningLuckyItem>) this.findByNamedParams(sql, params);
    }

    @Override
    public List<BingoLuckyPrizeResult> findLuckyPrizeResultByLuckyNoAndSerialNoAndVersion(String gameInstanceId,
            String luckyNo, int version) {
        String sql = "from BingoLuckyPrizeResult as t where t.gameInstanceId = :gameInstanceId and "
                + "t.luckyNo=:luckyNo and t.version=:version";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameInstanceId", gameInstanceId);
        params.put("luckyNo", luckyNo);
        params.put("version", version);
        return (List<BingoLuckyPrizeResult>) this.findByNamedParams(sql, params);
    }

}
