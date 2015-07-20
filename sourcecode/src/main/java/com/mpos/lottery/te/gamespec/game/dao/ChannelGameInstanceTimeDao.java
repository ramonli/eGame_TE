package com.mpos.lottery.te.gamespec.game.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.game.ChannelGameInstanceTime;

public interface ChannelGameInstanceTimeDao extends DAO {

    ChannelGameInstanceTime findByChannelType(String gameChannelSettingId, int channelType);
}
