package com.mpos.lottery.te.valueaddservice.vat.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.vat.OperatorBizType;

public interface OperatorBizTypeDao extends DAO {

    OperatorBizType findByOperator(String operatorId);
}
