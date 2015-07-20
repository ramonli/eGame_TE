package com.mpos.lottery.te.gameimpl.magic100.sale.support.validator;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.port.Context;

public class Magic100SingleSelectedNumberValidator extends AbstractSelectedNumberValidator {

    public Magic100SingleSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    @Override
    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        return 1;
    }

    public void validate(Context respCtx, BaseTicket ticket, Game game, BaseEntry entry) throws ApplicationException {
        // check format
        if (!this.checkFormat(entry.getSelectNumber(), this.getNumberFormat())) {
            throw new SystemException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER, "The selected number("
                    + entry.getSelectNumber() + ") of ticket(serialNo=" + entry.getTicketSerialNo()
                    + ") is not match to '" + this.getNumberFormat() + "'.");
        }
    }

}
