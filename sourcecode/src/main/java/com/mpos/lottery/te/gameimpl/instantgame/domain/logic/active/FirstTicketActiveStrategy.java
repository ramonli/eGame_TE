package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicketSerialNo;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;

public class FirstTicketActiveStrategy extends LastTicketActiveStrategy {

    protected void checkCriteria(ActiveCriteria criteria, InstantTicketSerialNo no, IgOperationParameter param)
            throws ApplicationException {
        if (no.getLongIndex() != param.getBeginTicketIndex()) {
            throw new ApplicationException(SystemException.CODE_NOT_LAST_SERIAL, "The serial number("
                    + criteria.getValue() + ") isn't the first " + "serial in book(number=" + no.getGGG() + no.getBBB()
                    + ") of game instance(" + no.getGGG() + ").");
        }
    }

}
