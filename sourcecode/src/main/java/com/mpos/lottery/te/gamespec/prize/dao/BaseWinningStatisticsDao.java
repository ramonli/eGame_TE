package com.mpos.lottery.te.gamespec.prize.dao;

import com.mpos.lottery.te.gamespec.prize.BaseWinningStatistics;

public interface BaseWinningStatisticsDao {

    <T extends BaseWinningStatistics> T getByGameDrawAndPrizeLevelAndVersion(Class<T> clazz, String gameDrawId,
            int prizeLevel, long lastSuccessVersion);
}
