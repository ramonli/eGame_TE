package com.mpos.lottery.te.gameimpl.digital.sale.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumber;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;

public class DigitalXDSelectedNumberValidator extends AbstractSelectedNumberValidator {

    public DigitalXDSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    @Override
    public void validate(Context respCtx, BaseTicket ticket, Game game, BaseEntry entry) throws ApplicationException {
        // check format
        if (!this.checkFormat(entry.getSelectNumber(), this.getNumberFormat())) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, "The selected number("
                    + entry.getSelectNumber() + ") of bet option(" + entry.getBetOption() + ") of ticket(serialNo="
                    + entry.getTicketSerialNo() + ") is not match to '" + this.getNumberFormat() + "'.");
        }

        this.checkNumbers(entry);
        this.checkEntryAmount(entry);
    }

    protected void checkEntryAmount(BaseEntry entry) throws ApplicationException {
        if (entry.getEntryAmount().compareTo(this.getOperationParam().getBaseAmount()) < 0
                || entry.getEntryAmount().compareTo(
                        ((DigitalOperationParameter) this.getOperationParam()).getMaxBetAmount()) > 0) {
            throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT,
                    "The entry amount must be in range(" + this.getOperationParam().getBaseAmount() + ","
                            + ((DigitalOperationParameter) this.getOperationParam()).getMaxBetAmount()
                            + "), however the amount is " + entry.getEntryAmount());
        }
    }

    protected void checkNumbers(BaseEntry entry) throws ApplicationException {
        // verify whether XD is supported
        if (entry.getBetOption() > this.getFunType().getN() || entry.getBetOption() < this.getFunType().getK()) {
            throw new SystemException(SystemException.CODE_UNSUPPORTED_BETOPTION, "Unsupported digital "
                    + entry.getBetOption() + "D bet option, only " + this.getFunType().getK() + "D to "
                    + this.getFunType().getN() + "D is supported.");
        }
        SelectedNumber sNumber = entry.parseSelectedNumber();
        this.checkCountOfNumber(entry, sNumber);
        this.checkRangeOfNumber(entry, sNumber);
    }

    protected void checkRangeOfNumber(BaseEntry entry, SelectedNumber sNumber) {

        for (int numberItem : sNumber.getBaseNumbers()) {
            if (numberItem < this.getFunType().getX() || numberItem > this.getFunType().getY()) {
                throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                        "The number of single selected number(" + entry.getSelectNumber() + ") must be in range of ("
                                + this.getFunType().getX() + "," + this.getFunType().getY() + ")");
            }
        }
    }

    protected void checkCountOfNumber(BaseEntry entry, SelectedNumber sNumber) {
        // the format of selected number has restricted the count of number,
        // refer to mlottery_te.properties

        // if (sNumber.getBaseNumbers().length != this.getFunType().getK()) {
        // throw new
        // SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
        // "The count of single selected number(" + entry.getSelectNumber() +
        // ") must be "
        // + this.getFunType().getK());
        // }
    }

    @Override
    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        return 1;
    }

    @Override
    public BigDecimal calEntryAmount(BaseEntry entry) throws ApplicationException {
        return entry.getEntryAmount();
    }
}
