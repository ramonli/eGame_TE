package com.mpos.lottery.te.gameimpl.bingo.sale.support.validator;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoFunType;
import com.mpos.lottery.te.gamespec.game.BaseFunType;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.support.validator.DefaultTicketValidator;
import com.mpos.lottery.te.port.Context;

public class BingoTicketValidator extends DefaultTicketValidator {

    @Override
    protected void customizeValidateTicketBefore(Context respCtx, BaseTicket clientTicket, Game game,
            BaseFunType funType, BaseOperationParameter opParam) throws ApplicationException {
        BingoFunType bFunType = (BingoFunType) funType;
        // only one entry allowed
        if (clientTicket.getEntries().size() != bFunType.getMaxEntriesInTicket()) {
            throw new ApplicationException(SystemException.CODE_UNMATCHED_ENTRY_COUNT, "There must be "
                    + bFunType.getMaxEntriesInTicket() + " entries in bingo ticket, actual "
                    + clientTicket.getEntries().size() + " entries.");
        }
    }
}
