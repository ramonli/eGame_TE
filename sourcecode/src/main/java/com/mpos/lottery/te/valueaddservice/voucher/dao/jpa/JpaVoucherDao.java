package com.mpos.lottery.te.valueaddservice.voucher.dao.jpa;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.DateUtils;
import com.mpos.lottery.te.valueaddservice.voucher.Voucher;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherDao;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Repository("jpaVoucherDao")
public class JpaVoucherDao extends BaseJpaDao implements VoucherDao {

    @Override
    public Voucher findByGameAndFaceAmount(String gameId, BigDecimal faceAmount, int bufferExpireDay) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("gameId", gameId);
        params.put("status", Voucher.STATUS_IMPORTED);
        params.put("faceAmount", faceAmount);
        params.put("expireDay", DateUtils.addDay(DateUtils.getBeginAndEndOfDay(new Date())[1], bufferExpireDay));

        String jpql = "from Voucher v where v.game.id=:gameId and v.status=:status and v.faceAmount=:faceAmount "
                + "and v.expireDate >:expireDay order by v.expireDate";
        return (Voucher) this.findSingleFromListByNamedParams(jpql, params);
    }

    @Override
    public Voucher findBySerialNo(String serialNo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serialNo", serialNo);

        String jpql = "from Voucher v where v.serialNo=:serialNo";
        return (Voucher) this.findSingleFromListByNamedParams(jpql, params);
    }

    @Override
    public Voucher findByVoucherId(String voucherid) {
        String jpql = "from Voucher s where s.id=:vouId";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("vouId", voucherid);
        return (Voucher) this.findSingleByNamedParams(jpql, params);
    }

}
