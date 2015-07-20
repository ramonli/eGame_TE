package com.mpos.lottery.te.gameimpl.bingo.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoEntry;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoEntryRef;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicketRef;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoEntryRefDao;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoTicketRefDao;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.AbstractTicketService;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class BingoSaleService extends AbstractTicketService {
    private static Log logger = LogFactory.getLog(BingoSaleService.class);
    private BingoEntryRefDao entryRefDao;
    private BingoTicketRefDao ticketRefDao;
    private TicketRefLookupService ticketRefLookupService;

    @Override
    public GameType supportedGameType() {
        return GameType.BINGO;
    }

    /**
     * When play bingo game, player doesn't need to pick number. If no number picked, system will pick some for player
     * automatically. However the ticket information is always use a in-advance generated one.
     */
    @Override
    protected void customAssembleClientTicket(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        BingoTicket clientBingoTicket = (BingoTicket) clientTicket;
        BingoTicketRef ticketRef = null;
        if (clientTicket.getEntries().size() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Player doesn't pick any numbers, system will pick some");
            }
            ticketRef = this.getTicketRefLookupService().lookupTicket(respCtx, (BingoTicket) clientTicket, true);
        } else {
            ticketRef = this.getTicketRefLookupService().lookupTicket(respCtx, (BingoTicket) clientTicket, false);
        }

        // assemble bingo ticket
        clientBingoTicket.merge(ticketRef);
        // update referenced ticket
        ticketRef.setUpdateTime(respCtx.getTransaction().getCreateTime());
        ticketRef.setStatus(BingoTicketRef.STATUS_SOLD);
        this.getTicketRefDao().update(ticketRef);
        for (BingoEntryRef entryRef : ticketRef.getEntryRefs()) {
            entryRef.setUpdateTime(respCtx.getTransaction().getCreateTime());
            entryRef.setStatus(BingoTicketRef.STATUS_SOLD);
        }
        this.getEntryRefDao().update(ticketRef.getEntryRefs());
    }

    @Override
    protected void customizeAssembleTicket(BaseTicket generatedTicket, BaseTicket clientTicket) {
        for (BaseEntry entry : generatedTicket.getEntries()) {
            ((BingoEntry) entry).setTicketId(generatedTicket.getId());
            ((BingoEntry) entry).setGameInstanceId(generatedTicket.getGameInstance().getId());
        }

    }

    /**
     * Mark those referenced ticket/entry as cancelled as well.
     * 
     * @see com.mpos.lottery.te.gamespec.sale.service.AbstractTicketService#doSuccessfulCancel(com.mpos.lottery.te.port.Context,
     *      com.mpos.lottery.te.gamespec.sale.BaseTicket)
     */
    @Override
    protected void doSuccessfulCancel(Context<?> respCtx, BaseTicket hostSoldTicket) throws ApplicationException {
        BingoTicket hostTicket = (BingoTicket) hostSoldTicket;
        BingoTicketRef ticketRef = this.getTicketRefDao().findBySerialNo(hostTicket.getImportedSerialNo());
        if (ticketRef == null) {
            throw new SystemException("NO entity(" + BingoTicketRef.class + ") found by importedSerialNo="
                    + hostTicket.getImportedSerialNo());
        }

        ticketRef.setUpdateTime(respCtx.getTransaction().getCreateTime());
        ticketRef.setStatus(BingoTicketRef.STATUS_CANCEL);
        if (hostTicket.getEntries().size() == 0) {
            List<BingoEntry> hostEntries = this.getBaseEntryDao().findByTicketSerialNo(BingoEntry.class,
                    hostTicket.getSerialNo(), false);
            for (BingoEntry hostEntry : hostEntries) {
                // system pick numbers
                if (hostEntry.getEntryRefId() != null) {
                    BingoEntryRef entryRef = this.entryRefDao.findById(BingoEntryRef.class, hostEntry.getEntryRefId());
                    entryRef.setUpdateTime(respCtx.getTransaction().getCreateTime());
                    entryRef.setStatus(BingoTicketRef.STATUS_CANCEL);
                    this.getEntryRefDao().update(entryRef);
                }
            }
        }
    }

    // -------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -------------------------------------------------------------------

    public TicketRefLookupService getTicketRefLookupService() {
        return ticketRefLookupService;
    }

    public void setTicketRefLookupService(TicketRefLookupService ticketRefLookupService) {
        this.ticketRefLookupService = ticketRefLookupService;
    }

    public BingoEntryRefDao getEntryRefDao() {
        return entryRefDao;
    }

    public void setEntryRefDao(BingoEntryRefDao entryRefDao) {
        this.entryRefDao = entryRefDao;
    }

    public BingoTicketRefDao getTicketRefDao() {
        return ticketRefDao;
    }

    public void setTicketRefDao(BingoTicketRefDao ticketRefDao) {
        this.ticketRefDao = ticketRefDao;
    }

}
