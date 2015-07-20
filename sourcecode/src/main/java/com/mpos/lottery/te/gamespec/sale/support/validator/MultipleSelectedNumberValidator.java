package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.common.util.Combination;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.port.Context;

public class MultipleSelectedNumberValidator extends AbstractSelectedNumberValidator {

    public MultipleSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    @Override
    protected void customValidateBefore(Context respCtx, BaseEntry entry) throws ApplicationException {
        if (!this.getOperationParam().isMultipleBetOptionSupported()) {
            throw new ApplicationException(SystemException.CODE_UNSUPPORTED_BETOPTION,
                    "Unsupported bet option: multiple, it has been disabled in operation parameters.");
        }
    }

    @Override
    protected void customValidateAfter(Context respCtx, BaseEntry entry, SelectedNumber sNumber)
            throws ApplicationException {
        // Make sure the selected number is a legal multiple numbers.
        int baseNumbers[] = sNumber.getBaseNumbers();
        int k = this.getFunType().getK();
        if (sNumber.getSpecialNumber() == null) {
            if (baseNumbers.length <= k || baseNumbers.length > this.getFunType().getN()) {
                throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                        "The count of base part of  multiple selected number of entry(number="
                                + entry.getSelectNumber() + ",serialNo=" + entry.getTicketSerialNo() + ",betOption="
                                + entry.getBetOption() + ") should be between " + k + " and "
                                + this.getFunType().getN());
            }
        } else {
            boolean isLegalMultiple = false;
            int specialNumbers[] = sNumber.getSpecialNumbers();
            int x = this.getFunType().getX();
            int y = this.getFunType().getY();
            if (baseNumbers.length == k && (specialNumbers.length > x && specialNumbers.length < y)) {
                isLegalMultiple = true;
            }
            if ((baseNumbers.length > k && baseNumbers.length < this.getFunType().getN())
                    && (specialNumbers.length >= x && specialNumbers.length < y)) {
                isLegalMultiple = true;
            }
            if (!isLegalMultiple) {
                throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                        "The multiple selected number of entry(number=" + entry.getSelectNumber() + ",serialNo="
                                + entry.getTicketSerialNo() + ",betOption=" + entry.getBetOption()
                                + ") should follow K/N[" + k + "/" + this.getFunType().getN() + "] and X/Y[" + x + "/"
                                + y + "]");
            }

        }
    }

    @Override
    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        SelectedNumber sNumber = entry.parseSelectedNumber();
        int baseNumbers[] = sNumber.getBaseNumbers();
        // The previous shoudl guarantee that numbers.length<funType.k
        Combination c = new Combination(baseNumbers.length, this.getFunType().getK());
        long totalCount = c.getTotal().longValue();

        if (sNumber.getSpecialNumber() != null) {
            int specialNumbers[] = sNumber.getSpecialNumbers();
            // The previous shoudl guarantee that numbers.length<funType.k
            Combination sc = new Combination(specialNumbers.length, this.getFunType().getX());
            totalCount = totalCount * sc.getTotal().longValue();
        }
        return totalCount;
    }

}
