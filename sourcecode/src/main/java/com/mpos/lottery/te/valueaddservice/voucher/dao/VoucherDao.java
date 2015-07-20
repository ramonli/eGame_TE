package com.mpos.lottery.te.valueaddservice.voucher.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.voucher.Voucher;

import java.math.BigDecimal;

public interface VoucherDao extends DAO {

    /**
     * Lookup valid voucher by game and face amount. Besides filter by game and face amount, below conditions must be
     * satisfied:
     * <ul>
     * <li>The status of voucher must be {@link Voucher#STATUS_IMPORTED}</li>
     * <li>Voucher must not be expired.</li>
     * <li>(Voucher.ExpireDay-bufferExpireDay) must be before current day.</li>
     * </ul>
     * Only a single voucher will be returned.
     */
    Voucher findByGameAndFaceAmount(String gameId, BigDecimal faceAmount, int bufferExpireDay);
    
    Voucher findBySerialNo(String serialNo);

    Voucher findByVoucherId(String voucherid);
}
