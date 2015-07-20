package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.merchant.domain.OperatorCommission;

public interface OperatorCommissionDao extends DAO {

    OperatorCommission getByOperatorAndMerchantAndGame(String operatorId, long merchantId, String gameId);

}
