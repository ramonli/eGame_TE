package com.mpos.lottery.te.gameimpl.raffle.sale.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.raffle.game.RaffleOperationParameter;
import com.mpos.lottery.te.gamespec.game.BaseFunType;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.support.validator.DefaultTicketValidator;
import com.mpos.lottery.te.port.Context;

public class RaffleTicketValidator extends DefaultTicketValidator {

    @Override
    protected BaseFunType lookupFunType(Context respCtx, Game game) {
        return null;
    }

    @Override
    protected BaseOperationParameter lookupOperationParameter(Context respCtx, Game game) {
        return this.getBaseJpaDao().findById(RaffleOperationParameter.class, game.getOperatorParameterId());
    }

    @Override
    protected void customizeValidateTicketBefore(Context respCtx, BaseTicket clientTicket, Game game,
            BaseFunType funType, BaseOperationParameter opParam) throws ApplicationException {
        // only one entry allowed
        if (clientTicket.getEntries().size() > 1) {
            throw new ApplicationException(SystemException.CODE_WRONGFORMAT_SELECTEDNUMBER,
                    "Raffle game only supports one entry, however client provide " + clientTicket.getEntries().size()
                            + " entries.");
        }
    }

}
