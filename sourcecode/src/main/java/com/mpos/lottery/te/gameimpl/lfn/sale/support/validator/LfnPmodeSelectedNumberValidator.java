package com.mpos.lottery.te.gameimpl.lfn.sale.support.validator;

import com.mpos.lottery.te.common.util.Combination;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnEntry;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumber;
import com.mpos.lottery.te.port.Context;

/**
 * Verify bet options of N1...N5.
 * 
 * @author Ramon
 * 
 */
public class LfnPmodeSelectedNumberValidator extends LfnNmodeSelectedNumberValidator {

    public LfnPmodeSelectedNumberValidator(String numberFormat, int betOption) {
        super(numberFormat, betOption);
    }

    @Override
    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        SelectedNumber sNumber = entry.parseSelectedNumber();
        Combination c = new Combination(sNumber.getBaseNumbers().length, this.getBetOption()
                - LfnEntry.BETOPTION_INTERVAL);
        return c.getTotal().longValue();
    }

    /**
     * Compare the count of selected balls.
     */
    @Override
    protected void customValidateAfter(Context respCtx, BaseEntry entry, SelectedNumber selectedNumber)
            throws ApplicationException {
        int allowCountOfNumber = this.getBetOption() - LfnEntry.BETOPTION_INTERVAL;

        // value of bet option P must be less than (K+50) of K/N
        if ((this.getFunType().getK() + LfnEntry.BETOPTION_INTERVAL) < this.getBetOption()) {
            throw new SystemException(SystemException.CODE_UNSUPPORTED_BETOPTION, "Unsupported bet option:"
                    + this.getBetOption() + ", the P bet option can't be greater than K("
                    + (this.getFunType().getK() + LfnEntry.BETOPTION_INTERVAL) + ")");
        }

        if (selectedNumber.getAllNumbers().length < allowCountOfNumber) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, "The count of P"
                    + this.getBetOption() + " selected number(" + entry.getSelectNumber() + ") of ticket(serialNo="
                    + entry.getTicketSerialNo() + ") must be greater than or equivalent with " + allowCountOfNumber);
        }

        this.checkAmount(entry);
    }
}
