package com.mpos.lottery.te.gamespec.game.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.gamespec.game.ChannelGameInstanceTime;
import com.mpos.lottery.te.gamespec.game.dao.ChannelGameInstanceTimeDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaChannelGameInstanceTimeDao extends BaseJpaDao implements ChannelGameInstanceTimeDao {

    @Override
    public ChannelGameInstanceTime findByChannelType(String gameChannelSettingId, int channelType) {
        String sql = "from ChannelGameInstanceTime c where c.channelType=:channelType and "
                + "c.gameChannelSettingId=:gameChannelSettingId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelType", channelType);
        params.put("gameChannelSettingId", gameChannelSettingId);
        List<ChannelGameInstanceTime> entities = this.findByNamedParams(sql, params);
        return this.single(entities, true);
    }
}
