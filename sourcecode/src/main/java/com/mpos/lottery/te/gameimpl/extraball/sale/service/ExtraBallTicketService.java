package com.mpos.lottery.te.gameimpl.extraball.sale.service;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallEntry;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallFunType;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallGameInstance;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallOperationParameter;
import com.mpos.lottery.te.gameimpl.extraball.sale.ExtraBallTicket;
import com.mpos.lottery.te.gameimpl.extraball.sale.dao.ExtraBallAlgorithmDao;
import com.mpos.lottery.te.gameimpl.lotto.draw.LottoOperationParameter;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.dao.BaseGameInstanceDao;
import com.mpos.lottery.te.gamespec.game.dao.GameDao;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.gamespec.sale.service.CompositeTicketService;
import com.mpos.lottery.te.gamespec.sale.web.QPEnquiryDto;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;
import com.mpos.lottery.te.trans.service.TransactionService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ExtraBallTicketService extends AbstractReversalOrCancelStrategy implements CompositeTicketService {
    private Log logger = LogFactory.getLog(ExtraBallTicketService.class);
    // SPRING DEPENDENCIES
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;
    private GameInstanceService gameInstanceService;
    private BaseGameInstanceDao baseGameInstanceDao;
    private CreditService creditService;
    private MerchantService merchantService;
    private BaseJpaDao baseJpaDao;
    private UUIDService uuidService;
    private GameDao gameDao;
    private TransactionService transactionService;
    // private IncentiveStrategyFactory incentiveStrategyFactory;
    private ExtraBallAlgorithmDao extraBallAlgorithmDao;

    @Override
    public BaseTicket enquiry(Context<?> respCtx, BaseTicket clientTicket, boolean fetchEntries)
            throws ApplicationException {
        List<ExtraBallTicket> tickets = this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
                clientTicket.getSerialNo(), true);
        if (tickets.size() == 0) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET, "can NOT find ticket(serialNO="
                    + clientTicket.getSerialNo() + ") from underlying database.");
        }
        /**
         * new a ticket, other entity manager will persist it to underlying database automatically when commit
         * transaction.
         */
        ExtraBallTicket boughtTicket = tickets.get(0);
        ExtraBallTicket ticket = (ExtraBallTicket) clientTicket;
        ticket.setGameInstance(this.getBaseGameInstanceDao().findById(ExtraBallGameInstance.class,
                boughtTicket.getGameInstance().getId()));
        // set gameId
        Game game = ticket.getGameInstance().getGame();
        game.setFunType(this.getBaseJpaDao().findById(ExtraBallFunType.class, game.getFunTypeId()));
        ticket.setCreateTime(boughtTicket.getCreateTime());
        ticket.setUpdateTime(boughtTicket.getUpdateTime());
        // ticket.setPIN(boughtTicket.getPIN());
        ticket.setStatus(boughtTicket.getStatus());
        ticket.setTransaction(boughtTicket.getTransaction());
        // avoid LazyInitilizationException
        ticket.getTransaction().getId();
        ticket.setWinning(boughtTicket.isWinning());
        ticket.setRawSerialNo(false, BaseTicket.descryptSerialNo(ticket.getSerialNo()));
        // set multiple draws
        ticket.setMultipleDraws(boughtTicket.getMultipleDraws());
        BigDecimal totalAmount = boughtTicket.getTotalAmount().multiply(new BigDecimal(tickets.size()));
        ticket.setTotalAmount(totalAmount);

        // lookup lastDrawNo
        BaseGameInstance lastDraw = this.getBaseGameInstanceDao().findById(ExtraBallGameInstance.class,
                tickets.get(tickets.size() - 1).getGameInstance().getId());
        ticket.setLastDrawNo(lastDraw.getNumber());

        return ticket;
    }

    @Override
    public List<? extends BaseTicket> sell(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        ExtraBallTicket ticket = (ExtraBallTicket) clientTicket;
        // validate ticket
        if (ticket.getMultipleDraws() < 1) {
            throw new ApplicationException(SystemException.CODE_MULTIPLEDRAW_NEGATIVE, "The multipleDraw("
                    + ticket.getMultipleDraws() + ") is 1 at least.");
        }
        // check game draw, and set current draw to ticket.
        List<? extends BaseGameInstance> gameInstances = this.lookupGameInstance(respCtx, ticket);
        // generate serial number for ticket.
        ticket.setRawSerialNo(this.getUuidService().getTicketSerialNo(gameInstances.get(0).getGame().getType()));
        // check pre-condition of sale
        this.beforeSale(respCtx, ticket);
        // apply incentive strategy
        this.countIncentive(respCtx, ticket.getTotalAmount(), false);

        // update the credit level
        if (LottoTicket.TICKET_TYPE_NORMAL == ticket.getTicketType()) {
            this.getCreditService().credit(ticket.getTransaction().getOperatorId(),
                    ticket.getTransaction().getMerchantId(), ticket.getTotalAmount(),
                    ticket.getGameInstance().getGame().getId(), false, true, ticket.isSoldByCreditCard());
        }

        // generate new tickets
        List<ExtraBallTicket> tickets = this.generateTicket(ticket, gameInstances);

        // // update instantaneous sale
        // for (LottoTicket t : tickets) {
        // this.snapshotSale(respCtx, t, true);
        // }
        return tickets;
    }

    @Override
    public boolean cancelByTicket(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        throw new UnsupportedOperationException("Rewrite this method");
        // List<ExtraBallTicket> tickets =
        // this.getBaseTicketDao().findBySerialNo(ExtraBallTicket.class,
        // clientTicket.getSerialNo(), true);
        // if (tickets.size() == 0) {
        // this.getTransactionService().pendTransaction(TransactionType.CANCEL_BY_TICKET.getRequestType(),
        // clientTicket.getSerialNo(), respCtx.getTransaction().getDeviceId(),
        // respCtx.getTransaction().getTraceMessageId());
        // return false;
        // }
        // /**
        // * A ticket can be canceled only when the game-draw in which a
        // customer
        // * bought the ticket is active and between start selling time and stop
        // * selling time, or the ticket is frozen. The returned ticket list is
        // * order by game-draw.number, so the game-draw associates with the
        // first
        // * ticket is the game-draw in which customer bought this ticket.
        // */
        // BaseGameInstance currentDraw =
        // this.getBaseJpaDao().findById(ExtraBallGameInstance.class,
        // tickets.get(0).getGameInstance().getId());
        // respCtx.getTransaction().setGameId(currentDraw.getGame().getId());
        // respCtx.getTransaction().setTicketSerialNo(clientTicket.getSerialNo());
        //
        // boolean isCancelDeclined = false;
        // // check the status of current game draw
        // Date now = new Date();
        // if (now.before(currentDraw.getFreezeTime())) {
        // if (currentDraw.getState() == LottoGameInstance.STATE_INACTIVE) {
        // throw new ApplicationException(SystemException.CODE_NO_ACTIVE_DRAW,
        // "Game instance(number="
        // + currentDraw.getNumber() + ",gameId=" +
        // currentDraw.getGame().getId()
        // + ") is not active.");
        // }
        // if (currentDraw.getState() >= LottoGameInstance.STATE_PAYOUT_STARTED)
        // {
        // // TODO: reject canceling request
        // }
        // }
        // else {
        // isCancelDeclined = true;
        // }
        // /**
        // * check if ticket has been canceled. Guarantee there are only one
        // * successful cancel transaction associates with a ticket, or
        // settlement
        // * will fail.
        // */
        // ExtraBallTicket boughtTicket = tickets.get(0);
        // if (LottoTicket.STATUS_CANCELED == boughtTicket.getStatus()
        // || LottoTicket.STATUS_CANCEL_DECLINED == boughtTicket.getStatus()) {
        // throw new
        // ApplicationException(SystemException.CODE_CANCEL_CANCELED_TICKET,
        // "Can NOT cancel a cancel/cancel declined ticket(status=" +
        // boughtTicket.getStatus()
        // + ") repeatedly.");
        // }
        //
        // // update credit
        // if (BaseTicket.TICKET_TYPE_NORMAL == boughtTicket.getTicketType()) {
        // // restore the credit of merchants
        // Transaction saleTrans = boughtTicket.getTransaction();
        // this.getMerchantService().credit(saleTrans.getOperatorId(),
        // saleTrans.getMerchantId(),
        // boughtTicket.getTotalAmount().multiply(new
        // BigDecimal(tickets.size())),
        // respCtx.getTransaction().getGameId(), true, true,
        // boughtTicket.isSoldByCreditCard());
        // }
        // // resotre incentive counter
        // BigDecimal totalAmount = boughtTicket.getTotalAmount().multiply(new
        // BigDecimal(tickets.size()));
        // this.countIncentive(respCtx, totalAmount, true);
        //
        // // update ticket
        // for (ExtraBallTicket ticket : tickets) {
        // ticket.setStatus(isCancelDeclined ?
        // LottoTicket.STATUS_CANCEL_DECLINED
        // : LottoTicket.STATUS_CANCELED);
        // ticket.setBatchNo(respCtx.getTransaction().getBatchNumber());
        // ticket.setTransaction(respCtx.getTransaction());
        // ticket.setCountInPool(isCancelDeclined ? true : false);
        // ticket.setUpdateTime(new Date());
        // }
        // this.getBaseTicketDao().update(tickets);
        //
        // // update total_amount field in te_transaction after cancel ticket
        // respCtx.getTransaction().setTotalAmount(totalAmount);
        //
        // return isCancelDeclined;
    }

    /**
     * A implement of cancel by transaction.
     */
    @Override
    public boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        if (TransactionType.SELL_TICKET.getRequestType() != targetTrans.getType()) {
            throw new SystemException("can't reverse a transaction of type:" + targetTrans.getType());
        }

        boolean isCancelDecline = false;
        // assemble a LottoTicket
        BaseTicket ticket = new ExtraBallTicket();
        ticket.setSerialNo(targetTrans.getTicketSerialNo());
        ticket.setTransaction(targetTrans);
        isCancelDecline = this.cancelByTicket(respCtx, ticket);
        if (isCancelDecline) {
            respCtx.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        }
        return isCancelDecline;
    }

    @Override
    public QPEnquiryDto enquiryQP(Context<?> respCtx, QPEnquiryDto dto) throws ApplicationException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Customize the routine key for <code>ReversalOrCancelStrategy</code>.
     */
    @Override
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(Game.TYPE_EXTRABALL, TransactionType.SELL_TICKET.getRequestType(), null);
    }

    @Override
    public GameType supportedGameType() {
        return GameType.EXTRABALL;
    }

    // --------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------

    /**
     * When sell extraball ticket, its total amount will be counted to LG incentive counter.
     */
    public void countIncentive(Context<?> respCtx, BigDecimal totalAmount, boolean isCancel)
            throws ApplicationException {
        // Lookup the base amount of LG game
        // There should be only one LG game for LOTTO
        Game game = this.getGameDao().findSingleByType(Game.TYPE_LOTT, false);
        if (logger.isDebugEnabled()) {
            logger.debug("Found lotto game(id=" + game.getId() + ").");
        }
        LottoOperationParameter lottoOpParam = this.getBaseJpaDao().findById(LottoOperationParameter.class,
                game.getOperatorParameterId(), false);
        // IncentiveStrategy incentStrategy =
        // this.getIncentiveStrategyFactory().getIncentiveStrategy(
        // respCtx.getProtocalVersion());
        // int counter =
        // totalAmount.divide(lottoOpParam.getBaseAmount()).intValue();
        // incentStrategy.counter(respCtx, isCancel ? -1 * counter : counter);
    }

    /**
     * Generate ticket according to request.
     */
    private List<ExtraBallTicket> generateTicket(ExtraBallTicket ticket, List<? extends BaseGameInstance> gameInstances)
            throws ApplicationException {
        // save ticket
        List<ExtraBallTicket> tickets = this.assembleMultiplDrawTickets(ticket, gameInstances);
        this.getBaseTicketDao().insert(tickets);

        // prepare and save entries
        this.getBaseJpaDao().insert(tickets.get(0).getEntries());
        if (logger.isDebugEnabled()) {
            logger.debug("Generate ticket(serialNo=" + ticket.getSerialNo() + ") successfully.");
        }

        return tickets;
    }

    /**
     * If a ticket is multipleDraws, then multiple ticket records will be saved.
     */
    protected List<ExtraBallTicket> assembleMultiplDrawTickets(ExtraBallTicket ticket,
            List<? extends BaseGameInstance> gameInstances) throws ApplicationException {
        // assemble entries
        this.assembleEntries(ticket, ticket.getEntries());

        List<ExtraBallTicket> tickets = new LinkedList<ExtraBallTicket>();
        BigDecimal singleDrawAmount = ticket.getTotalAmount().divide(new BigDecimal(gameInstances.size()), 2,
                BigDecimal.ROUND_HALF_UP);
        for (int i = 0; i < ticket.getMultipleDraws(); i++) {
            /**
             * For a multiple draw ticket, only the first entity will be set the value of requested multple draw, all
             * other will be set to 0.
             */
            int multpleDraws = ticket.getMultipleDraws();
            if (i != 0) {
                multpleDraws = 0;
            }
            // must new a instance
            ExtraBallTicket t = new ExtraBallTicket(this.getUuidService().getGeneralID(), gameInstances.get(i).getId(),
                    ticket.getTransaction(), ticket.getSerialNo(), singleDrawAmount, multpleDraws);
            t.setGameInstance(gameInstances.get(i));
            t.setEntries(ticket.getEntries());

            tickets.add(t);
        }

        return tickets;
    }

    /**
     * Save lotto/racing.... entries of ticket. Before saving, the identifier of entry should be assigned.
     */
    protected void assembleEntries(ExtraBallTicket ticket, List entries) throws ApplicationException {
        int index = 1;
        // assign identifier to Entry
        for (Object entry : entries) {
            ExtraBallEntry lEntry = (ExtraBallEntry) entry;
            lEntry.setId(this.getUuidService().getGeneralID());
            lEntry.setTicketSerialNo(ticket.getSerialNo());
            lEntry.setCreateTime(new Date());
            lEntry.setEntryNo((index++) + "");
            lEntry.setUpdateTime(lEntry.getCreateTime());
            lEntry.setArgorithm(this.getExtraBallAlgorithmDao().findByType(lEntry.getBetOption()));
        }
    }

    /**
     * Check the pre-conditions before sale.
     */
    private void beforeSale(Context respCtx, ExtraBallTicket ticket) throws ApplicationException {
        // check the status of current game draw
        Game game = ticket.getGameInstance().getGame();
        // check if the operator can sell this game
        Transaction trans = ticket.getTransaction();
        trans.setGameId(game.getId());
        // OperatorCommission comm =
        // this.getOperatorCommissionDao().getByOperatorAndMerchantAndGame(
        // trans.getOperatorId(), trans.getMerchantId(), game.getId());
        // if (comm == null) {
        // throw new SystemException(SystemException.CODE_OPERATOR_NOPRIVILEDGE,
        // "operator(id="
        // + trans.getOperatorId() +
        // ") has no priviledge to sell ticket of game '" + game.getId()
        // + "'.");
        // }
        // check whether this sale exceeds the allowed multi-draw
        Merchant merchant = this.getMerchantService().getMerchant(trans.getMerchantId());
        if (ticket.getMultipleDraws() > merchant.getAllowedMultiDraw()) {
            throw new ApplicationException(SystemException.CODE_EXCEED_ALLOWD_MULTI_DRAW, "The multi-draw("
                    + ticket.getMultipleDraws() + ") of ticket(" + ticket.getSerialNo()
                    + ") has exceeds the allowed number(" + merchant.getAllowedMultiDraw() + ") of merchant(id="
                    + trans.getMerchantId() + ").");
        }

        // validate ticket format and amount etc
        ticket.validate(this.getBaseJpaDao().findById(ExtraBallFunType.class, game.getFunTypeId(), false), this
                .getBaseJpaDao().findById(ExtraBallOperationParameter.class, game.getOperatorParameterId(), false));
    }

    /**
     * Check if game draw exist, then the transaction will be accepted only below two conditions if fulfilled:
     * <ul>
     * <li>current time is between start selling time and stop selling time.</li>
     * <li>the game draw is not inactive.</li>
     * </ul>
     * 
     * @param ticket
     *            The sold ticket.
     * @return the required future game instances, they are ordered by stop selling time.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    protected List<? extends BaseGameInstance> lookupGameInstance(Context respCtx, ExtraBallTicket ticket)
            throws ApplicationException {
        BaseGameInstance gameInstance = ticket.getGameInstance();
        List<? extends BaseGameInstance> gameInstances = this.getGameInstanceService().enquirySaleReady(respCtx,
                gameInstance.getGame().getId(), gameInstance.getNumber(), ticket.getMultipleDraws());
        ticket.setGameInstance(gameInstances.get(0));
        ticket.setLastDrawNo(gameInstances.get(gameInstances.size() - 1).getNumber());
        return gameInstances;
    }

    // --------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------

    public MerchantService getMerchantService() {
        return merchantService;
    }

    public GameInstanceService getGameInstanceService() {
        return gameInstanceService;
    }

    public void setGameInstanceService(GameInstanceService gameInstanceService) {
        this.gameInstanceService = gameInstanceService;
    }

    public void setMerchantService(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public GameDao getGameDao() {
        return gameDao;
    }

    public void setGameDao(GameDao gameDao) {
        this.gameDao = gameDao;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // public IncentiveStrategyFactory getIncentiveStrategyFactory() {
    // return incentiveStrategyFactory;
    // }
    //
    // public void setIncentiveStrategyFactory(IncentiveStrategyFactory
    // incentiveStrategyFactory) {
    // this.incentiveStrategyFactory = incentiveStrategyFactory;
    // }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

    public ExtraBallAlgorithmDao getExtraBallAlgorithmDao() {
        return extraBallAlgorithmDao;
    }

    public void setExtraBallAlgorithmDao(ExtraBallAlgorithmDao extraBallAlgorithmDao) {
        this.extraBallAlgorithmDao = extraBallAlgorithmDao;
    }

    public BaseGameInstanceDao getBaseGameInstanceDao() {
        return baseGameInstanceDao;
    }

    public void setBaseGameInstanceDao(BaseGameInstanceDao baseGameInstanceDao) {
        this.baseGameInstanceDao = baseGameInstanceDao;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

}
