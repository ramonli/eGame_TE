package com.mpos.lottery.te.gameimpl.extraball.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallAlgorithm;

public interface ExtraBallAlgorithmDao extends DAO {

    ExtraBallAlgorithm findByType(int type);
}
