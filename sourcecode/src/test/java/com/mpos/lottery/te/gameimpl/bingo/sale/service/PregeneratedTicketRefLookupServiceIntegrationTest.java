package com.mpos.lottery.te.gameimpl.bingo.sale.service;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoEntryRef;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicketRef;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import javax.annotation.Resource;

public class PregeneratedTicketRefLookupServiceIntegrationTest extends BaseTransactionalIntegrationTest {
    private static Log logger = LogFactory.getLog(PregeneratedTicketRefLookupServiceIntegrationTest.class);
    @Resource(name = "pregeneratedTicketRefLookupService")
    private TicketRefLookupService onlineTicketLookupService;
    @Resource(name = "baseGameInstanceDao")
    private BaseGameInstanceDao gameInstanceDao;

    @Test
    public void testLookupTicket() throws Exception {
        Context respCtx = new Context();
        BingoTicket clientTicket = new BingoTicket();
        BingoGameInstance gameInstance = this.getGameInstanceDao().lookupByGameAndNumber(111, BingoGameInstance.class,
                "BINGO-1", "11002");
        clientTicket.setGameInstance(gameInstance);

        BingoTicketRef ticketRef = this.getOnlineTicketLookupService().lookupTicket(respCtx, clientTicket, true);
        assertEquals(3, ticketRef.getEntryRefs().size());

        logger.info("ID:" + ticketRef.getId() + ",SerialNO:" + ticketRef.getImportTicketSerialNo());
        for (BingoEntryRef entryRef : ticketRef.getEntryRefs())
            logger.info("    ID:" + entryRef.getId() + ",Entry:" + entryRef.getSelectedNumber());
    }

    public TicketRefLookupService getOnlineTicketLookupService() {
        return onlineTicketLookupService;
    }

    public void setOnlineTicketLookupService(TicketRefLookupService onlineTicketLookupService) {
        this.onlineTicketLookupService = onlineTicketLookupService;
    }

    public BaseGameInstanceDao getGameInstanceDao() {
        return gameInstanceDao;
    }

    public void setGameInstanceDao(BaseGameInstanceDao gameInstanceDao) {
        this.gameInstanceDao = gameInstanceDao;
    }

}
