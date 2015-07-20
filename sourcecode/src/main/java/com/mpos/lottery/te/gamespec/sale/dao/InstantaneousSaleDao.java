package com.mpos.lottery.te.gamespec.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.sale.InstantaneousSale;

public interface InstantaneousSaleDao extends DAO {

    InstantaneousSale findByGameDraw(String gameDrawId);
}
