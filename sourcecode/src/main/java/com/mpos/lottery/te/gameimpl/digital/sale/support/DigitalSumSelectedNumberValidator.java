package com.mpos.lottery.te.gameimpl.digital.sale.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalOperationParameter;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

public class DigitalSumSelectedNumberValidator extends DigitalXDSelectedNumberValidator {

    public DigitalSumSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    @Override
    protected void checkNumbers(BaseEntry entry) throws ApplicationException {
        // check format
        if (!this.checkFormat(entry.getSelectNumber(), this.getNumberFormat())) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, "The selected number("
                    + entry.getSelectNumber() + ") of bet option(" + entry.getBetOption() + ") of ticket(serialNo="
                    + entry.getTicketSerialNo() + ") is not match to '" + this.getNumberFormat() + "'.");
        }
        // check whether the SUM bet options is supported
        DigitalOperationParameter opPara = (DigitalOperationParameter) this.getOperationParam();
        if (!opPara.isSupportSum()) {
            throw new ApplicationException(SystemException.CODE_UNSUPPORTED_BETOPTION,
                    "SUM bet option are not supported");
        }

        int minSum = this.getFunType().getX() * this.getFunType().getK();
        int maxSum = this.getFunType().getY() * this.getFunType().getN();
        int sum = Integer.parseInt(entry.getSelectNumber());
        if (sum < minSum || sum > maxSum) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, "The sum should be between "
                    + minSum + " and " + maxSum + ", however it is " + sum);
        }
    }
}
