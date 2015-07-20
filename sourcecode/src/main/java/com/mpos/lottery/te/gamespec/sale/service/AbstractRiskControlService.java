package com.mpos.lottery.te.gamespec.sale.service;

import com.google.gson.Gson;

import com.mpos.lottery.te.common.util.Combination;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.InstantaneousSale;
import com.mpos.lottery.te.gamespec.sale.RiskControlLog;
import com.mpos.lottery.te.gamespec.sale.dao.InstantaneousSaleDao;
import com.mpos.lottery.te.gamespec.sale.dao.RiskControlLogDao;
import com.mpos.lottery.te.gamespec.sale.support.ChanceOdds;
import com.mpos.lottery.te.gamespec.sale.support.ChanceOfEntry;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.TransactionMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SuppressWarnings("rawtypes")
public abstract class AbstractRiskControlService implements RiskControlService {
    private Log logger = LogFactory.getLog(AbstractRiskControlService.class);
    private static final String KEY_JSONTRANS_RISKLOG = "riskLogs";
    private final Object lock = new Object();
    // SPRING DEPENDENCIES
    private InstantaneousSaleDao instantaneousSaleDao;
    private RiskControlLogDao riskControlLogDao;
    private RiskControlLogServiceAsyn riskControlLogServiceAsyn;
    @PersistenceContext(unitName = "lottery_te")
    private EntityManager entityManager;

    @Override
    public void riskControl(Context respCtx, BaseTicket ticket, List<? extends BaseGameInstance> gameInstances)
            throws ApplicationException {
        /**
         * Record the transaction message for later possible cancellation, its format will be
         * {"data":[{"Id_riskControlLog#1":bet_amount},{"Id_riskControlLog#2" :bet_amount }...{}]}
         * <p/>
         * Refer to {@link JsonRiskControlTrans}
         */
        JsonRiskControlTrans jsonTrans = new JsonRiskControlTrans();

        for (BaseGameInstance gameInstance : gameInstances) {
            StopWatch sw = new Log4JStopWatch();

            for (BaseEntry entry : ticket.getEntries()) {
                // determine the final limit
                BigDecimal finalLimit = this.determineFinalLimit(entry, ticket, gameInstance);
                this.doRiskConrolOnEntry(respCtx, ticket, gameInstance, entry, finalLimit, jsonTrans);
            }
            sw.stop("Risk_Control",
                    "Risk control of ticket(" + ticket.getSerialNo() + ") of game type("
                            + GameType.fromType(gameInstance.getGame().getType()) + ")");
        }

        TransactionMessage transMsg = new TransactionMessage();
        transMsg.setTransactionId(respCtx.getTransaction().getId());
        respCtx.getTransaction().setTransMessage(transMsg);
        // add response JSON entry
        transMsg.addRespJsonEntry(KEY_JSONTRANS_RISKLOG, jsonTrans.getRiskLogs());
    }

    @Override
    public void cancelRiskControl(Context respCtx, List<? extends BaseTicket> hostTickets) throws ApplicationException {
        // lookup the cancelled sale transaction
        TransactionMessage saleTransaction = this.getEntityManager().find(TransactionMessage.class,
                respCtx.getTransaction().getCancelTransactionId());
        JsonRiskControlTrans riskTrans = new Gson().fromJson(saleTransaction.getResponseMsg(),
                JsonRiskControlTrans.class);
        // cancel those logs one by one
        for (JsonRiskControlTransItem logItem : riskTrans.getRiskLogs()) {
            this.getRiskControlLogDao().updateWithAmount(logItem.getId(),
                    logItem.getAmount().multiply(new BigDecimal("-1")));
        }
    }

    /**
     * Apply risk control on given entry. 
     * 
     * @param respCtx
     *            The context of current sale transaction.
     * @param ticket
     *            The ticket which contains given entry.
     * @param gameInstance
     *            In which the turnover will be verified...consider a multi-draw ticket.
     * @param entry
     *            Apply risk control method on this entry.
     * @param finalLimit
     *            The final amount of loss.
     * @param jsonTrans
     *            The transaction log which will be used when cancellation.
     */
    protected final void doRiskConrolOnEntry(Context respCtx, BaseTicket ticket, BaseGameInstance gameInstance,
            BaseEntry entry, BigDecimal finalLimit, JsonRiskControlTrans jsonTrans) throws ApplicationException {
        List<ChanceOdds> chanceOdds = this.determineOddsOfEntry(respCtx, ticket, gameInstance, entry);
        if (chanceOdds == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("There is no any possible prize levels found for entry(" + entry + ")");
            }
            return;
        }

        List<ChanceOfEntry> chances = this.determineSelectedNumbers(respCtx, ticket, gameInstance, entry);
        BigDecimal finalLossAmountOfPrizeLevel = determineFinalLossAmountOfPrizeLevel(finalLimit, chanceOdds,
                gameInstance);

        for (ChanceOfEntry chance : chances) {
            for (ChanceOdds chanceOdd : chanceOdds) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Prepare to generate risk control log of " + chanceOdd);
                }
                // determine betting number of each possible prize levels
                String bettingNumber = this.determineBettingNumberOfPrizeLevel(respCtx, entry, chanceOdd, chance);
                if (logger.isDebugEnabled()) {
                    logger.debug("Determine the odds of betting number(" + bettingNumber + ", amount="
                            + chance.getAmount() + ") is " + chanceOdd.getOdds());
                }
                // lookup RiskControlLog first
                RiskControlLog riskLog = null;
                /**
                 * There are 3 questions:
                 * <ol>
                 * <li>Why needs a synchronized block here?</li>
                 * <li>Why needs a <code>RiskControlLogServiceAsyn</code>?</li>
                 * <li>Why the <code>RiskControlLogServiceAsyn</code> create a default risk control log whose total
                 * amount is 0?</li>
                 * </ol>
                 * <li>Why needs a synchronized block here?</li>
                 * <p/>
                 * Simple answer is to avoid generating duplicated records of same game instance and selected number in
                 * multi-threads environment. THe synchronized block guarantee that only one thread can create the new
                 * risk control log (actually per TE instance).
                 * <p/>
                 * Now why need a <code>RiskControlLogServiceAsyn</code>?
                 * <p/>
                 * Lets imagine that there are two threads reach the entry point of this synchronized block, one thread
                 * go through the block, and it creates a risk control log, but as its transaction doesn't commit, even
                 * the other thread troop in the synchronized block, it won't see the record created by 1st thread, so
                 * it will create a duplicated risk control log.
                 * <p/>
                 * To avoid this, the logic of create a risk control log must be wrapped in a isolated transaction. In
                 * TE transaction definition, any service whose name ends with 'ServiceAsyn' will require a new
                 * transaction.
                 * <p/>
                 * Now it is time to answer why to create a default risk control log?
                 * <p/>
                 * The answer is connected to the 2nd question. As the creation of risk control log is happened in a new
                 * transaction, so if the outer transaction of the synchronized block rolled back, how to roll back the
                 * generated risk control log?? So now I will simply create a default risk control log, and then update
                 * it in the outer transaction.
                 */
                synchronized (lock) {
                    riskLog = this.getRiskControlLogDao().findByGameInstanceAndSelectedNumber(gameInstance.getId(),
                            bettingNumber, chanceOdd.getPrizeLevelTypeInt());
                    if (riskLog == null) {
                        riskLog = this.getRiskControlLogServiceAsyn().createDefault(gameInstance.getId(),
                                bettingNumber, chanceOdd.getPrizeLevelTypeInt());
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("found risk control log " + riskLog);
                        }
                    }
                }
                BigDecimal riskPrizeAmount = this.determineRiskPrizeAmount(respCtx, riskLog, ticket, gameInstance,
                        entry, chance, chanceOdd);
                if (riskPrizeAmount.compareTo(finalLossAmountOfPrizeLevel) > 0) {
                    throw new ApplicationException(SystemException.CODE_OUT_OF_RISK_CONTROL, "The riskPrizeAmount("
                            + riskPrizeAmount + ") of bettng number(" + bettingNumber + ") of entry(" + entry
                            + ") exceeds the risk limit(" + finalLossAmountOfPrizeLevel + ") of prizelelve type:"
                            + chanceOdd.getPrizelLevelType() + ".");
                } else {
                    // update risk control log
                    riskLog.setTotalAmount(riskLog.getTotalAmount());
                    this.riskControlLogDao.updateWithAmount(riskLog.getId(), chance.getAmount());

                    JsonRiskControlTransItem jsonTransItem = new JsonRiskControlTransItem();
                    jsonTransItem.setId(riskLog.getId());
                    jsonTransItem.setAmount(chance.getAmount());
                    jsonTrans.merge(jsonTransItem);
                }
            }
        }

    }

    /**
     * Determine the final loss amount of a prize level. In general it should be
     * <code>(finalLimit/countOfPrizeLevelOfGameInstance)</code>
     * 
     * @param finalLimit
     *            The allowed loss prize amount of a game instance.
     * @param chanceOdds
     *            THose chance odds of a given entry.
     * @param gameInstance
     *            the game instance of current sale.
     * @return The final loss amount of a prize level
     */
    protected abstract BigDecimal determineFinalLossAmountOfPrizeLevel(BigDecimal finalLimit,
            List<ChanceOdds> chanceOdds, BaseGameInstance gameInstance) throws ApplicationException;

    /**
     * Determine the betting number of a specific prize level. For example a '4D' entry '2,0,1,4' may win 'First4D',
     * 'first3D' and 'last3d', then the betting number of each prize level types will be,
     * <ul>
     * <li>first4d - 2,0,1,4</li>
     * <li>first3d - 2,0,1</li>
     * <li>last3d - 0,1,4</li>
     * </ul>
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param entry
     *            the ticket entry.
     * @param chanceOdd
     *            The odds and prize level type of a given chance.
     * @param chance
     *            The chance which may win a prize.
     * @return the betting number of a specific prize level
     * @throws ApplicationException
     *             if encounter any biz exception.
     */
    protected abstract String determineBettingNumberOfPrizeLevel(Context respCtx, BaseEntry entry,
            ChanceOdds chanceOdd, ChanceOfEntry chance) throws ApplicationException;

    /**
     * Calculate the risk prize amount.
     */
    protected BigDecimal determineRiskPrizeAmount(Context respCtx, RiskControlLog riskLog, BaseTicket ticket,
            BaseGameInstance gameInstance, BaseEntry entry, ChanceOfEntry chance, ChanceOdds chanceOdds) {
        return SimpleToolkit.mathMultiple(chance.getAmount().add(riskLog.getTotalAmount()), chanceOdds.getOdds());
    }

    /**
     * Determine the odds of given entry. Even a multiple entry, all single selected number share the same odds.The risk
     * prize amount should be the max prize amount a selected number possibly win.
     * 
     * @param respCtx
     *            The context of sale transaction.
     * @param ticket
     *            The sold ticket.
     * @param gameInstance
     *            In which the turnover will be verified. The ticket.getGameInstance() will always return the game
     *            instance which is the one in which the ticket is sold.
     * @param entry
     *            The entry from which the single selected numbers derived.
     * @return All possible prize levels a entry may win
     * @throws ApplicationException
     *             if fail to get risk prize amount.
     */
    protected abstract List<ChanceOdds> determineOddsOfEntry(Context respCtx, BaseTicket ticket,
            BaseGameInstance gameInstance, BaseEntry entry) throws ApplicationException;

    /**
     * Determine how many chances derived from given entry, for example a 'multiple' selected number can generate many
     * 'single' selected number, and each 'single' selected number is a chance.
     * <p/>
     * The subclass can override this.
     */
    protected List<ChanceOfEntry> determineSelectedNumbers(Context respCtx, BaseTicket ticket,
            BaseGameInstance gameInstance, BaseEntry entry) {
        return Arrays.asList(new ChanceOfEntry(entry.getSelectNumber(), entry.getEntryAmount(), entry.getBetOption()));
    }

    /**
     * Determine the final limit. The final limit is up to the risk control method. For 'max loss method', the final
     * limit will be <code>gameInstance.maxLossAmount</code>. If 'dynamic method', the final limit will be
     * <code>max(gameInstance.maxLossAmount, turnover*gameInstance.percentageOfTurnover)</code>
     * 
     * @param entry
     *            The entry which will apply risk control checking on.
     * @param ticket
     *            The sold ticket.
     * @param gameInstance
     *            the game instance in which the turnover will be counted...think about multi-draw ticket.
     * @return the final limit of risk control.
     */
    protected BigDecimal determineFinalLimit(BaseEntry entry, BaseTicket ticket, BaseGameInstance gameInstance) {
        int riskControlMethod = gameInstance.getRiskControlMethod();
        BigDecimal finalLimit = new BigDecimal("0");
        if (BaseGameInstance.RISKCONTROL_METHOD_MAX_LOSS == riskControlMethod) {
            finalLimit = gameInstance.getMaxLossAmount();
        } else if (BaseGameInstance.RISKCONTROL_METHOD_DYNAMIC == riskControlMethod) {
            finalLimit = gameInstance.getMaxLossAmount();
            InstantaneousSale insSale = this.getInstantaneousSaleDao().findByGameDraw(gameInstance.getId());
            if (insSale != null) {
                BigDecimal percentageTurnover = SimpleToolkit.mathMultiple(
                        insSale.getTurnover().add(
                                SimpleToolkit.mathDivide(ticket.getTotalAmount(),
                                        new BigDecimal(ticket.getMultipleDraws()))),
                        gameInstance.getPercentageOfTurnover());
                if (percentageTurnover.compareTo(finalLimit) > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("The final limit of dynamic rick control method(maxLoss:"
                                + gameInstance.getMaxLossAmount() + ",turnPercentage:" + percentageTurnover
                                + ") of ticket(" + ticket.getSerialNo() + ")");
                    }
                    finalLimit = percentageTurnover;
                }
            }
        } else {
            throw new SystemException("Unsupported risk control method:" + riskControlMethod);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Determine the final risk control limit of ticket(" + ticket.getSerialNo() + ") as "
                    + finalLimit);
        }
        return finalLimit;
    }

    /**
     * Assemble single selected number based on a entry.
     * 
     * @param entry
     *            The ticket entries.
     * @param kkk
     *            The count of numbers of a single selected-number.
     * @param betOption
     *            The bet option of chance.
     * @return All possible single selected numbers.
     */
    protected List<ChanceOfEntry> assembleSingleSelectedNumbers(BaseEntry entry, int kkk, int betOption) {
        int[] numbers = entry.getParsedSelectedNumber().getBaseNumbers();
        // sort numbers to make sure we always get same selected number even
        // player given in a different order.
        Arrays.sort(numbers);
        Combination c = new Combination(numbers.length, kkk);
        BigDecimal singleEntryAmount = SimpleToolkit.mathDivide(entry.getEntryAmount(), new BigDecimal(c.getTotal()
                .intValue()));
        List<ChanceOfEntry> singleEntries = new ArrayList<ChanceOfEntry>();
        int totalEles = c.getTotal().intValue();
        for (int i = 0; i < totalEles; i++) {
            int[] ele = c.getNext();
            StringBuffer selectedNumber = new StringBuffer();
            for (int j = 0; j < ele.length; j++) {
                if (j != 0) {
                    selectedNumber.append(entry.getParsedSelectedNumber().getNumberDelemeter());
                }
                selectedNumber.append(numbers[ele[j]]);
            }
            singleEntries.add(new ChanceOfEntry(selectedNumber.toString(), singleEntryAmount, betOption));
        }

        return singleEntries;
    }

    public InstantaneousSaleDao getInstantaneousSaleDao() {
        return instantaneousSaleDao;
    }

    public void setInstantaneousSaleDao(InstantaneousSaleDao instantaneousSaleDao) {
        this.instantaneousSaleDao = instantaneousSaleDao;
    }

    public RiskControlLogDao getRiskControlLogDao() {
        return riskControlLogDao;
    }

    public void setRiskControlLogDao(RiskControlLogDao riskControlLogDao) {
        this.riskControlLogDao = riskControlLogDao;
    }

    public RiskControlLogServiceAsyn getRiskControlLogServiceAsyn() {
        return riskControlLogServiceAsyn;
    }

    public void setRiskControlLogServiceAsyn(RiskControlLogServiceAsyn riskControlLogServiceAsyn) {
        this.riskControlLogServiceAsyn = riskControlLogServiceAsyn;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected class JsonRiskControlTrans {
        private List<JsonRiskControlTransItem> riskLogs = new LinkedList<JsonRiskControlTransItem>();

        /**
         * As a ticket may contain multiple entries, those multiple entries may maintain the same {@link RiskControlLog}
         * entities, this method will merge those same {@link RiskControlLog} into a single item to avoid repeatedly
         * access same entity when do cancellation.
         */
        public void merge(JsonRiskControlTransItem item) {
            boolean itemExisted = false;
            for (JsonRiskControlTransItem existedItem : riskLogs) {
                if (existedItem.getId().equalsIgnoreCase(item.getId())) {
                    existedItem.setAmount(existedItem.getAmount().add(item.getAmount()));
                    itemExisted = true;
                }
            }
            if (!itemExisted) {
                riskLogs.add(item);
            }
        }

        public List<JsonRiskControlTransItem> getRiskLogs() {
            return riskLogs;
        }
    }

    protected class JsonRiskControlTransItem {
        // id of risk control log
        private String id;
        // amount of the risk control log of current sale transaction.
        private BigDecimal amount;

        public String getId() {
            return id;
        }

        public void setId(String idOfRiskControlLog) {
            this.id = idOfRiskControlLog;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal betAmount) {
            this.amount = betAmount;
        }
    }
}
