package com.mpos.lottery.te.valueaddservice.voucher.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.transactionhandle.AbstractTransactionHandle;
import com.mpos.lottery.te.valueaddservice.voucher.Voucher;
import com.mpos.lottery.te.valueaddservice.voucher.dao.VoucherDao;

public class VoucherTransactionEnquiryHandler extends AbstractTransactionHandle {
    private VoucherDao voucherDao;

    @Override
    public int supportHandle() {
        return GameType.TELECO_VOUCHER.getType();
    }

    /**
     * The modle of 'voucher sale' should be voucher.
     */
    @Override
    public Object getTransactionModel(Context respCtx, Transaction targetTrans) throws ApplicationException {
        // voucher sale transaction will set voucher serialNo to ticket seralNo
        Voucher voucher = this.getVoucherDao().findBySerialNo(targetTrans.getTicketSerialNo());
        if (voucher == null) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET, "No voucher found by serialNo:"
                    + targetTrans.getTicketSerialNo());
        }
        voucher.decryptPin();
        return voucher;
    }

    public VoucherDao getVoucherDao() {
        return voucherDao;
    }

    public void setVoucherDao(VoucherDao voucherDao) {
        this.voucherDao = voucherDao;
    }

}
