package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseFunType;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractSelectedNumberValidator implements SelectedNumberValidator {
    private String numberFormat;
    private BaseFunType funType;
    private BaseOperationParameter operationParam;

    public AbstractSelectedNumberValidator(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public BaseFunType getFunType() {
        return funType;
    }

    public void setFunType(BaseFunType funType) {
        this.funType = funType;
    }

    public BaseOperationParameter getOperationParam() {
        return operationParam;
    }

    public void setOperationParam(BaseOperationParameter operationParam) {
        this.operationParam = operationParam;
    }

    @Override
    public void validate(Context respCtx, BaseTicket ticket, Game game, BaseEntry entry) throws ApplicationException {
        this.customValidateBefore(respCtx, entry);
        // check format
        if (!this.checkFormat(entry.getSelectNumber(), this.numberFormat)) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, "The selected number("
                    + entry.getSelectNumber() + ") of bet option(" + entry.getBetOption() + ") of ticket(serialNo="
                    + entry.getTicketSerialNo() + ") is not match to '" + numberFormat + "'.");
        }
        SelectedNumber sNumber = entry.parseSelectedNumber();
        // int allNumbers[] = sNumber.getAllNumbers();
        this.checkMinNumber(sNumber, entry);
        this.checkMaxNumber(sNumber, entry);
        this.checkUniqueNumber(sNumber, entry);
        this.customValidateAfter(respCtx, entry, sNumber);
    }

    @Override
    public BigDecimal calEntryAmount(BaseEntry entry) throws ApplicationException {
        return this.getOperationParam().getBaseAmount().multiply(new BigDecimal(entry.getTotalBets()));
    }

    protected boolean checkFormat(String selectedNumber, String numberFormat) {
        Pattern p = Pattern.compile(numberFormat);
        Matcher m = p.matcher(selectedNumber);
        return m.matches();
    }

    protected void customValidateBefore(Context respCtx, BaseEntry entry) throws ApplicationException {
        // template method
    }

    /**
     * Check that the minimal number can not be less than 1
     */
    protected final void checkMinNumber(SelectedNumber sNumber, BaseEntry entry) {
        // check both baseNumbers and specialNumbers
        if ((sNumber.getBaseNumber() != null && sNumber.getBaseNumbers()[0] < 1)
                || (sNumber.getSpecialNumber() != null && sNumber.getSpecialNumbers()[0] < 1)) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The min number of entry(number=" + entry.getSelectNumber() + ",serialNo="
                            + entry.getTicketSerialNo() + ") is less than 1.");
        }
    }

    /**
     * The maximal number can not be greater than N.
     */
    protected final void checkMaxNumber(SelectedNumber sNumber, BaseEntry entry) {
        int n = this.funType.getN();

        // check baseNumbers first
        if (sNumber.getBaseNumbers()[sNumber.getBaseNumbers().length - 1] > n) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The max number of entry(number=" + entry.getSelectNumber() + ",serialNo="
                            + entry.getTicketSerialNo() + ") is greater than N[" + n + "].");
        }

        // check special numbers then
        if (sNumber.getSpecialNumber() != null) {
            int y = this.funType.getY();
            if (sNumber.getSpecialNumbers()[sNumber.getSpecialNumbers().length - 1] > y) {
                throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                        "The max number of entry(number=" + entry.getSelectNumber() + ",serialNo="
                                + entry.getTicketSerialNo() + ") is greater than Y[" + y + "].");
            }
        }

    }

    /**
     * The number can not be duplicated.
     */
    protected final void checkUniqueNumber(SelectedNumber sNumber, BaseEntry entry) {
        // check baseNumbers fist
        if (SimpleToolkit.containRepeatedly(sNumber.getBaseNumbers())
                || (sNumber.getSpecialNumber() != null && SimpleToolkit.containRepeatedly(sNumber.getSpecialNumbers()))) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The selected numbers of entry(number=" + entry.getSelectNumber() + ",serialNo="
                            + entry.getTicketSerialNo() + ") contain duplicated numbers.");
        }

    }

    /**
     * Template method for subclass to customize the validation process.
     */
    protected void customValidateAfter(Context respCtx, BaseEntry entry, SelectedNumber selectedNumber)
            throws ApplicationException {
    }

}
