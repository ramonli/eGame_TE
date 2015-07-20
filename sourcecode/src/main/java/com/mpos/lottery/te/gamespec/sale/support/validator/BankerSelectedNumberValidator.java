package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.common.util.Combination;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.port.Context;

public class BankerSelectedNumberValidator extends AbstractSelectedNumberValidator {

    public BankerSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    @Override
    protected void customValidateBefore(Context respCtx, BaseEntry entry) throws ApplicationException {
        if (!this.getOperationParam().isBankerBetOptionSupported()) {
            throw new ApplicationException(SystemException.CODE_UNSUPPORTED_BETOPTION,
                    "Unsupported bet option: banker, it has been disabled in operation parameters.");
        }
    }

    /**
     * The banker selected number contains two parts: banker and leg, such as selected number
     * '1,2,3,4-11,23,24,30,39,45', the banker part is '1,2,3,4', and the leg part is '11,23,24,30,39,45'. Check below
     * constraints:
     * <ol>
     * <li>The total count of number(banker+leg) must be greater than funType.getKKK()</li>
     * <li>The count of banker number must be less than funType.getKKK()</li>
     * </ol>
     * In this case, if K is 6 and N is 49, then a chance will be a combination of banker and any 2 number from leg
     * part.
     */
    @Override
    protected void customValidateAfter(Context respCtx, BaseEntry entry, SelectedNumber sNumber)
            throws ApplicationException {
        int[] baseNumbers = sNumber.getBaseNumbers();
        int[] specialNumbers = sNumber.getSpecialNumbers();
        int k = this.getFunType().getK();
        if ((baseNumbers.length + specialNumbers.length) <= k) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The count of selected number of entry(number=" + entry.getSelectNumber() + ",serialNo="
                            + entry.getTicketSerialNo() + ",betOption=" + entry.getBetOption()
                            + ") should be greater than " + k + ". ");
        }
        if (baseNumbers.length >= k) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The count of bankder number of entry(number=" + entry.getSelectNumber() + ",serialNo="
                            + entry.getTicketSerialNo() + ",betOption=" + entry.getBetOption()
                            + ") should be less than " + k + ". ");
        }
    }

    /**
     * Calculate the total bets of a banker selelcted-number.
     */
    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        SelectedNumber sNumber = entry.parseSelectedNumber();
        int[] baseNumbers = sNumber.getBaseNumbers();
        int[] specialNumbers = sNumber.getSpecialNumbers();
        // The previous shoudl guarantee that numbers.length<funType.k
        int r = getFunType().getK() - baseNumbers.length;
        int n = specialNumbers.length;
        Combination c = new Combination(n, r);
        return c.getTotal().longValue();
    }

}
