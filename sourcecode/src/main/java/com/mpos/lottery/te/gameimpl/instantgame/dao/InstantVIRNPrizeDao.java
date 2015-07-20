package com.mpos.lottery.te.gameimpl.instantgame.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantVIRNPrize;

public interface InstantVIRNPrizeDao extends DAO {

    InstantVIRNPrize getByGameDrawAndVIRN(String gameDrawID, String virn);

}
