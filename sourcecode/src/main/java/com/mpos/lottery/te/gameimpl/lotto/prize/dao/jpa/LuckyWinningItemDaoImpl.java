package com.mpos.lottery.te.gameimpl.lotto.prize.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.dao.LuckyWinningItemDao;
import com.mpos.lottery.te.gameimpl.lotto.prize.domain.LuckyWinningItem;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuckyWinningItemDaoImpl extends BaseJpaDao implements LuckyWinningItemDao {

    @Override
    public List<LuckyWinningItem> findByTicketAndGameDraw(String serialNo, String gameDrawId, long lastSuccessfulVersion)
            throws SQLException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ticketSerialNo", serialNo);
        params.put("isValid", Boolean.TRUE);
        params.put("gameDrawId", gameDrawId);
        params.put("version", lastSuccessfulVersion);
        return this.findByNamedParams("from LuckyWinningItem i where i.ticketSerialNo=:ticketSerialNo "
                + "and i.valid=:isValid and i.gameInstanceId=:gameDrawId and i.version=:version", params);
    }

    @Override
    public List<LuckyWinningItem> findByTicketSerial(String serialNo) throws SQLException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ticketSerialNo", serialNo);

        return this.findByNamedParams("from LuckyWinningItem i where i.ticketSerialNo=:ticketSerialNo ", params);
    }

}
