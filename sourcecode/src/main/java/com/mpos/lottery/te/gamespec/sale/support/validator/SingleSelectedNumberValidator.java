package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.port.Context;

public class SingleSelectedNumberValidator extends AbstractSelectedNumberValidator {

    public SingleSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        return 1;
    }

    @Override
    protected void customValidateAfter(Context respCtx, BaseEntry entry, SelectedNumber sNumber)
            throws ApplicationException {
        if (sNumber.getBaseNumbers().length != this.getFunType().getK()) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The count of base numbers of single selected number(" + entry.getSelectNumber()
                            + ") of ticket(serialNo=" + entry.getTicketSerialNo() + ") must be "
                            + this.getFunType().getK());
        }

        if (sNumber.getSpecialNumber() != null && sNumber.getSpecialNumbers().length != this.getFunType().getX()) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The count of special numbers of single selected number(" + entry.getSelectNumber()
                            + ") of ticket(serialNo=" + entry.getTicketSerialNo() + ") must be "
                            + this.getFunType().getX());
        }
    }

}
