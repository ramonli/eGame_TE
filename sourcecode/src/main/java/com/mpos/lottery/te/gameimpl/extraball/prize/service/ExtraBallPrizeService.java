package com.mpos.lottery.te.gameimpl.extraball.prize.service;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.extraball.prize.ExtraBallWinningItem;
import com.mpos.lottery.te.gameimpl.extraball.prize.support.PayoutStrategy;
import com.mpos.lottery.te.gameimpl.extraball.prize.support.PayoutStrategyFactory;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.PrizeItem;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallGameInstance;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallOperationParameter;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gameimpl.lotto.draw.LottoOperationParameter;
import com.mpos.lottery.te.gameimpl.lotto.draw.domain.LottoGameInstance;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;
import com.mpos.lottery.te.gamespec.prize.NewPrintTicket;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.BaseWinningItemDao;
import com.mpos.lottery.te.gamespec.prize.dao.NewPrintTicketDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.service.TaxService;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ExtraBallPrizeService extends AbstractReversalOrCancelStrategy implements PrizeService {
    protected Log logger = LogFactory.getLog(ExtraBallPrizeService.class);
    // SPRINT DEPENDENCIES
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;
    private BaseGameInstanceDao baseGameInstanceDao;
    private BaseWinningItemDao baseWinningItemDao;
    private BaseJpaDao baseJpaDao;
    private PayoutDao payoutDao;
    private UUIDService uuidService;
    private PayoutStrategyFactory payoutStrategyFactory;
    private CreditService creditService;
    private MerchantService merchantService;
    // private PrizeGroupItemDao prizeGroupItemDao;
    // private PrizeLevelDao prizeLevelDao;
    private TaxService taxService;
    private NewPrintTicketDao newPrintTicketDao;

    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        if (TransactionType.PAYOUT.getRequestType() != targetTrans.getType()) {
            throw new SystemException("can't reverse a transaction of type:" + targetTrans.getType());
        }

        List<Payout> payouts = this.getPayoutDao().getByTransactionAndStatus(targetTrans.getId(), Payout.STATUS_PAID);
        // calculate credit amount
        BigDecimal credit = new BigDecimal("0");
        for (Payout payout : payouts) {
            // only care about amount after tax.
            credit = credit.add(payout.getTotalAmount());
            payout.setStatus(Payout.STATUS_REVERSED);
            this.getPayoutDao().update(payout);
        }
        // restore the status of multiple-draw ticket, or the winning analysis
        // process
        // will ignore these tickets.
        String ticketSerialNo = targetTrans.getTicketSerialNo();
        if (ticketSerialNo == null) {
            throw new SystemException("transacion(payout).ticketserialNo can NOT be null");
        }
        List<ExtraBallTicket> tickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class, ticketSerialNo,
                true);
        for (ExtraBallTicket ticket : tickets) {
            // only 'paid' ticket will be restored
            if (BaseTicket.STATUS_PAID == ticket.getStatus()) {
                ticket.setStatus(LottoTicket.STATUS_ACCEPTED);
                this.getBaseTicketDao().update(ticket);
            }
        }
        this.reverseNewPrintTicket(ticketSerialNo);
        // if
        // (MLotteryContext.getInstance().getSysConfiguration().isRestoreCreditLevelWhenPayout())
        // {
        // restore credit amount
        this.getCreditService().credit(respCtx.getTransaction().getOperatorId(),
                respCtx.getTransaction().getMerchantId(), credit, respCtx.getTransaction().getGameId(), false, false,
                false);

        return false;
    }

    /**
     * Customize the routine key for <code>ReversalOrCancelStrategy</code>.
     */
    @Override
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(Game.TYPE_EXTRABALL, TransactionType.PAYOUT.getRequestType(), null);
    }

    @Override
    public Prize enquiry(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) clientTicket;
        List<ExtraBallTicket> dbTickets = this.assemblePhysicalTicket(respCtx, ticket);
        // assemble the prize information
        return this.assemblePrize(dbTickets, ticket, true);
    }

    @Override
    public Prize payout(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) clientTicket;
        List<ExtraBallTicket> dbTickets = this.assemblePhysicalTicket(respCtx, ticket);

        this.beforeCalculatePrize(dbTickets, ticket);
        // assemble the prize information
        Prize prize = this.assemblePrize(dbTickets, ticket, true);
        this.beforePayout(respCtx, dbTickets.get(0), prize);

        // retrieve lotto operator parameter
        PayoutStrategy strategy = this.getPayoutStrategyFactory().lookupPayoutStrategy(
                ticket.getGameInstance().getGame().getType(), prize.getPayoutMode());
        strategy.payout(respCtx, prize);

        // update credit level of merchant
        BigDecimal creditAmount = prize.getPrizeAmount().subtract(prize.getTaxAmount());
        if (prize.getReturnAmount() != null) {
            creditAmount = creditAmount.add(prize.getReturnAmount());
        }
        // retrieve game identifier
        String gameId = prize.getWinningTicket().getGameInstance().getGame().getId();
        this.getCreditService().credit(respCtx.getTransaction().getOperatorId(),
                respCtx.getTransaction().getMerchantId(), creditAmount, gameId, true, false, false);
        return prize;
    }

    @Override
    public void confirmPayout(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        List<ExtraBallTicket> dbTickets = this.assemblePhysicalTicket(respCtx, clientTicket);
        BaseTicket boughtTicket = dbTickets.get(0);
        boughtTicket.setGameInstance(this.getBaseGameInstanceDao().findById(ExtraBallGameInstance.class,
                boughtTicket.getGameInstance().getId(), false));
        BaseOperationParameter operationParam = this.getBaseJpaDao().findById(ExtraBallOperationParameter.class,
                boughtTicket.getGameInstance().getGame().getOperatorParameterId());

        /**
         * check if the ticket has been payed. Anyway if a ticket has been payed, the first ticket record of
         * multiple-draw should be 'payed' status.
         */
        if (LottoTicket.STATUS_PAID == boughtTicket.getStatus()) {
            // BigDecimal refundAmount = new BigDecimal("0");
            for (ExtraBallTicket ticket : dbTickets) {
                if (LottoTicket.STATUS_ACCEPTED == ticket.getStatus()) {
                    // if the status of game instance is 'in progress of winning
                    // analysis' or 'payout started',
                    int drawState = ticket.getGameInstance().getState();
                    if (LottoGameInstance.STATE_WINANALYSIS_STARTED == drawState
                            || LottoGameInstance.STATE_PAYOUT_STARTED == drawState) {
                        // tickets have joined winning analysis...absorption
                    } else {
                        ticket.setStatus((operationParam.getPayoutMode() == LottoOperationParameter.PAYOUTMODE_REFUND)
                                ? LottoTicket.STATUS_RETURNED
                                : LottoTicket.STATUS_INVALID);
                        ticket.setCountInPool(false);
                        ticket.setUpdateTime(new Date());
                        this.getBaseTicketDao().update(ticket);

                        if (operationParam.getPayoutMode() == LottoOperationParameter.PAYOUTMODE_PRINTNEWTICKET) {
                            // update NewPrintTicket
                            NewPrintTicket newTicket = this.getNewPrintTicketDao().getByOldTicket(ticket.getSerialNo());
                            if (newTicket != null) {
                                newTicket.setStatus(NewPrintTicket.STATUS_CONFIRMED);
                                this.getNewPrintTicketDao().update(newTicket);
                            }
                        }
                    }
                }
            }
        } else {
            throw new ApplicationException(SystemException.CODE_CONFIRM_NONPAYEDTIKCET, "LottoTicket(serialNo="
                    + clientTicket.getSerialNo() + ") hasn't been paid, can NOT confim refund.");
        }
    }

    // -------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------

    /**
     * Verify pre-conditions before real payout.
     */
    protected void beforePayout(Context<?> respCtx, ExtraBallTicket hostTicket, Prize prize)
            throws ApplicationException {
        // - check max payout amount of merchant
        this.getMerchantService().allowPayout(respCtx, hostTicket.getGameInstance().getGame(), null,
                prize.getActualAmount());

        // TODO - check whether the merchant has priviledge to pay this prize
        // level
        // int gameType = physicalTicket.getGameInstance().getGame().getType();
        // if (new BigDecimal("0").compareTo(prize.getPrizeAmount()) != 0) {
        // // win normal prize
        // this.getMerchantService().allowPayout(respCtx.getTransaction().getMerchantId(),
        // prize.getWinnedPrizeLevels(), gameType,
        // PrizeGroupItem.GROUP_TYPE_LOTTO);
        // }
    }

    protected void reverseNewPrintTicket(String ticketSerialNo) throws ApplicationException {
        // set new printed ticket to false
        NewPrintTicket newTicket = this.getNewPrintTicketDao().getByOldTicket(ticketSerialNo);
        if (newTicket != null) {
            List<ExtraBallTicket> newTickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                    newTicket.getNewTicketSerialNo(), true);
            if (newTickets.size() > 0) {
                BaseGameInstance gameInstance = this.getBaseGameInstanceDao().findById(ExtraBallGameInstance.class,
                        newTickets.get(0).getGameInstance().getId());
                int firstState = gameInstance.getState();
                if (firstState == LottoGameInstance.STATE_NEW || firstState == LottoGameInstance.STATE_ACTIVE) {
                    for (ExtraBallTicket ticket : newTickets) {
                        ticket.setStatus(LottoTicket.STATUS_INVALID);
                        ticket.setCountInPool(false);
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
     * Verify the pre-condition before calculating prize.
     */
    protected void beforeCalculatePrize(List<ExtraBallTicket> dbTickets, ExtraBallTicket physicalTicket)
            throws ApplicationException {
        ExtraBallTicket boughtTicket = dbTickets.get(0);
        // - check if this ticket has been paid
        if (BaseTicket.STATUS_PAID == boughtTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_INVALID_PAYOUT, "LottoTicket(serialNo="
                    + physicalTicket.getSerialNo() + ") has been payed.");
        }

        // - check the last payout time(it should be counted from the final
        // game draw)
        ExtraBallGameInstance lastGameInstance = this.getBaseGameInstanceDao().findById(ExtraBallGameInstance.class,
                dbTickets.get(dbTickets.size() - 1).getGameInstance().getId(), false);
        lastGameInstance.isPastLastClaimDay();
    }

    /**
     * Assemble a physical ticket information based on serial number.
     * 
     * @return The host ticket entities.
     */
    protected List<ExtraBallTicket> assemblePhysicalTicket(Context<?> respCtx, BaseTicket ticket)
            throws ApplicationException {
        List<ExtraBallTicket> tickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                ticket.getSerialNo(), true);
        if (tickets.size() == 0) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET, "can NOT find ticket with serialNO="
                    + ticket.getSerialNo());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Start to enquiry prize for ticket(serialNo=" + ticket.getSerialNo() + ",multipleDraw="
                    + tickets.size() + ").");
        }
        /**
         * the returned ticket list is ordered by game-draw.number, so the game draw associates with the 1st ticket is
         * the game draw in which customer bought the ticket.
         */
        ExtraBallTicket boughtTicket = tickets.get(0);
        ExtraBallGameInstance boughtGameInstance = this.getBaseGameInstanceDao().findById(ExtraBallGameInstance.class,
                boughtTicket.getGameInstance().getId(), false);
        ticket.setGameInstance(boughtGameInstance);
        ticket.setMultipleDraws(tickets.size());
        ticket.setTotalAmount(boughtTicket.getTotalAmount().multiply(new BigDecimal(ticket.getMultipleDraws())));

        // assemble transaction
        respCtx.getTransaction().setGameId(boughtGameInstance.getGame().getId());
        respCtx.getTransaction().setTicketSerialNo(boughtTicket.getSerialNo());

        return tickets;
    }

    /**
     * Assemble PrizeDto instance from ticket.serialNo. Below precondition must be satisfied:
     * <ol>
     * <li>The ticket exists</li>
     * <li>At least the buying gamedraw is 'payout started', customer can go to claim prize at any time. To
     * multiple-draw ticket, some later gamedraws maybe not 'payout started'</li>
     * </ol>
     * The prize will be assembled from WinningItems.
     */
    protected Prize assemblePrize(List<ExtraBallTicket> dbTickets, ExtraBallTicket physicalTicket, boolean isPayout)
            throws ApplicationException {
        BaseGameInstance boughtDraw = physicalTicket.getGameInstance();
        // - only 'payout started' game draw can do enquiry or payout. To
        // multiple-draw ticket, at least
        // one game draw should be 'payout started'.
        if (BaseGameInstance.STATE_PAYOUT_STARTED != boughtDraw.getState()) {
            throw new ApplicationException(SystemException.CODE_DRAW_NOTPAYOUTSTARTED, "The game draw(drawNo="
                    + boughtDraw.getNumber() + ") of buying ticket(serialNo=" + physicalTicket.getSerialNo()
                    + ") is not 'payout started':" + boughtDraw.getState());
        }

        // - check all game draws, whether some game draws are in progress of
        // winning analysis or payout blocked.
        for (int i = 0; i < dbTickets.size(); i++) {
            ExtraBallGameInstance gameInstance = this.getBaseGameInstanceDao().findById(ExtraBallGameInstance.class,
                    dbTickets.get(i).getGameInstance().getId(), false);
            dbTickets.get(i).setGameInstance(gameInstance);
            if ((new Date().after(gameInstance.getEndTime()) && BaseGameInstance.STATE_PAYOUT_STARTED != gameInstance
                    .getState())) {
                throw new ApplicationException(SystemException.CODE_INPROGRESSOF_WINNINGANALYSIS, "game draw(id="
                        + gameInstance.getId() + ",number=" + gameInstance.getNumber()
                        + ") is in progress of winning analysis, please try later.");
            }
            // check below condition only when real payout
            if (isPayout) {
                if (gameInstance.isPayoutBlocked()) {
                    throw new ApplicationException(SystemException.CODE_SUSPEND_PAYOUT, "game instance(id="
                            + gameInstance.getId() + ",number=" + gameInstance.getNumber()
                            + ") has been payout suspended, please try later.");
                }
                if (dbTickets.get(i).isPayoutBlocked()) {
                    throw new ApplicationException(SystemException.CODE_TICKET_BLOCKPAYOUT, "LottoTicket(serialNo="
                            + physicalTicket.getSerialNo() + ") has been blocked for payout.");
                }
            }
        }

        // calculate prize amount
        Prize prize = calculatePrizeAmount(dbTickets, physicalTicket);
        if (prize.getActualAmount().compareTo(new BigDecimal("0")) == 0) {
            throw new ApplicationException(SystemException.CODE_NOTWINNINGTICKET, "LottoTicket(serialNo="
                    + physicalTicket.getSerialNo() + ") isn't a winning ticket.");
        }
        // assemble game information
        prize.setGame(physicalTicket.getGameInstance().getGame());

        // assemble isVerifyPIN
        // if
        // (!boughtTicket.getPIN().equals(MLotteryContext.getInstance().getIgnoredPIN()))
        // {
        // prize.setVerifyPIN(true);
        // }

        return prize;
    }

    /**
     * Calculate prize amount of the given ticket.
     */
    protected Prize calculatePrizeAmount(List<ExtraBallTicket> dbTickets, ExtraBallTicket physicalTicket)
            throws ApplicationException {
        ExtraBallTicket boughtTicket = dbTickets.get(0);
        BaseGameInstance boughtDraw = physicalTicket.getGameInstance();
        // initialize prize
        Prize prize = new Prize();
        prize.setWinningTicket(physicalTicket);

        BaseOperationParameter operationParam = this.getBaseJpaDao().findById(ExtraBallOperationParameter.class,
                boughtTicket.getGameInstance().getGame().getOperatorParameterId());
        prize.setPayoutMode(operationParam.getPayoutMode());
        BigDecimal returnAmount = new BigDecimal("0");
        /**
         * handle ticket one by one...each game instance will has its own valid version
         */
        for (ExtraBallTicket t : dbTickets) {
            if (BaseGameInstance.STATE_PAYOUT_STARTED == t.getGameInstance().getState()) {
                long lastSuccessfulVersion = t.getGameInstance().getVersion();
                // get all winning items
                List<ExtraBallWinningItem> winItems = this.getBaseWinningItemDao()
                        .findByGameInstanceAndSerialNoAndVersion(ExtraBallWinningItem.class,
                                t.getGameInstance().getId(), physicalTicket.getSerialNo(), lastSuccessfulVersion);
                PrizeItem prizeItem = this.handleWinItemsOfSingleGameInstance(winItems, prize, t.getGameInstance());

                // stat prize amount of the winning ticket
                prize.setPrizeAmount(prize.getPrizeAmount().add(prizeItem.getPrizeAmount()));
                prize.setTaxAmount(prize.getTaxAmount().add(prizeItem.getTaxAmount()));
                prize.setActualAmount(prize.getActualAmount().add(prizeItem.getActualAmount()));

                prize.getPaidTickets().add(t);
            } else {
                prize.getFutureTickets().add(t);
                if (BaseOperationParameter.PAYOUTMODE_REFUND == operationParam.getPayoutMode()) {
                    returnAmount = returnAmount.add(t.getTotalAmount());
                }
            }
        }
        prize.setReturnAmount(returnAmount);
        // actualAmount := prizeAmount + returnAmount - taxAmount
        prize.setActualAmount(prize.getActualAmount().add(returnAmount));
        return prize;
    }

    /**
     * Handle each winning item, and calculate tax if needed.
     * 
     * @return the prize item of a single game instance.
     */
    protected PrizeItem handleWinItemsOfSingleGameInstance(List<ExtraBallWinningItem> winningItems, Prize prize,
            BaseGameInstance gameInstance) throws ApplicationException {
        // lookup prizeItem
        PrizeItem prizeItem = prize.lookupPrizeItem(gameInstance.getKey());
        if (prizeItem == null) {
            prizeItem = new PrizeItem();
            prizeItem.setGameInstance(gameInstance);
            prize.addPrizeItem(prizeItem);
        }

        // handle winning item
        for (ExtraBallWinningItem winningItem : winningItems) {
            // check if the winning item is valid
            if (!winningItem.isValid()) {
                throw new ApplicationException(SystemException.CODE_CANCELED_WINNING_TICKET,
                        "can not payout a invalid winning ticket(serialNo=" + prize.getWinningTicket().getSerialNo()
                                + ").");
            }
            // stat prize amount of a single game instance
            // if the tax-method of Game is TAXMETHOD_PAYOUT, then we need
            // to calculate the tax
            prizeItem.setPrizeAmount(prizeItem.getPrizeAmount().add(winningItem.getPrizeAmount()));
            Game game = gameInstance.getGame();
            if (Game.TAXMETHOD_PAYOUT == game.getTaxMethod()) {
                BigDecimal tax = this.getTaxService().tax(winningItem.getPrizeAmount(), game.getId());
                prizeItem.setTaxAmount(prizeItem.getTaxAmount().add(tax));
                prizeItem.setActualAmount(prizeItem.getActualAmount().add(winningItem.getPrizeAmount().subtract(tax)));
            } else if (Game.TAXMETHOD_ANALYSIS == game.getTaxMethod()) {
                prizeItem.setTaxAmount(prizeItem.getTaxAmount().add(winningItem.getTaxAmount()));
                prizeItem.setActualAmount(prizeItem.getActualAmount().add(winningItem.getActualAmount()));
            } else {
                throw new RuntimeException("Unsupported tax method:" + game.getTaxMethod() + " of game(id="
                        + game.getId() + ")");
            }
        }
        return prizeItem;
    }

    // -------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -------------------------------------------------------------

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public BaseWinningItemDao getBaseWinningItemDao() {
        return baseWinningItemDao;
    }

    public void setBaseWinningItemDao(BaseWinningItemDao baseWinningItemDao) {
        this.baseWinningItemDao = baseWinningItemDao;
    }

    public BaseGameInstanceDao getBaseGameInstanceDao() {
        return baseGameInstanceDao;
    }

    public void setBaseGameInstanceDao(BaseGameInstanceDao baseGameInstanceDao) {
        this.baseGameInstanceDao = baseGameInstanceDao;
    }

    public TaxService getTaxService() {
        return taxService;
    }

    public void setTaxService(TaxService taxService) {
        this.taxService = taxService;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

    public PayoutStrategyFactory getPayoutStrategyFactory() {
        return payoutStrategyFactory;
    }

    public void setPayoutStrategyFactory(PayoutStrategyFactory payoutStrategyFactory) {
        this.payoutStrategyFactory = payoutStrategyFactory;
    }

    public NewPrintTicketDao getNewPrintTicketDao() {
        return newPrintTicketDao;
    }

    public void setNewPrintTicketDao(NewPrintTicketDao newPrintTicketDao) {
        this.newPrintTicketDao = newPrintTicketDao;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

}
