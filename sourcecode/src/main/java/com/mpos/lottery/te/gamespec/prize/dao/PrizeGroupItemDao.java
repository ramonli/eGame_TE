package com.mpos.lottery.te.gamespec.prize.dao;

import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;

import java.util.List;

public interface PrizeGroupItemDao {

    // List<PrizeGroupItem> findByGroup(String prizeGroupId, int prizeType) ;
    List<PrizeGroupItem> findByGroupAndGameTypeAndGroupType(String prizeGroupId, int gameType, int groupType);
}
