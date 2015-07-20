package com.mpos.lottery.te.gameimpl.digital.sale.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalOperationParameter;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

public class DigitalOddEvenSelectedNumberValidator extends DigitalXDSelectedNumberValidator {

    public DigitalOddEvenSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    @Override
    protected void checkNumbers(BaseEntry entry) throws ApplicationException {
        // check whether the ODD/EVEN bet options are supported
        DigitalOperationParameter opPara = (DigitalOperationParameter) this.getOperationParam();
        if (!opPara.isSupportOddEven()) {
            throw new ApplicationException(SystemException.CODE_UNSUPPORTED_BETOPTION,
                    "ODD/EVEN bet option are not supported");
        }
    }
}
