package com.mpos.lottery.te.gameimpl.lfn.sale.support.validator;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumber;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;

/**
 * Verify bet options of N1...N5.
 * 
 * @author Ramon
 * 
 */
public class LfnNmodeSelectedNumberValidator extends AbstractSelectedNumberValidator {
    private int betOption;

    public LfnNmodeSelectedNumberValidator(String numberFormat, int betOption) {
        super(numberFormat);
        this.betOption = betOption;
    }

    @Override
    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        return 1;
    }

    @Override
    public BigDecimal calEntryAmount(BaseEntry entry) throws ApplicationException {
        // the client provide entry amount, no need to calculate at server side.
        return entry.getEntryAmount();
    }

    /**
     * Verify the amount of a entry, it must be equal or greater than base amount.
     */
    protected final void checkAmount(BaseEntry entry) throws ApplicationException {
        if (entry.getEntryAmount().compareTo(this.getOperationParam().getBaseAmount()) < 0) {
            throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "The entry amount["
                    + entry.getEntryAmount() + "] is less than base amount:" + this.getOperationParam().getBaseAmount());
        }
    }

    /**
     * Compare the count of selected balls.
     */
    @Override
    protected void customValidateAfter(Context respCtx, BaseEntry entry, SelectedNumber selectedNumber)
            throws ApplicationException {
        this.checkAmount(entry);
        // value of bet option N must be less than K of K/N
        if (this.getFunType().getK() < this.betOption) {
            throw new SystemException(SystemException.CODE_UNSUPPORTED_BETOPTION, "Unsupported bet option:" + betOption
                    + ", the N bet option can't be greater than K(" + this.getFunType().getK() + ")");
        }

        if (selectedNumber.getAllNumbers().length != this.betOption) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, "The count of N"
                    + this.betOption + " selected number(" + entry.getSelectNumber() + ") of ticket(serialNo="
                    + entry.getTicketSerialNo() + ") exceeds " + this.betOption);
        }
    }

    public int getBetOption() {
        return betOption;
    }

}
