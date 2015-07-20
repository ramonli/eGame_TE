package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.instantgame.dao.InstantTicketDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.logic.ReversalOrCancelStrategy;

public class InstantSellReversalStrategy implements ReversalOrCancelStrategy {
    private InstantTicketDao instantTicketDao;

    @Override
    public RoutineKey supportedReversalRoutineKey() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Reverse a 'sell instant ticket' transaction.
     */
    public boolean cancelOrReverse(Context<?> respCtx, Transaction trans) throws ApplicationException {
        String ticketSerialNo = trans.getTicketSerialNo();
        InstantTicket ticket = this.getInstantTicketDao().getBySerialNo(ticketSerialNo);
        if (ticket == null) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET,
                    "can NOT find instant ticket with serialNo='" + ticketSerialNo + "'.");
        }
        // reverse the sell
        ticket.setSoldTime(null);
        ticket.setStatus(InstantTicket.STATUS_INACTIVE);
        ticket.setSoldToCustomer(false);
        this.getInstantTicketDao().update(ticket);
        return false;
    }

    public InstantTicketDao getInstantTicketDao() {
        return instantTicketDao;
    }

    public void setInstantTicketDao(InstantTicketDao instantTicketDao) {
        this.instantTicketDao = instantTicketDao;
    }

}
