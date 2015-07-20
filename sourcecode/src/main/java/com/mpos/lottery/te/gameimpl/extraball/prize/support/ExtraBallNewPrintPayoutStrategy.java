package com.mpos.lottery.te.gameimpl.extraball.prize.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallEntry;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.support.TicketHelper;
import com.mpos.lottery.te.port.Context;

import java.util.List;

public class ExtraBallNewPrintPayoutStrategy extends NewPrintPayoutStrategy {

    @Override
    public String key() {
        return Game.TYPE_EXTRABALL + "-" + BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET;
    }

    @Override
    protected void doAssembleNewTicket(BaseTicket newTicket, BaseTicket templateTicket) {
        ExtraBallTicket ticket = (ExtraBallTicket) newTicket;
        ticket.setBatchNo(null);
    }

    @Override
    protected List lookupEntries(String serialNo) throws ApplicationException {
        return this.getBaseEntryDao().findByTicketSerialNo(ExtraBallEntry.class, serialNo, true);
    }

    @Override
    protected BaseTicket generateNewPrintPhysicalTicket(Context<?> respCtx, Prize prize, List<BaseTicket> newTickets,
            List newEntries) {
        return TicketHelper.assemblePhysicalTicket(newTickets, newEntries);
    }

}
