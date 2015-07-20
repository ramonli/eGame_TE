package com.mpos.lottery.te.gamespec.prize.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gamespec.prize.TaxThreshold;

import java.math.BigDecimal;
import java.util.Date;

public interface TaxThresholdDao extends DAO {

    TaxThreshold getByPolicyAndAmountAndDateRange(String taxPolicyId, BigDecimal amount, Date taxDate);
}
