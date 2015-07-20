package com.mpos.lottery.te.gameimpl.instantgame.domain.logic.active;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveCriteria;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.ActiveResult;

public class SingleTicketActiveStrategy extends AbstractCriteriaActiveStrategy {

    public ActiveResult active(ActiveCriteria criteria) throws ApplicationException {
        // InstantTicket ticket =
        // this.getInstantTicketDao().getBySerialNo(criteria.getValue());
        // List<InstantTicket> tickets = new ArrayList<InstantTicket>();
        // tickets.add(ticket);
        String serialNo = criteria.getValue();
        return this.batchActive(serialNo, serialNo, criteria.getTrans());
    }

}
