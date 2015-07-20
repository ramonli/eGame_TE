package com.mpos.lottery.te.valueaddservice.voucher.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherStatistics;

import java.math.BigDecimal;

public interface VoucherStatisticsDao extends DAO {

    VoucherStatistics findByGameAndFaceAmount(BigDecimal faceAmount, String gameId);
}
