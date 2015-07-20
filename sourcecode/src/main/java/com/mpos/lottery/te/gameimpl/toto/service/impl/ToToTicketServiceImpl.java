package com.mpos.lottery.te.gameimpl.toto.service.impl;

import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.service.AbstractTicketService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TOTO game ticket handler
 */
public class ToToTicketServiceImpl extends AbstractTicketService {
    private Log logger = LogFactory.getLog(ToToTicketServiceImpl.class);

    @Override
    public GameType supportedGameType() {
        return GameType.TOTO;
    }

}
