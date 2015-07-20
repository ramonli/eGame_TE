package com.mpos.lottery.te.merchant.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.merchant.domain.MerchantCommission;

import java.util.List;

public interface MerchantCommissionDao extends DAO {

    MerchantCommission getByMerchantAndGame(long merchantId, String gameId);

    /**
     * Lookup all supported game of this merchant.
     */
    List<MerchantCommission> getByMerchant(long merchantId);
}
