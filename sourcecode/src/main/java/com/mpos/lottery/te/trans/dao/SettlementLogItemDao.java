package com.mpos.lottery.te.trans.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.trans.domain.SettlementLogItem;

import java.util.Date;

public interface SettlementLogItemDao extends DAO {

    SettlementLogItem findByOperator(String operatorId, int status, Date settlementTime);
}
