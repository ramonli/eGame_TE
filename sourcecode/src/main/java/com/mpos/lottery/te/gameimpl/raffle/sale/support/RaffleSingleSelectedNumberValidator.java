package com.mpos.lottery.te.gameimpl.raffle.sale.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.port.Context;

public class RaffleSingleSelectedNumberValidator extends AbstractSelectedNumberValidator {

    public RaffleSingleSelectedNumberValidator(String numberFormat) {
        super(numberFormat);
    }

    @Override
    public long calTotalBets(BaseEntry entry) throws ApplicationException {
        return entry.getTotalBets();
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
