package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.common.util.Combination;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.port.Context;

public class RollSelectedNumberValidator extends AbstractSelectedNumberValidator {

    public RollSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        SelectedNumber sNumber = entry.parseSelectedNumber();
        int numbers[] = sNumber.getBaseNumbers();
        // The previous should guarantee that numbers.length<funType.k
        int n = this.getFunType().getN() - numbers.length;
        int r = this.getFunType().getK() - numbers.length;
        Combination c = new Combination(n, r);
        return c.getTotal().longValue();
    }

    /**
     * Roll bet option can be regarded as a special banker. If this bet option, all other numbers will be regarded as
     * leg part.
     */
    @Override
    protected void customValidateAfter(Context respCtx, BaseEntry entry, SelectedNumber selectedNumber)
            throws ApplicationException {
        int numbers[] = selectedNumber.getBaseNumbers();
        int k = this.getFunType().getK();
        if (numbers.length >= k) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The count of selected number of entry(number=" + entry.getSelectNumber() + ",serialNo="
                            + entry.getTicketSerialNo() + ",betOption=" + entry.getBetOption()
                            + ") should be less than " + k + ".");
        }
    }

}
