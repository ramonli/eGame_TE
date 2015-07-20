package com.mpos.lottery.te.gameimpl.union.sale.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.union.game.UnionCOperationParameter;
import com.mpos.lottery.te.gameimpl.union.game.UnionOperationParameter;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.support.validator.MultipleSelectedNumberValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumber;
import com.mpos.lottery.te.port.Context;

public class UnionMultipleSelectedNumberValidator extends MultipleSelectedNumberValidator {

    public UnionMultipleSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    @Override
    protected void customValidateAfter(Context respCtx, BaseEntry entry, SelectedNumber sNumber)
            throws ApplicationException {
        super.customValidateAfter(respCtx, entry, sNumber);

        int countOfTriple = sNumber.getBaseNumbers().length;
        UnionCOperationParameter cPara = ((UnionOperationParameter) this.getOperationParam())
                .findByTriple(countOfTriple);
        if (cPara == null) {
            throw new SystemException("No any Triple&Double setting found of give triple(" + countOfTriple + ")");
        }
        if (sNumber.getSpecialNumbers().length < cPara.getMinDouble()
                || sNumber.getSpecialNumbers().length > cPara.getMaxDouble()) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "The count of DOUBLE part of selected number(" + sNumber.getSelectedNumber() + ") must between "
                            + cPara.getMinDouble() + " and " + cPara.getMaxDouble() + ", if count of TRIPLE is "
                            + countOfTriple);
        }
    }

}
