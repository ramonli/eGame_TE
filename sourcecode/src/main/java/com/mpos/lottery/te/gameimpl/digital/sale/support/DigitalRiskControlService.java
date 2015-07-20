package com.mpos.lottery.te.gameimpl.digital.sale.support;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalFunType;
import com.mpos.lottery.te.gameimpl.digital.game.DigitalGameInstance;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalEntry;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.AbstractRiskControlService;
import com.mpos.lottery.te.gamespec.sale.support.ChanceOdds;
import com.mpos.lottery.te.gamespec.sale.support.ChanceOfEntry;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumber;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class DigitalRiskControlService extends AbstractRiskControlService {
    private static final String KEY_ALGORIGHM_TYPE = "DIGITAL.ALGORITHM_TYPE";
    private static final int TYPE_FIRST = 1;
    private static final int TYPE_LAST = 2;
    private static final int TYPE_MIXTURE = 3;

    private Log logger = LogFactory.getLog(DigitalRiskControlService.class);
    /**
     * For typeA, digital game will support multiple winner analysis, that says a ticket may join multiple winner
     * analysis. For this type, K equals with N, so player can only buy, for example 4D, and can win only 'Frist4D' and
     * 'First Mixture'
     */
    public static final String ALGORITHM_TYPE_A = "7";
    /**
     * For typeB, K equals with N, so player can only buy. for example 4D, ticket, and this ticket may win(here we
     * assume K=4, N=4),
     * <ul>
     * <li>First 4D</li>
     * <li>First 3D</li>
     * <li>Last 3D</li>
     * <li>First 2D</li>
     * <li>Last 2D</li>
     * <li>First 1D</li>
     * <li>Last 1D</li>
     * </ul>
     */
    public static final String ALGORITHM_TYPE_B = "20";
    /**
     * For typeC, K doesn't equals with N, so player can buy, for example 4D, 3D, 2D, 1D, ODD/EVEN and SUM. For a given
     * bet option, the prize levels it can win are as below(here we assume K=2, N=4),
     * <table border="1">
     * <tr>
     * <td>bet option</td>
     * <td>prize level</td>
     * </tr>
     * <tr>
     * <td rowspan="2">4D</td>
     * <td>First 4D</td>
     * </tr>
     * <tr>
     * <td>Mixture 4D</td>
     * </tr>
     * <tr>
     * <td rowspan="3">3D</td>
     * <td>First 3D</td>
     * </tr>
     * <tr>
     * <td>Last 3D</td>
     * </tr>
     * <tr>
     * <td>Mixture 3D</td>
     * </tr>
     * <tr>
     * <td rowspan="3">2D</td>
     * <td>First 2D</td>
     * </tr>
     * <tr>
     * <td>Last 2D</td>
     * </tr>
     * <tr>
     * <td>Mixture 2D</td>
     * </tr>
     * <tr>
     * <td>ODD</td>
     * <td>ODD</td>
     * </tr>
     * <tr>
     * <td>EVEN</td>
     * <td>EVEN</td>
     * </tr>
     * <tr>
     * <td>SUM</td>
     * <td>SUM</td>
     * </tr>
     * </table>
     */
    public static final String ALGORITHM_TYPE_C = "21";
    /**
     * Supports only XD, for example if K/N(N=6), then only 6D allowed.
     */
    public static final String ALGORITHM_TYPE_E = "26";

    @PersistenceContext(unitName = "lottery_te")
    private EntityManager entityManager;

    @Override
    protected List<ChanceOdds> determineOddsOfEntry(Context respCtx, BaseTicket ticket, BaseGameInstance gameInstance,
            BaseEntry entry) throws ApplicationException {
        String prizeLogicId = ((DigitalGameInstance) gameInstance).getPrizeLogicId();

        String algorithmType = this.determinePrizeAlgorithmType(prizeLogicId);
        // cache the 'algorithmType' for later use
        respCtx.setProperty(KEY_ALGORIGHM_TYPE, algorithmType);
        List<ChanceOdds> chanceOdds = null;
        if (ALGORITHM_TYPE_A.equals(algorithmType) || ALGORITHM_TYPE_B.endsWith(algorithmType)) {
            chanceOdds = this.determineOdds(prizeLogicId, entry, false);
        } else if (ALGORITHM_TYPE_C.equals(algorithmType)) {
            if (DigitalEntry.DIGITAL_BETOPTION_SUM != entry.getBetOption()) {
                // lookup odd of XD and odd/even bet option
                chanceOdds = this.determineOdds(prizeLogicId, entry, true);
            } else {
                // lookup odd if SUM bet option.
                chanceOdds = this.determineOddsOfSum(prizeLogicId, gameInstance.getGame().getFunTypeId(), entry);
            }
        } else if (ALGORITHM_TYPE_E.equals(algorithmType)) {
            chanceOdds = this.determineOddsTypeE(prizeLogicId, entry);
        } else {
            throw new SystemException("Unsupportted prize algorithm type:" + algorithmType);
        }
        if (chanceOdds == null) {
            throw new SystemException("No valid odds found for entry(" + entry + ")");
        }
        return chanceOdds;
    }

    @Override
    protected BigDecimal determineFinalLossAmountOfPrizeLevel(BigDecimal finalLimit, List<ChanceOdds> chanceOdds,
            BaseGameInstance gameInstance) {
        BigDecimal result = SimpleToolkit.mathDivide(finalLimit, new BigDecimal(chanceOdds.size() + ""));
        if (logger.isDebugEnabled()) {
            logger.debug("There total " + chanceOdds.size()
                    + " possible prize levels found, determine the final loss amount of prize level as " + result + "="
                    + finalLimit + "/" + chanceOdds.size());
        }
        return result;
    }

    @Override
    protected String determineBettingNumberOfPrizeLevel(Context respCtx, BaseEntry entry, ChanceOdds chanceOdd,
            ChanceOfEntry chance) throws ApplicationException {
        DigitalEntry digitEntry = (DigitalEntry) entry;
        if (digitEntry.isXD()) {
            String algorithmType = (String) respCtx.getProperty(KEY_ALGORIGHM_TYPE);
            if (ALGORITHM_TYPE_E.equalsIgnoreCase(algorithmType)) {
                // for type E, the prize level type is a single digital number.
                chanceOdd.setBettingNumber(entry.getSelectNumber());
            } else {
                /**
                 * refer to {@link com.mpos.lottery.te.gamespec.sale.support.ChanceOdds#prizeLevelType}. This service
                 * will generate a risk control log for each possible winning prize level, for example 'first4D', 'last
                 * 3D' etc.
                 */
                // it should be a 2 digital number
                String prizeLevelType = chanceOdd.getPrizelLevelType();
                int countOfNumbers = Integer.parseInt(prizeLevelType.substring(0, 1));
                int typeOfNumbers = Integer.parseInt(prizeLevelType.substring(1));

                int[] numbers = SimpleToolkit.string2IntArray(entry.getSelectNumber(), SelectedNumber.DELEMETER_NUMBER,
                        false);
                if (TYPE_FIRST == typeOfNumbers) {
                    if (ALGORITHM_TYPE_A.equals(algorithmType)) {
                        chanceOdd.setBettingNumber(entry.getSelectNumber());
                    } else {
                        chanceOdd.setBettingNumber(SimpleToolkit.join(numbers, 0, countOfNumbers,
                                SelectedNumber.DELEMETER_NUMBER));
                    }
                } else if (TYPE_LAST == typeOfNumbers) {
                    if (ALGORITHM_TYPE_A.equals(algorithmType)) {
                        chanceOdd.setBettingNumber(entry.getSelectNumber());
                    } else {
                        chanceOdd.setBettingNumber(SimpleToolkit.join(numbers, numbers.length - countOfNumbers,
                                countOfNumbers, SelectedNumber.DELEMETER_NUMBER));
                    }
                } else if (TYPE_MIXTURE == typeOfNumbers) {
                    /**
                     * if mixture, that means if winning number is '2,6,4,3', then any combination of '2,6,4,3' will be
                     * winning, we must trace this by sorting them.
                     * <p/>
                     * For example, no matter '2,6,4,3', '2,3,6,4' or '6,4,3,2', we will trace it by
                     * <code>RiskControlLog</code>:
                     * <ul>
                     * <li>selecteNumber - '2,3,4,6' (sort the numbers)</li>
                     * <li>prizeLevelType - 33(Mixture 3D for example)</li>
                     * </ul>
                     */
                    Arrays.sort(numbers);
                    chanceOdd.setBettingNumber(SimpleToolkit.join(numbers, 0, numbers.length,
                            SelectedNumber.DELEMETER_NUMBER));
                } else {
                    throw new SystemException("Unsupported prize level type:" + typeOfNumbers);
                }
            }
        } else {
            chanceOdd.setBettingNumber(entry.getSelectNumber());
        }
        return chanceOdd.getBettingNumber();
    }

    private String determinePrizeAlgorithmType(String prizeLogicId) {
        String sql = "select p.algorithm_id from prize_logic p where p.PRIZE_LOGIC_ID=:prizeLogicId";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter("prizeLogicId", prizeLogicId);
        return (String) query.getSingleResult();
    }

    /**
     * Determine the odds of a entry if its bet option is XD, ODD or EVEN.
     */
    private List<ChanceOdds> determineOdds(String prizeLogicId, BaseEntry entry, boolean withBetOption) {
        String sql = "SELECT parameter_name,parameter_value FROM (SELECT pp.prize_logic_id, pp.parameter_name, "
                + "pp.parameter_value, DECODE(pp.parameter_name, -1, -1, -2, -2, SUBSTR(pp.parameter_name, 0, "
                + "LENGTH(pp.parameter_name) - 1)) te_betoption, pp.bet_option FROM prize_parameters pp where "
                + "pp.IS_ENABLE=1) pp_new WHERE ";
        if (withBetOption) {
            sql += "te_betoption=:teBetOption AND ";
        }
        sql += "prize_logic_id=:prizeLogicId ORDER BY bet_option";
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("prizeLogicId", prizeLogicId);
        if (withBetOption) {
            param.put("teBetOption", entry.getBetOption());
        }

        return this.assembleOdds(entry, sql, param);
    }

    private List<ChanceOdds> determineOddsTypeE(String prizeLogicId, BaseEntry entry) {
        String sql = "SELECT parameter_name,parameter_value FROM (SELECT pp.prize_logic_id, pp.parameter_name, "
                + "pp.parameter_value, DECODE(pp.parameter_name, -1, -1, -2, -2, pp.parameter_name) te_betoption, "
                + "pp.bet_option FROM prize_parameters pp WHERE pp.prize_logic_id=:prizeLogicId AND pp.IS_ENABLE= 1) "
                + "pp_new";
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("prizeLogicId", prizeLogicId);
        return this.assembleOdds(entry, sql, param);
    }

    protected List<ChanceOdds> assembleOdds(BaseEntry entry, String sql, Map<String, Object> param) {
        Query query = this.getEntityManager().createNativeQuery(sql);
        Iterator<String> keyIte = param.keySet().iterator();
        while (keyIte.hasNext()) {
            String key = keyIte.next();
            query.setParameter(key, param.get(key));
        }

        List resultList = query.getResultList();

        List<ChanceOdds> chanceOdds = new LinkedList<ChanceOdds>();
        for (int i = 0; i < resultList.size(); i++) {
            ChanceOdds odds = new ChanceOdds();
            Object[] row = (Object[]) resultList.get(i);
            odds.setPrizelLevelType((String) row[0]);
            odds.setOdds(new BigDecimal((String) row[1]));
            chanceOdds.add(odds);
        }
        return chanceOdds;
    }

    protected List<ChanceOdds> determineOddsOfSum(String prizeLogicId, String funTypeId, BaseEntry entry) {
        // lookup the XY/KN
        DigitalFunType funType = this.getEntityManager().find(DigitalFunType.class, funTypeId);
        int actualSum = this.determineActualSum(funType.getN(), funType.getX(), funType.getY(),
                Integer.parseInt(entry.getSelectNumber()));

        String sql = "select a.odds from(SELECT pl.prize_logic_name prize_name, pp.parameter_name, "
                + "SUBSTR(pp.parameter_name, 1, DECODE(instr(pp.parameter_name, 'or', 1, 1), 0, LENGTH(pp.parameter_name), "
                + "instr(pp.parameter_name, 'or') - 2)) prize_level, pp.parameter_value odds, pp.bet_option, "
                + "-3 digit FROM prize_logic pl, prize_parameters pp WHERE pp.IS_ENABLE=1 and pp.prize_logic_id = pl.prize_logic_id AND "
                + "pl.prize_logic_id=(SELECT p.sum_prize_logic_id FROM prize_logic p WHERE p.prize_logic_id=:prizeLogicId)) "
                + "a where a.prize_level=:prizeLevel";
        Query query = this.getEntityManager().createNativeQuery(sql);
        query.setParameter("prizeLogicId", prizeLogicId);
        query.setParameter("prizeLevel", actualSum + "");
        List<ChanceOdds> chanceOdds = new LinkedList<ChanceOdds>();
        ChanceOdds odds = new ChanceOdds();
        odds.setBettingNumber(entry.getSelectNumber());
        // simply as the bet option, as it can win only a single SUM prize level
        odds.setPrizelLevelType(entry.getBetOption() + "");
        odds.setOdds(new BigDecimal((String) query.getSingleResult()));
        chanceOdds.add(odds);
        return chanceOdds;
    }

    /**
     * For SUM bet option, the prize level definition may like below(Lets say it is 3D, and each ball ranges from 0 to
     * 9, so the min sum is 0 and max sum is 27):
     * <table border="1">
     * <tr>
     * <th>Sum</th>
     * <th>Odds</th>
     * </tr>
     * <tr>
     * <td>0 or 36</td>
     * <td>100</td>
     * </tr>
     * <tr>
     * <td>1 or 35</td>
     * <td>90</td>
     * </tr>
     * <tr>
     * <td>2 or 34</td>
     * <td>80</td>
     * </tr>
     * <tr>
     * <td colspan="2">...</td>
     * </tr>
     * <tr>
     * <td>18</td>
     * <td>50</td>
     * </tr>
     * </table>
     * 
     * So this method has to convert the selected number into a value which is less than middle number. In this case,
     * the middle number is (0+36)/2=18, then if the selected number is 35, it will be converted into 1, and more.
     * <ul>
     * <li>1 -> 1</li>
     * <li>35 -> 1</li>
     * <li>2 -> 2</li>
     * <li>34 -> 2</li>
     * <li>...</li>
     * <li>18 -> 18</li>
     * </ul>
     * 
     * No choice, M.Lottery saves the prize definition in that way, TE has to make such cumbersome work to suit it.
     * 
     * @param numberOfBall
     *            How many balls are supported by this digital game.
     * @param minNumberOfBall
     *            The minimum number of a ball.
     * @param maxNumberOfBall
     *            The maximum number of a ball.
     * @param selectedSum
     *            The sum selected by player.
     * @return The actual sum converted from player chose sum.
     */
    protected int determineActualSum(int numberOfBall, int minNumberOfBall, int maxNumberOfBall, int selectedSum) {
        // determine the middle number
        int middleSum = numberOfBall * (minNumberOfBall + maxNumberOfBall) / 2;
        int actualSum = selectedSum;
        if (actualSum > middleSum) {
            actualSum = numberOfBall * (minNumberOfBall + maxNumberOfBall) - actualSum;
        }
        return actualSum;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
