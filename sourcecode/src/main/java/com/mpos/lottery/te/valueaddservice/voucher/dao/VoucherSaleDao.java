package com.mpos.lottery.te.valueaddservice.voucher.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.valueaddservice.voucher.VoucherSale;

public interface VoucherSaleDao extends DAO {

    VoucherSale findByTransaction(String transactionId);
}
