package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.merchant.domain.OperatorMerchant;

public interface OperatorMerchantDao extends DAO {

    OperatorMerchant findByOperatorAndMerchant(String operatorId, long merchantID);

    /**
     * A operator only can be allocated a single merchant.
     */
    OperatorMerchant findByOperator(String operatorId);
}
