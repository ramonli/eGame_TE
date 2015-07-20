package com.mpos.lottery.te.valueaddservice.voucher.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherStatistics;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherStatisticsDao;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository("jpaVoucherStatDao")
public class JpaVoucherStatisticsDao extends BaseJpaDao implements VoucherStatisticsDao {

    @Override
    public VoucherStatistics findByGameAndFaceAmount(BigDecimal faceAmount, String gameId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("faceAmount", faceAmount);
        params.put("gameId", gameId);
        Object entity = this.findSingleFromListByNamedParams(
                "from VoucherStatistics v where v.faceAmount=:faceAmount and v.gameId=:gameId", params);
        return (VoucherStatistics) entity;
    }

}
