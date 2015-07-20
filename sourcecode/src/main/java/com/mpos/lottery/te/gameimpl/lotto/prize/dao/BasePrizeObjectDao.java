package com.mpos.lottery.te.gameimpl.lotto.prize.dao;

import com.mpos.lottery.te.common.dao.DAO;

public interface BasePrizeObjectDao extends DAO {

    /**
     * Lookup the object number in bd_prize_object_item.
     */
    int findByParentObjectId(String parentObjectId);

}
