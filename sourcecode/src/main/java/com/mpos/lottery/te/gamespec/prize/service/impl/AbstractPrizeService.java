package com.mpos.lottery.te.gamespec.prize.service.impl;

import com.google.gson.Gson;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.gamespec.prize.BasePrizeObject;
import com.mpos.lottery.te.gamespec.prize.BaseWinningItem;
import com.mpos.lottery.te.gamespec.prize.PrizeGroupItem;
import com.mpos.lottery.te.gamespec.prize.PrizeLevel;
import com.mpos.lottery.te.gamespec.prize.PrizeLevelItem;
import com.mpos.lottery.te.gamespec.prize.dao.BaseWinningItemDao;
import com.mpos.lottery.te.gamespec.prize.dao.BaseWinningStatisticsDao;
import com.mpos.lottery.te.gamespec.prize.dao.PrizeLevelDao;
import com.mpos.lottery.te.gamespec.prize.service.PrizeService;
import com.mpos.lottery.te.gamespec.prize.service.TaxService;
import com.mpos.lottery.te.gamespec.prize.support.luckydraw.LuckyDrawWinningItem;
import com.mpos.lottery.te.gamespec.prize.support.luckydraw.LuckyGameInstance;
import com.mpos.lottery.te.gamespec.prize.support.payoutstrategy.PayoutStrategy;
import com.mpos.lottery.te.gamespec.prize.support.payoutstrategy.PayoutStrategyFactory;
import com.mpos.lottery.te.gamespec.prize.web.PrizeAmount;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelObjectItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.dao.BalanceTransactionsDao;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.web.PayoutLevelAllowRequest;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

public abstract class AbstractPrizeService extends AbstractReversalOrCancelStrategy implements PrizeService {
    private Log logger = LogFactory.getLog(AbstractPrizeService.class);
    // Spring dependencies
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;
    private GameInstanceService gameInstanceService;
    private CreditService creditService;
    private MerchantService merchantService;
    private BaseJpaDao baseJpaDao;
    private BaseWinningItemDao baseWinningItemDao;
    private BaseWinningStatisticsDao baseWinningStatisticsDao;
    private TaxService taxService;
    private PayoutStrategyFactory payoutStrategyFactory;
    private PrizeLevelDao prizeLevelDao;
    @Resource(name = "balanceTransactionsDao")
    private BalanceTransactionsDao balanceTransactionsDao;

    /**
     * @see com.mpos.lottery.te.trans.domain.logic.ReversalOrCancelStrategy#supportedReversalRoutineKey() .
     */
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(this.supportedGameType().getType(), TransactionType.PAYOUT.getRequestType(), null);
    }

    @Override
    public final PrizeDto enquiry(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        if (logger.isDebugEnabled()) {
            logger.debug("Enquiry prize information of ticket:" + clientTicket.getSerialNo());
        }
        List<? extends BaseTicket> hostTickets = this.lookupTickets(clientTicket.getSerialNo());

        BaseTicket soldTicket = hostTickets.get(0);
        BaseGameInstance soldGameInstance = soldTicket.getGameInstance();

        // check whether game instance are ready for payout
        this.getGameInstanceService().allowPayout(respCtx, hostTickets, true);
        // check whether ticket is allowed for payout
        soldTicket.allowPayout(respCtx, clientTicket, true, this.lookupTicketEntriess(soldTicket.getSerialNo()));
        this.determineWinningStatus(clientTicket, hostTickets);

        // calculate PrizeDto
        PrizeDto prize = this.assemblePrize(respCtx, clientTicket, hostTickets, true);
        if (BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET == prize.getPayoutMode()) {
            prize.setReturnAmount(null);
        }

        // Tell client whether PIN verification will be preocessed when payout.
        if (!soldTicket.getPIN().equals(MLotteryContext.getInstance().getIgnoredPIN())) {
            prize.setVerifyPIN(true);
        }
        // update transaction
        respCtx.getTransaction().setGameId(soldGameInstance.getGame().getId());
        respCtx.getTransaction().setTotalAmount(prize.getActualAmount());
        respCtx.getTransaction().setTicketSerialNo(soldTicket.getSerialNo());

        return prize;
    }

    @Override
    public final PrizeDto payout(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        List<? extends BaseTicket> hostTickets = this.lookupTickets(clientTicket.getSerialNo());
        this.determineWinningStatus(clientTicket, hostTickets);

        BaseTicket soldTicket = hostTickets.get(0);
        BaseGameInstance soldGameInstance = soldTicket.getGameInstance();
        // check whether game instance are ready for payout
        this.getGameInstanceService().allowPayout(respCtx, hostTickets, false);
        // check whether ticket is allowed for payout
        soldTicket.allowPayout(respCtx, clientTicket, false, this.lookupTicketEntriess(soldTicket.getSerialNo()));
        // check whether the distributor of sale and payout is same
        this.checkDistributor(soldTicket.getTransaction(), respCtx.getTransaction());

        PrizeDto prize = this.assemblePrize(respCtx, clientTicket, hostTickets, false);

        BigDecimal totalAmount = prize.getActualAmount();
        if (prize.getReturnAmount() != null
                && BaseOperationParameter.PAYOUTMODE_PRINTNEWTICKET == prize.getPayoutMode()) {
            /**
             * Consider below case:
             * <p/>
             * a player buy multiple-draw ticket(3 advanced draw), and the first draw is closed, and the ticket win
             * nothing, the player come to claim prize, for sure system should print new ticket successfully. In this
             * case the actual amount is 0, if we don't count return amount, a exception "isn't a winning ticket" will
             * be thrown out.
             */
            totalAmount = totalAmount.add(prize.getReturnAmount());
            // shouldn't response return amount to client.
            prize.setReturnAmount(null);
        }
        // lucky amount won't be counted into prize amount.
        if ((totalAmount.add(prize.getLuckyPrizeAmount())).compareTo(new BigDecimal("0")) == 0) {
            throw new ApplicationException(SystemException.CODE_NOTWINNINGTICKET, "Ticket(serialNo="
                    + clientTicket.getSerialNo() + ") isn't a winning ticket.");
        }

        // check max payout amount of merchant
        this.getMerchantService().allowPayout(respCtx, soldTicket.getGameInstance().getGame(),
                this.assemblePayoutLevelAllowRequests(respCtx, prize, prize.getWinningTicket()),
                prize.getActualAmount());

        // apply Payout Strategy
        PayoutStrategy strategy = this.getPayoutStrategyFactory().lookupPayoutStrategy(prize.getPayoutMode());
        strategy.payout(respCtx, this.supportedGameType(), prize, hostTickets);

        // update transactio

        respCtx.getTransaction().setGameId(soldGameInstance.getGame().getId());
        respCtx.getTransaction().setTotalAmount(prize.getActualAmount());
        respCtx.getTransaction().setTicketSerialNo(soldTicket.getSerialNo());

        // restore credit
        // update credit level of merchant
        BigDecimal creditAmount = prize.getActualAmount();
        // retrieve game identifier
        Object updatedOperator = this.getCreditService().credit(respCtx.getTransaction().getOperatorId(),
                respCtx.getTransaction().getMerchantId(), creditAmount, soldGameInstance.getGame().getId(), true,
                false, false, respCtx.getTransaction());
        // 是否計算傭金
        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            BalanceTransactions tempBalanceTransactions = new Gson().fromJson(respCtx.getTransaction()
                    .getTransMessage().getRequestMsg(), BalanceTransactions.class);
            BalanceTransactions operatorBalanceTransactions = balanceTransactionsDao.assembleBalanceTransactions(
                    respCtx, creditAmount);
            operatorBalanceTransactions.setGameId(soldGameInstance.getGameId());
            operatorBalanceTransactions.setCommissionAmount(tempBalanceTransactions.getCommissionAmount());
            operatorBalanceTransactions.setCommissionRate(tempBalanceTransactions.getCommissionRate());
            if (updatedOperator == null) {
                throw new SystemException(SystemException.CODE_OPERATOR_TOPUP_IGNORED, "THe payout to operator(id="
                        + respCtx.getOperatorId() + " will be ignored.");
            } else {
                if (updatedOperator instanceof Merchant) {
                    Merchant merchant = (Merchant) updatedOperator;
                    BalanceTransactions merchantBalanceTransactions = balanceTransactionsDao
                            .assembleBalanceTransactions(respCtx, creditAmount);
                    merchantBalanceTransactions.setOwnerId(String.valueOf(merchant.getId()));
                    merchantBalanceTransactions.setOwnerType(BalanceTransactions.OWNER_TYPE_MERCHANT);
                    balanceTransactionsDao.insert(merchantBalanceTransactions);
                }
            }
            balanceTransactionsDao.insert(operatorBalanceTransactions);
        }
        return prize;
    }

    @Override
    public final void confirmPayout(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        List<? extends BaseTicket> hostTickets = this.lookupTickets(clientTicket.getSerialNo());
        BaseOperationParameter opParam = this.lookupOperationParameter(hostTickets.get(0).getGameInstance().getGame());

        // apply Payout Strategy
        PayoutStrategy strategy = this.getPayoutStrategyFactory().lookupPayoutStrategy(opParam.getPayoutMode());
        strategy.confirm(respCtx, this.supportedGameType(), hostTickets);
    }

    @Override
    public final boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        // restore the status of multiple-draw ticket, or the winning analysis
        // process will ignore these tickets.
        String ticketSerialNo = targetTrans.getTicketSerialNo();
        if (ticketSerialNo == null) {
            throw new SystemException("transacion(payout).ticketserialNo can NOT be null");
        }
        // apply Payout reversal Strategy
        List<? extends BaseTicket> hostTickets = this.lookupTickets(ticketSerialNo);
        BaseOperationParameter opParam = this.lookupOperationParameter(hostTickets.get(0).getGameInstance().getGame());
        PayoutStrategy strategy = this.getPayoutStrategyFactory().lookupPayoutStrategy(opParam.getPayoutMode());
        strategy.reverse(respCtx, this.supportedGameType(), hostTickets, targetTrans);

        // restore credit level
        this.getCreditService().credit(targetTrans.getOperatorId(), targetTrans.getMerchantId(),
                targetTrans.getTotalAmount(), hostTickets.get(0).getGameInstance().getGame().getId(), false, false,
                false, targetTrans);

        if (MLotteryContext.getInstance().getSysConfiguration().isSupportCommissionCalculation()) {
            // cancelled balance transaction
            BalanceTransactions tempBalanceTransactions = new Gson().fromJson(targetTrans.getTransMessage()
                    .getRequestMsg(), BalanceTransactions.class);
            BalanceTransactions operatorBalanceTransactions = balanceTransactionsDao.assembleBalanceTransactions(
                    respCtx, tempBalanceTransactions.getTransactionAmount());
            operatorBalanceTransactions.setOriginalTransType(targetTrans.getType());
            operatorBalanceTransactions.setCommissionAmount(BalanceTransactions.ZERO.subtract(tempBalanceTransactions
                    .getCommissionAmount()));
            operatorBalanceTransactions.setTransactionAmount(tempBalanceTransactions.getTransactionAmount());
            operatorBalanceTransactions.setCommissionRate(tempBalanceTransactions.getCommissionRate());
            balanceTransactionsDao.updateBalanceTransactionsStatusByteTransactionId(targetTrans.getTransMessage()
                    .getTransactionId());
            balanceTransactionsDao.insert(operatorBalanceTransactions);
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // HELPER METHODS
    // -----------------------------------------------------------------------

    /**
     * Check whether the distributor of retailer which sold a ticket allows the tickets to claim prize at other
     * distributor.
     */
    private void checkDistributor(Transaction saleTrans, Transaction payoutTrans) throws ApplicationException {
        Merchant saleDistributor = this.getMerchantService().getMerchant(saleTrans.getMerchantId()).lookupDistributor();
        if (saleDistributor != null && saleDistributor.isSalePayoutUnderSameDistributor()) {
            Merchant payoutDistributor = this.getMerchantService().getMerchant(payoutTrans.getMerchantId())
                    .lookupDistributor();
            if (payoutDistributor != null && saleDistributor.getId() == payoutDistributor.getId()) {
                // they are under same distributor
            } else {
                throw new ApplicationException(SystemException.CODE_SALE_PAYOUT_DIFF_DISTRIBUTOR,
                        "The distributor of sale is " + saleDistributor.getId()
                                + " isn't same with distributor of payout " + payoutDistributor.getId());
            }
        }
    }

    protected PayoutLevelAllowRequest[] assemblePayoutLevelAllowRequests(Context<?> respCtx, PrizeDto prize,
            BaseTicket ticket) {
        // key is the draw type, such as normal draw, lucky draw etc.
        Map<String, PayoutLevelAllowRequest> levelRequestMap = new HashMap<String, PayoutLevelAllowRequest>();
        for (PrizeItemDto prizeItem : prize.getPrizeItems()) {
            PayoutLevelAllowRequest levelRequest = levelRequestMap.get(prizeItem.getType() + "");
            if (levelRequest == null) {
                int gameType = ticket.getGameInstance().getGame().getType();
                if (PrizeGroupItem.GROUP_TYPE_GLOBAL_LUCKYDRAW == prizeItem.getType()) {
                    // for lucky draw, the game type will be fixed.
                    gameType = GameType.LUCKYDRAW.getType();
                }
                levelRequest = new PayoutLevelAllowRequest(new HashSet<Integer>(), gameType, prizeItem.getType());
                levelRequestMap.put(prizeItem.getType() + "", levelRequest);
            }
            for (PrizeLevelItemDto w : prizeItem.getPrizeLevelItems()) {
                levelRequest.getRequestedPrizeLevels().add(Integer.parseInt(w.getPrizeLevel()));
            }
        }
        PayoutLevelAllowRequest[] reqs = new PayoutLevelAllowRequest[levelRequestMap.values().size()];
        levelRequestMap.values().toArray(reqs);
        return reqs;
    }

    /**
     * Calculate prize information of a given ticket.
     */
    protected PrizeDto assemblePrize(Context<?> respCtx, BaseTicket clientTicket,
            List<? extends BaseTicket> hostTickets, boolean isPrizeEnquiry) throws ApplicationException {
        BaseTicket soldTicket = hostTickets.get(0);
        // initial PrizeDto
        PrizeDto prize = this.newPrize(clientTicket, hostTickets);

        // ** Handle normal draw
        if (clientTicket.isWinning()) {
            StopWatch sw = new Log4JStopWatch();
            // handle ticket one by one, that says one game instance by one game
            // instance
            for (BaseTicket t : hostTickets) {
                // only need to check the payout-started game instance
                if (BaseGameInstance.STATE_PAYOUT_STARTED == t.getGameInstance().getState()) {
                    prize.getPaidTickets().add(t);
                    // assemble normal prize information
                    List<? extends BaseWinningItem> winningItems = this.lookupWinningItem(t.getGameInstance().getId(),
                            soldTicket.getSerialNo(), t.getGameInstance().getVersion());
                    if (winningItems.size() == 0) {
                        continue;
                    }

                    PrizeItemDto prizeItem = new PrizeItemDto();
                    prizeItem.setGameInstance(t.getGameInstance());
                    prizeItem.getGameInstance().setGameId(t.getGameInstance().getGame().getId());
                    prizeItem.setType(PrizeGroupItem.GROUP_TYPE_NORMAL_DRAW);
                    prize.addPrizeItem(prizeItem);
                    this.assembleNormalPrizeItem(prize, prizeItem, t, winningItems);

                    // if calculate tax per ticket, do it here
                    this.calculateTaxBasedOnPerTicket(prize, t, prizeItem);
                } else {
                    // regard it as 'return'
                    prize.setReturnAmount(prize.getReturnAmount().add(t.getTotalAmount()));
                    prize.getFutureTickets().add(t);
                }
            }
            sw.stop("Assemble_Normal_Prize", "Assemble normal draw prize of ticket(" + clientTicket.getSerialNo() + ")");
        }

        // ** handle lucky draw
        if (clientTicket.isLuckyWinning()) {
            StopWatch sw = new Log4JStopWatch();
            // assemble lucky prize information
            this.assembleLuckyPrizeItem(prize, clientTicket, isPrizeEnquiry);
            sw.stop("Assemble_Lucky_Prize", "Assemble lucky draw prize of ticket(" + clientTicket.getSerialNo() + ")");
        }

        this.assembleSecondPrize(prize, clientTicket, isPrizeEnquiry);

        /**
         * update the prize statistics of prize information.
         */
        for (PrizeItemDto prizeItem : prize.getPrizeItems()) {
            prize.setPrizeAmount(prize.getPrizeAmount().add(prizeItem.getPrizeAmount()));
            prize.setTaxAmount(prize.getTaxAmount().add(prizeItem.getTaxAmount()));
            prize.setActualAmount(prize.getActualAmount().add(prizeItem.getActualAmount()));
            // the lucky amount is object type prize, only for reference
            prize.setLuckyPrizeAmount(prize.getLuckyPrizeAmount().add(prizeItem.getObjectPrizeAmount()));
        }

        prize.setPayoutMode(this.lookupOperationParameter(soldTicket.getGameInstance().getGame()).getPayoutMode());
        if (BaseOperationParameter.PAYOUTMODE_REFUND == prize.getPayoutMode()) {
            prize.setActualAmount(prize.getActualAmount().add(prize.getReturnAmount()));
        }

        return prize;
    }

    /**
     * If the tax calculation is based on per ticket(per draw), the tax and actual amount must be recalculated once the
     * prize statistics of a game instance has been determined. Otherwise both tax and actual amount will be 0.
     */
    protected void calculateTaxBasedOnPerTicket(PrizeDto prize, BaseTicket t, PrizeItemDto prizeItem) {
        if (Game.TAXMETHOD_PAYOUT == t.getGameInstance().getGame().getTaxMethod()) {
            if (Game.TAXMETHOD_BASE_TICKET == t.getGameInstance().getGame().getTaxMethodBase()) {
                BigDecimal totalPrizeAmountOfLevel = prizeItem.getPrizeAmount();
                // calculate tax based on prize level amount.
                BigDecimal totalTaxAmountOfLevel = this.getTaxService().tax(totalPrizeAmountOfLevel,
                        t.getGameInstance().getGame().getId());
                BigDecimal totalActualAmountOfLevel = totalPrizeAmountOfLevel.subtract(totalTaxAmountOfLevel);
                // set total prize/actual amount
                prizeItem.setTaxAmount(prize.getTaxAmount().add(totalTaxAmountOfLevel));
                prizeItem.setActualAmount(prize.getActualAmount().add(totalActualAmountOfLevel));
            }
        }
    }

    /**
     * Construct and initialize a <code>PrizeDto</code> instance. Subclass can override this method to customize
     * initialization process, for example return a instance of <code>PrizeDto</code>'s subclass.
     * 
     * @param clientTicket
     *            The ticket instance constructed based on client's request.
     * @param hostTickets
     *            The tickets records looked up at the backend.
     * @return a <code>PrizeDto</code> instance.
     */
    protected PrizeDto newPrize(BaseTicket clientTicket, List<? extends BaseTicket> hostTickets) {
        PrizeDto prize = new PrizeDto();
        // calculate total amount of multi-draw ticket.
        clientTicket.setTotalAmount(hostTickets.get(0).getTotalAmount().multiply(new BigDecimal(hostTickets.size())));
        clientTicket.setGameInstance(hostTickets.get(0).getGameInstance());
        prize.setWinningTicket(clientTicket);
        return prize;
    }

    /**
     * Assemble a PrizeItemDto which represents the winning statistics of a specific game instance based on winning
     * items.
     * <p/>
     * NOTE that for normal prize, only cash will be handled, no logic to handle object prize.
     */
    protected void assembleNormalPrizeItem(PrizeDto prize, PrizeItemDto prizeItem, BaseTicket ticket,
            List<? extends BaseWinningItem> winningItems) throws ApplicationException {
        // generate prize level items
        for (BaseWinningItem winningItem : winningItems) {
            // check if the winning item is valid
            if (!winningItem.isValid()) {
                throw new ApplicationException(SystemException.CODE_CANCELED_WINNING_TICKET,
                        "can not payout a invalid winning item(id=" + winningItem.getId() + ").");
            }
            PrizeAmount prizeLevelAmount = this.lookupAnnouncedPrizeAmount(winningItem, ticket);

            // lookup the prize level definition
            PrizeLevelItemDto levelDef = this.assemblePrizeLevelInfo(ticket.getGameInstance(),
                    winningItem.getPrizeLevel(), prizeLevelAmount.getPrizeAmount(), prizeLevelAmount.getTaxAmount(),
                    prizeLevelAmount.getActualAmount(), winningItem.getNumberOfPrize());
            PrizeLevelItemDto existLevelItem = prizeItem.lookupPrizeLevelItem(winningItem.getPrizeLevel(), false);
            if (existLevelItem == null) {
                existLevelItem = levelDef;
                prizeItem.getPrizeLevelItems().add(existLevelItem);
            } else {
                existLevelItem.setNumberOfPrizeLevel(existLevelItem.getNumberOfPrizeLevel()
                        + levelDef.getNumberOfPrizeLevel());
            }

            this.updatePrizeStatPerDraw(prize, prizeItem, levelDef);
        }
    }

    /**
     * Assemble second prize information. After the bingo can put the sales ticket again.Secondary items and cash two
     * modes
     * <p/>
     * Currently only supports the bingo game
     */
    protected void assembleSecondPrize(PrizeDto prize, BaseTicket t, boolean isPrizeEnquiry)
            throws ApplicationException {
    }

    /**
     * Assemble lucky prize information. No matter the ticket is what game type, they all can join a lucky draw.
     * <p/>
     * For lucky draw, the object prize must be considered.
     */
    protected void assembleLuckyPrizeItem(PrizeDto prize, BaseTicket t, boolean isPrizeEnquiry)
            throws ApplicationException {
        // lookup winning items
        List<LuckyDrawWinningItem> luckyWinningItems = this.getBaseWinningItemDao().findBySerialNo(
                LuckyDrawWinningItem.class, t.getSerialNo());
        if (logger.isDebugEnabled()) {
            logger.debug("Handle winnings of lucky draw of ticket(serialNo:" + t.getSerialNo() + "), total "
                    + luckyWinningItems.size() + " winning items found.");
        }
        // handle the lucky winning item one by one...different from normal
        // prize which is handled draw by draw.
        List<PrizeItemDto> luckyPrizeItemList = new ArrayList<PrizeItemDto>();
        for (LuckyDrawWinningItem luckyWinningItem : luckyWinningItems) {
            // check if the winning item is valid
            if (!luckyWinningItem.isValid()) {
                throw new ApplicationException(SystemException.CODE_CANCELED_WINNING_TICKET,
                        "can not payout a invalid lucky winning item(id=" + luckyWinningItem.getId() + ").");
            }
            /**
             * lookup lucky-draw game instance ... refactor to reduce the enquiry of game instance, as multiple winning
             * items may associate with same game instance
             * <p/>
             * FIX - no worry, check the API doc of JPA2.0.
             * <p/>
             * Find by primary key. Search for an entity of the specified class and primary key. If the entity instance
             * is contained in the persistence context, it is returned from there.
             */
            LuckyGameInstance luckyGameInstance = this.baseJpaDao.findById(LuckyGameInstance.class,
                    luckyWinningItem.getGameInstanceId());
            if (luckyGameInstance == null) {
                throw new DataIntegrityViolationException("No lucky draw found by id("
                        + luckyWinningItem.getGameInstanceId() + ").");
            }

            PrizeItemDto prizeItem = prize.lookupPrizeItem(luckyGameInstance.getKey());
            if (prizeItem == null) {
                prizeItem = new PrizeItemDto();
                prizeItem.setGameInstance(luckyGameInstance);
                prizeItem.getGameInstance().setGameId(luckyGameInstance.getGame().getId());
                prizeItem.setType(PrizeGroupItem.GROUP_TYPE_GLOBAL_LUCKYDRAW);
                prize.addPrizeItem(prizeItem);
                luckyPrizeItemList.add(prizeItem);
            }
            PrizeLevelItemDto prizeLevelItemDto = assemblePrizeItemFromBasePrizeLeveDef(
                    luckyGameInstance.getPrizeLogicId(), luckyWinningItem, prizeItem);

            this.updatePrizeStatPerDraw(prize, prizeItem, prizeLevelItemDto);

            if (!isPrizeEnquiry) {
                luckyWinningItem.setStatus(BaseTicket.STATUS_PAID);
            }
        }
        this.getBaseJpaDao().update(luckyWinningItems);

        for (PrizeItemDto luckyPrizeItem : luckyPrizeItemList) {
            this.calculateTaxBasedOnPerTicket(prize, t, luckyPrizeItem);
        }
    }

    /**
     * Assemble a <code>PrizeItemDto</code> which represents a single winning item based on the base prize level
     * definition.
     * 
     * @return the prize level definition of the given <code>winningItem</code>.
     */
    protected PrizeLevelItemDto assemblePrizeItemFromBasePrizeLeveDef(String prizeLogicId, BaseWinningItem winningItem,
            PrizeItemDto prizeItem) throws ApplicationException {
        // lookup prize level definition...for lucky draw, prize level
        // information must be retrieved from bd_prize_level.
        PrizeLevel prizeLevel = this.getPrizeLevelDao().findByPrizeLogicAndLevel(prizeLogicId,
                winningItem.getPrizeLevel());
        // PrizeLevelItemDto only represents cash prize information
        PrizeLevelItemDto prizeLevelItemDto = null;
        // sort prize-level-Item to guarantee cash prize will be handled first,
        // otherwise the object prize-level-item may be erased during the
        // handling of cash prize-level-item.
        Collections.sort(prizeLevel.getLevelItems(), new Comparator<PrizeLevelItem>() {

            @Override
            public int compare(PrizeLevelItem o1, PrizeLevelItem o2) {
                return o1.getPrizeType() - o2.getPrizeType();
            }
        });

        for (PrizeLevelItem prizeLevelItem : prizeLevel.getLevelItems()) {
            if (PrizeLevel.PRIZE_TYPE_CASH == prizeLevelItem.getPrizeType()) {
                // for cash, the prizeLevelItem.getNumberOfObject() should
                // always be 1.
                prizeLevelItemDto = this.assemblePrizeLevelInfo(prizeItem.getGameInstance(),
                        winningItem.getPrizeLevel(), prizeLevelItem.getPrizeAmount(), prizeLevelItem.getTaxAmount(),
                        prizeLevelItem.getActualAmount(), winningItem.getNumberOfPrize());
            } else {
                if (prizeLevelItemDto == null) {
                    prizeLevelItemDto = new PrizeLevelItemDto();
                    // the sum of object prize will be wrote to
                    // PrizeDto.luckyPrizeAmount...all object prize information
                    // will be stayed in PrizeLevelObjectItem.
                    prizeLevelItemDto.setPrizeLevel(prizeLevel.getPrizeLevel() + "");
                    prizeLevelItemDto.setNumberOfPrizeLevel(winningItem.getNumberOfPrize());
                }
                PrizeLevelObjectItemDto objectItemDto = new PrizeLevelObjectItemDto();
                objectItemDto.setObjectId(prizeLevelItem.getObjectId());
                objectItemDto.setNumberOfObject(prizeLevelItem.getNumberOfObject());
                // retrieve information from bd_prize_object
                BasePrizeObject prizeObject = this.getBaseJpaDao().findById(BasePrizeObject.class,
                        prizeLevelItem.getObjectId());
                // objectItemDto.setObjectName(prizeLevelItem.getObjectName());
                // objectItemDto.setPrice(prizeLevelItem.getPrizeAmount());
                // objectItemDto.setTaxAmount(prizeLevelItem.getTaxAmount());
                objectItemDto.setObjectName(prizeObject.getName());
                objectItemDto.setPrice(prizeObject.getPrice());
                objectItemDto.setTaxAmount(prizeObject.getTax());
                prizeLevelItemDto.getPrizeLevelObjectItems().add(objectItemDto);
            }
        }

        // multiple entries may all win same prize level
        PrizeLevelItemDto existLevelItem = prizeItem.lookupPrizeLevelItem(winningItem.getPrizeLevel(), false);
        if (existLevelItem == null) {
            existLevelItem = prizeLevelItemDto;
            prizeItem.getPrizeLevelItems().add(existLevelItem);
        } else {
            existLevelItem.setNumberOfPrizeLevel(existLevelItem.getNumberOfPrizeLevel()
                    + prizeLevelItemDto.getNumberOfPrizeLevel());
        }
        return prizeLevelItemDto;
    }

    protected PrizeLevelItemDto assemblePrizeLevelInfo(BaseGameInstance gameInstance, int prizeLevel,
            BigDecimal prizeAmount, BigDecimal taxAmount, BigDecimal actualAmount, int numberOfPrize)
            throws ApplicationException {
        PrizeLevelItemDto levelItem = new PrizeLevelItemDto();
        levelItem.setPrizeLevel(prizeLevel + "");
        levelItem.setPrizeAmount(prizeAmount);
        // calculate tax
        /**
         * If ticket wins in a lucky draw, tax calculation method should be based on lucky game instance.
         */
        if (Game.TAXMETHOD_PAYOUT == gameInstance.getGame().getTaxMethod()) {
            if (Game.TAXMETHOD_BASE_BET == gameInstance.getGame().getTaxMethodBase()) {
                // calculate tax based on prize level amount.
                taxAmount = this.getTaxService().tax(prizeAmount, gameInstance.getGame().getId());
                actualAmount = prizeAmount.subtract(taxAmount);
            } else {
                taxAmount = new BigDecimal("0");
                actualAmount = new BigDecimal("0");
            }
        }
        levelItem.setTaxAmount(taxAmount);
        levelItem.setActualAmount(actualAmount);

        levelItem.setNumberOfPrizeLevel(numberOfPrize);
        return levelItem;
    }

    /**
     * Update the prize statistics information of each game instance based on single prize level item.
     */
    protected void updatePrizeStatPerDraw(PrizeDto prize, PrizeItemDto prizeItem, PrizeLevelItemDto levelDef) {
        if (levelDef == null) {
            return;
        }
        // update prizeItem of per game instance
        prizeItem.setPrizeAmount(prizeItem.getPrizeAmount().add(
                levelDef.getPrizeAmount().multiply(new BigDecimal(levelDef.getNumberOfPrizeLevel()))));
        prizeItem.setTaxAmount(prizeItem.getTaxAmount().add(
                levelDef.getTaxAmount().multiply(new BigDecimal(levelDef.getNumberOfPrizeLevel()))));
        prizeItem.setActualAmount(prizeItem.getActualAmount().add(
                levelDef.getActualAmount().multiply(new BigDecimal(levelDef.getNumberOfPrizeLevel()))));
        for (PrizeLevelObjectItemDto objectItem : levelDef.getPrizeLevelObjectItems()) {
            prizeItem.setObjectPrizeAmount(prizeItem.getObjectPrizeAmount().add(
                    SimpleToolkit.mathMultiple(objectItem.getPrice(), new BigDecimal(
                            (objectItem.getNumberOfObject() * levelDef.getNumberOfPrizeLevel()) + ""))));
        }
    }

    /**
     * Lookup the announced prize amount of given prize level
     */
    protected abstract PrizeAmount lookupAnnouncedPrizeAmount(BaseWinningItem winningItem, BaseTicket ticket)
            throws ApplicationException;

    protected List<? extends BaseWinningItem> lookupWinningItem(String gameInstanceId, String ticketSerialNo,
            long version) throws ApplicationException {
        return this.getBaseWinningItemDao().findByGameInstanceAndSerialNoAndVersion(
                this.supportedGameType().getWinningItemType(), gameInstanceId, ticketSerialNo, version);
    }

    protected List<? extends BaseEntry> lookupTicketEntriess(String serialNo) throws ApplicationException {
        return this.getBaseEntryDao().findByTicketSerialNo(this.supportedGameType().getTicketEntryType(), serialNo,
                false);
    }

    protected List<? extends BaseTicket> lookupTickets(String serialNo) throws ApplicationException {
        return this.getBaseTicketDao().findBySerialNo(this.supportedGameType().getTicketType(), serialNo, false);
    }

    /**
     * Determine whether the ticket is a winning(normal or lucky draw) ticket. A ticket may win either normal draw or
     * lucky draw. For multiple-draw tickets, if ticket wins any draw, it must be regarded as winning ticket.
     * 
     * @param clientTicket
     *            The ticket represents client request.
     * @param hostTickets
     *            THe tickets of server side representation.
     */
    protected void determineWinningStatus(BaseTicket clientTicket, List<? extends BaseTicket> hostTickets)
            throws ApplicationException {
        for (BaseTicket hostTicket : hostTickets) {
            if (hostTicket.isWinning()) {
                clientTicket.setWinning(true);
            }
            if (hostTicket.isLuckyWinning()) {
                clientTicket.setLuckyWinning(true);
            }
        }

        if (!clientTicket.isWinning() && !clientTicket.isLuckyWinning()) {
            throw new ApplicationException(SystemException.CODE_NOTWINNINGTICKET, "Ticket(serialNo="
                    + clientTicket.getSerialNo() + ") isn't a winning ticket.");
        }
    }

    protected BaseOperationParameter lookupOperationParameter(Game game) throws ApplicationException {
        return this.getBaseJpaDao().findById(this.supportedGameType().getOperationParametersType(),
                game.getOperatorParameterId());
    }

    // -----------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------------------

    public GameInstanceService getGameInstanceService() {
        return gameInstanceService;
    }

    public void setGameInstanceService(GameInstanceService gameInstanceService) {
        this.gameInstanceService = gameInstanceService;
    }

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

    public MerchantService getMerchantService() {
        return merchantService;
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

    public BaseWinningItemDao getBaseWinningItemDao() {
        return baseWinningItemDao;
    }

    public void setBaseWinningItemDao(BaseWinningItemDao baseWinningItemDao) {
        this.baseWinningItemDao = baseWinningItemDao;
    }

    public TaxService getTaxService() {
        return taxService;
    }

    public void setTaxService(TaxService taxService) {
        this.taxService = taxService;
    }

    public PayoutStrategyFactory getPayoutStrategyFactory() {
        return payoutStrategyFactory;
    }

    public void setPayoutStrategyFactory(PayoutStrategyFactory payoutStrategyFactory) {
        this.payoutStrategyFactory = payoutStrategyFactory;
    }

    public BaseWinningStatisticsDao getBaseWinningStatisticsDao() {
        return baseWinningStatisticsDao;
    }

    public void setBaseWinningStatisticsDao(BaseWinningStatisticsDao baseWinningStatisticsDao) {
        this.baseWinningStatisticsDao = baseWinningStatisticsDao;
    }

    public PrizeLevelDao getPrizeLevelDao() {
        return prizeLevelDao;
    }

    public void setPrizeLevelDao(PrizeLevelDao prizeLevelDao) {
        this.prizeLevelDao = prizeLevelDao;
    }

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

    public BalanceTransactionsDao getBalanceTransactionsDao() {
        return balanceTransactionsDao;
    }

    public void setBalanceTransactionsDao(BalanceTransactionsDao balanceTransactionsDao) {
        this.balanceTransactionsDao = balanceTransactionsDao;
    }

}
