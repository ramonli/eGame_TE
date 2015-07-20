package com.mpos.lottery.te.gameimpl.extraball.prize.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.port.Context;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class NewPrintPayoutStrategy extends AbstractPayoutStrategy {
    private BaseEntryDao baseEntryDao;
    private NewPrintTicketDao newPrintTicketDao;

    @Override
    public String key() {
        return Game.TYPE_UNDEF + "-" + BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET;
    }

    protected final void generateTicket(Context<?> respCtx, Prize prize) throws ApplicationException {
        if (prize.getFutureTickets().size() <= 0) {
            return;
        }

        String newSerialNo = this.getUuidService().getTicketSerialNo(
                prize.getWinningTicket().getGameInstance().getGame().getType());
        // generate tickets
        List<BaseTicket> newTickets = new LinkedList<BaseTicket>();
        for (BaseTicket ticket : prize.getFutureTickets()) {
            BaseTicket newTicket = (BaseTicket) ticket.clone();
            newTicket.setId(this.getUuidService().getGeneralID());
            newTicket.setTransaction(respCtx.getTransaction());
            newTicket.setRawSerialNo(newSerialNo);
            newTicket.setCreateTime(new Date());
            newTicket.setUpdateTime(newTicket.getCreateTime());
            newTicket.setStatus(BaseTicket.STATUS_ACCEPTED);
            this.doAssembleNewTicket(newTicket, ticket);

            newTickets.add(newTicket);

            this.getBaseTicketDao().insert(newTicket);
        }
        // generate entries
        List newEntries = this.generateEntries(respCtx, prize, newTickets.get(0).getSerialNo(), prize
                .getWinningTicket().getSerialNo());
        // generate new print log
        this.generateNewPrintLog(respCtx, newTickets.get(0).getSerialNo(), prize.getWinningTicket().getSerialNo());
        // assemble new generated tickets.
        prize.getGeneratedTickets().add(generateNewPrintPhysicalTicket(respCtx, prize, newTickets, newEntries));
    }

    /**
     * Construct a BaseTicket of given game type.
     */
    protected abstract BaseTicket generateNewPrintPhysicalTicket(Context<?> respCtx, Prize prize,
            List<BaseTicket> newTickets, List newEntries);

    protected void generateNewPrintLog(Context<?> respCtx, String newSerialNo, String oldSerialNo)
            throws ApplicationException {
        NewPrintTicket newPrintLog = new NewPrintTicket(this.getUuidService().getGeneralID(), oldSerialNo, newSerialNo);
        this.getNewPrintTicketDao().insert(newPrintLog);
    }

    protected final List<BaseEntry> generateEntries(Context<?> respCtx, Prize prize, String newSerialNo,
            String oldSerialNo) throws ApplicationException {
        List entries = this.lookupEntries(oldSerialNo);
        List newEntries = new LinkedList();
        for (Object o : entries) {
            BaseEntry entry = (BaseEntry) o;
            BaseEntry newEntry = (BaseEntry) entry.clone();
            newEntry.setId(this.getUuidService().getGeneralID());
            newEntry.setTicketSerialNo(newSerialNo);
            newEntry.setCreateTime(new Date());
            newEntry.setUpdateTime(newEntry.getCreateTime());

            newEntries.add(newEntry);
            this.getBaseEntryDao().insert(newEntry);
        }
        return newEntries;
    }

    protected abstract List lookupEntries(String serialNo) throws ApplicationException;

    /**
     * Subclss may need to assemble ticket instance with more information.
     * 
     * @param newTicket
     *            The new generated ticket which will be persisted to underlying database
     * @param templateTicket
     *            provide information for newly generated ticket.
     */
    protected void doAssembleNewTicket(BaseTicket newTicket, BaseTicket templateTicket) {
        // Templete method for subclass
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

    public NewPrintTicketDao getNewPrintTicketDao() {
        return newPrintTicketDao;
    }

    public void setNewPrintTicketDao(NewPrintTicketDao newPrintTicketDao) {
        this.newPrintTicketDao = newPrintTicketDao;
    }

}
