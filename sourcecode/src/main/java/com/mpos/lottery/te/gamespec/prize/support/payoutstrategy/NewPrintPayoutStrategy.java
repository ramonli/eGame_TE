package com.mpos.lottery.te.gamespec.prize.support.payoutstrategy;

import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTamperProofTicket;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewPrintPayoutStrategy extends AbstractPayoutStrategy {
    private Log logger = LogFactory.getLog(NewPrintPayoutStrategy.class);
    private BaseEntryDao baseEntryDao;
    private BaseTicketDao baseTicketDao;
    private NewPrintTicketDao newPrintTicketDao;

    @Override
    public final void confirm(Context respCtx, GameType supportedGameType, List<? extends BaseTicket> hostTickets)
            throws ApplicationException {
        BaseTicket soldTicket = hostTickets.get(0);
        // check if the ticket has been paid. Anyway if a ticket has been
        // paid, the first ticket record of multiple-draw should be 'paid'
        // status.
        if (BaseTicket.STATUS_PAID != soldTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_CONFIRM_NONPAYEDTIKCET, "LottoTicket(serialNo="
                    + soldTicket.getSerialNo() + ") hasn't been paid, can NOT confim payout.");
        }
        for (BaseTicket ticket : hostTickets) {
            if (BaseTicket.STATUS_ACCEPTED == ticket.getStatus()) {
                /**
                 * if it has passed freezing time of game instance, that says the ticket may has joined winner analysis,
                 * no need to confirm payout any more, go into 'company absorption' process.
                 */
                if (new Date().before(ticket.getGameInstance().getFreezeTime())) {
                    ticket.setStatus(BaseTicket.STATUS_INVALID);
                    ticket.setUpdateTime(respCtx.getTransaction().getCreateTime());
                    ticket.setCountInPool(false);
                    this.customizeConfirmPayoutOldTicket(respCtx, ticket);
                    this.getBaseTicketDao().update(ticket);

                    // update NewPrintTicket
                    NewPrintTicket newTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
                    if (newTicket != null) {
                        newTicket.setStatus(NewPrintTicket.STATUS_CONFIRMED);
                        this.getNewPrintTicketDao().update(newTicket);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Game instance(id=" + ticket.getGameInstance().getId() + "), "
                                + ticket.getGameInstance()
                                + " has passed freezing time, its associated ticket may has joined "
                                + "winner analysis, mark it as company absorption.");
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------
    /**
     * A subclass should override this method to implement required logic of a specific payout mode.
     */
    @Override
    protected void doPayout(Context<?> respCtx, GameType supportedGameType, PrizeDto prize,
            List<? extends BaseTicket> hostTickets) throws ApplicationException {
        // update tickets
        for (BaseTicket ticket : prize.getPaidTickets()) {
            /**
             * If internal payout, only when all game instances are payout-started, this operation will be allowed.
             */
            if (respCtx.isInternalCall()) {
                // won't update the status and updatetime of ticket, otherwise
                // it may affect activity report.
                ticket.setAbsorptionPaid(true);
            } else {
                ticket.setStatus(BaseTicket.STATUS_PAID);
                ticket.setUpdateTime(respCtx.getTransaction().getCreateTime());
            }
        }
        this.getBaseTicketDao().update(prize.getPaidTickets());

        // generate new tickets if needed
        if (prize.getFutureTickets().size() > 0) {
            String newSerialNo = this.getUuidService().getTicketSerialNo(supportedGameType.getType());
            List<BaseEntry> newEntries = this.assembleEntries(supportedGameType, prize.getWinningTicket(), newSerialNo);
            String extendText = BaseTicket.generateExtendText(newEntries);
            for (int i = 0; i < prize.getFutureTickets().size(); i++) {
                BaseTicket ticket = prize.getFutureTickets().get(i);
                int multiDraw = i == 0 ? prize.getFutureTickets().size() : 0;
                prize.getGeneratedTickets().add(
                        this.generateNewTicket(respCtx, prize, ticket, newSerialNo, newEntries, extendText, multiDraw,
                                supportedGameType));
            }
            // insert new generated tickets and entries
            this.getBaseTicketDao().insert(prize.getGeneratedTickets());
            this.getBaseEntryDao().insert(newEntries);

            // generate new printed physical ticket
            BaseTicket newPrintTicket = (BaseTicket) prize.getGeneratedTickets().get(0).clone();
            newPrintTicket.setMultipleDraws(prize.getGeneratedTickets().size());
            newPrintTicket.setTotalAmount(newPrintTicket.getTotalAmount().multiply(
                    new BigDecimal(newPrintTicket.getMultipleDraws())));
            newPrintTicket.getGameInstance().setGameId(newPrintTicket.getGameInstance().getGame().getId());
            // set last draw number
            newPrintTicket.setLastDrawNo(prize.getGeneratedTickets().get(prize.getGeneratedTickets().size() - 1)
                    .getGameInstance().getNumber());
            this.customizePrintedPhysicalTicket(respCtx, newPrintTicket);

            prize.setNewPrintTicket(newPrintTicket);

            // generate new print log
            this.logNewPrintTicket(respCtx, prize);
        }

    }

    protected void customizePrintedPhysicalTicket(Context<?> respCtx, BaseTicket newPrintTicket) {
        // template method for subclass to customized assembling
    }

    @Override
    protected void doReversal(Context respCtx, GameType supportedGameType, List<? extends BaseTicket> hostTickets)
            throws ApplicationException {
        for (BaseTicket ticket : hostTickets) {
            // only 'paid' ticket will be restored
            if (BaseTicket.STATUS_PAID == ticket.getStatus()) {
                ticket.setStatus(BaseTicket.STATUS_ACCEPTED);
                ticket.setUpdateTime(new Date());
            }
        }
        this.getBaseTicketDao().update(hostTickets);
        this.reverseNewPrintTicket(respCtx, supportedGameType, hostTickets.get(0).getSerialNo());
    }

    protected void reverseNewPrintTicket(Context respCtx, GameType supportedGameType, String ticketSerialNo)
            throws ApplicationException {
        // set new printed ticket to false
        NewPrintTicket newTicket = this.getNewPrintTicketDao().getByOldTicket(ticketSerialNo);
        if (newTicket != null) {
            List<? extends BaseTicket> newTickets = this.lookupTickets(supportedGameType,
                    newTicket.getNewTicketSerialNo());
            if (newTickets.size() > 0) {
                int firstState = newTickets.get(0).getGameInstance().getState();
                if (firstState == LottoGameInstance.STATE_NEW || firstState == LottoGameInstance.STATE_ACTIVE) {
                    for (BaseTicket ticket : newTickets) {
                        ticket.setStatus(LottoTicket.STATUS_INVALID);
                        ticket.setCountInPool(false);
                        this.customizeReversePayoutNewTicket(respCtx, ticket);
                        this.getBaseTicketDao().update(ticket);
                    }
                }
                // or the ticket has joined winning analysis, can NOT reverse
                // it.
            }
            // mark the NewPrintTicket as invalid
            newTicket.setStatus(NewPrintTicket.STATUS_REVERSED);
            this.getNewPrintTicketDao().update(newTicket);
        }
    }

    /**
     * Template method for subclass to override to customize the process of payout confirm.
     */
    protected void customizeConfirmPayoutOldTicket(Context<?> respCtx, BaseTicket oldTicket) {
        // template method
    }

    protected void customizeReversePayoutNewTicket(Context<?> respCtx, BaseTicket newTicket) {
        // template method
    }

    protected void logNewPrintTicket(Context respCtx, PrizeDto prize) throws ApplicationException {
        // check if there is already one record with same old ticket
        NewPrintTicket newTicket = this.getNewPrintTicketDao().getByOldTicket(prize.getWinningTicket().getSerialNo());
        if (newTicket == null) {
            // save new print tickets map
            newTicket = new NewPrintTicket();
            newTicket.setId(this.getUuidService().getGeneralID());
            newTicket.setCreateTime(new Date());
            newTicket.setOldTicketSerialNo(prize.getWinningTicket().getSerialNo());
            newTicket.setNewTicketSerialNo(prize.getNewPrintTicket().getSerialNo());
            newTicket.setStatus(NewPrintTicket.STATUS_WAITCONFIRM);
            this.getNewPrintTicketDao().insert(newTicket);
        } else {
            newTicket.setNewTicketSerialNo(prize.getNewPrintTicket().getSerialNo());
            newTicket.setUpdateTime(new Date());
            newTicket.setStatus(NewPrintTicket.STATUS_WAITCONFIRM);
            this.getNewPrintTicketDao().update(newTicket);
        }
    }

    /**
     * Generate new ticket record for future game instance.
     */
    protected BaseTicket generateNewTicket(Context<?> respCtx, PrizeDto prize, BaseTicket hostTicket,
            String newSerialNo, List<BaseEntry> newEntries, String extendTxt, int multiDraw, GameType supportedGameType)
            throws ApplicationException {
        BaseTicket generatedTicket = (BaseTicket) hostTicket.clone();
        // assemble generated ticket
        generatedTicket.setId(this.getUuidService().getGeneralID());
        generatedTicket.setMultipleDraws(multiDraw);
        generatedTicket.setRawSerialNo(newSerialNo);
        /**
         * make the create time of new-generated ticket same as old one, otherwise many reports of M.Lottery will fail.
         * As M.Lottery count sale by create time, it will count the new-generated ticket as sale of the date of payout.
         */
        // generatedTicket.setCreateTime(new Date());
        generatedTicket.setUpdateTime(respCtx.getTransaction().getCreateTime());
        generatedTicket.setPIN(hostTicket.getPIN());
        generatedTicket.setStatus(BaseTicket.STATUS_ACCEPTED);
        generatedTicket.setTransaction(respCtx.getTransaction());
        generatedTicket.setValidationCode(BaseTicket.generateValidationCode());
        generatedTicket.setWinning(false);
        // make operator/merchant/device same with sale transaction
        generatedTicket.setOperatorId(hostTicket.getTransaction().getOperatorId());
        generatedTicket.setMerchantId(hostTicket.getTransaction().getMerchantId());
        generatedTicket.setDevId(hostTicket.getTransaction().getDeviceId());
        // generatedTicket.setOperatorId(respCtx.getTransaction().getOperatorId());
        // generatedTicket.setMerchantId(respCtx.getTransaction().getMerchantId());
        // generatedTicket.setDevId(respCtx.getTransaction().getDeviceId());
        generatedTicket.setTransType(TransactionType.PAYOUT.getRequestType());
        // set barcode
        generatedTicket.setBarcode(new Barcoder(supportedGameType.getType(), generatedTicket.getRawSerialNo())
                .getBarcode());
        // lookup entries
        generatedTicket.setEntries(newEntries);
        this.customizeGenerateNewTicket(respCtx, generatedTicket, hostTicket, extendTxt);

        return generatedTicket;
    }

    protected void customizeGenerateNewTicket(Context<?> respCtx, BaseTicket generatedTicket, BaseTicket hostTicket,
            String extendTxt) {
        // template method for subclass to customize the new ticket generation
        // logic

        // set extendTxt if TamperProofTicket
        if (generatedTicket instanceof BaseTamperProofTicket) {
            ((BaseTamperProofTicket) generatedTicket).setExtendText(extendTxt);
        }
    }

    /**
     * Game type specific implementation can override this method.
     */
    protected List<? extends BaseEntry> lookupEntries(GameType supportedGameType, BaseTicket hostTicket)
            throws ApplicationException {
        return this.getBaseEntryDao().findByTicketSerialNo(supportedGameType.getTicketEntryType(),
                hostTicket.getSerialNo(), false);
    }

    /**
     * Game type specific implementation can override this method.
     */
    protected List<? extends BaseTicket> lookupTickets(GameType supportedGameType, String ticketSerialNo)
            throws ApplicationException {
        return this.getBaseTicketDao().findBySerialNo(supportedGameType.getTicketType(), ticketSerialNo, false);
    }

    protected List<BaseEntry> assembleEntries(GameType supportedGameType, BaseTicket oldHostTicket, String newSerialNo)
            throws ApplicationException {
        List<? extends BaseEntry> entries = this.lookupEntries(supportedGameType, oldHostTicket);
        List<BaseEntry> newEntries = new ArrayList<BaseEntry>();
        for (BaseEntry entry : entries) {
            BaseEntry newEntry = (BaseEntry) entry.clone();
            newEntry.setCreateTime(new Date());
            newEntry.setUpdateTime(newEntry.getCreateTime());
            newEntry.setId(this.getUuidService().getGeneralID());
            newEntry.setTicketSerialNo(BaseTicket.encryptSerialNo(newSerialNo));
            newEntries.add(newEntry);
        }
        return newEntries;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public NewPrintTicketDao getNewPrintTicketDao() {
        return newPrintTicketDao;
    }

    public void setNewPrintTicketDao(NewPrintTicketDao newPrintTicketDao) {
        this.newPrintTicketDao = newPrintTicketDao;
    }

}
