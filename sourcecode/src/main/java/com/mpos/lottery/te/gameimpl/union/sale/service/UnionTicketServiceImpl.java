package com.mpos.lottery.te.gameimpl.union.sale.service;

import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.AbstractTicketService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * LottoTicket manager.
 */
public class UnionTicketServiceImpl extends AbstractTicketService {
    private Log logger = LogFactory.getLog(UnionTicketServiceImpl.class);

    @Override
    public GameType supportedGameType() {
        return GameType.UNION;
    }

    @Override
    protected void customizeAssembleTicket(BaseTicket generatedTicket, BaseTicket clientTicket) {
        super.customizeAssembleTicket(generatedTicket, clientTicket);
    }

}
