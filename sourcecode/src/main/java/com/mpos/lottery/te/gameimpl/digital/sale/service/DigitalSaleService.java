package com.mpos.lottery.te.gameimpl.digital.sale.service;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.digital.sale.DigitalEntry;
import com.mpos.lottery.te.gameimpl.digital.sale.dao.DigitalStatOfSelectedNumberDao;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.StatOfSelectedNumber;
import com.mpos.lottery.te.gamespec.sale.service.AbstractTicketService;
import com.mpos.lottery.te.gamespec.sale.support.validator.SelectedNumber;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DigitalSaleService extends AbstractTicketService {
    private Log logger = LogFactory.getLog(DigitalSaleService.class);
    private DigitalStatOfSelectedNumberDao statOfSelectedNumberDao;

    @Override
    public GameType supportedGameType() {
        return GameType.DIGITAL;
    }

    @Override
    protected void generateQPWhenSale(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        for (BaseEntry entry : clientTicket.getEntries()) {
            if (entry.isQP() && ((DigitalEntry) entry).isXD()) {
                // generate QP for XD option only
                int countOfQPNumber = this.determineCountOfQPNumber(entry);
                if (countOfQPNumber > 0) {
                    StopWatch sw = new Log4JStopWatch();
                    entry.setSelectNumber(doGeneratingQP(respCtx, clientTicket.getGameInstance(), countOfQPNumber));
                    sw.stop("Generate_QP", "Generate QP number for ticket(" + clientTicket.getSerialNo() + ")");
                }
            }
        }
    }

    /**
     * Generate QP numbers for digital game.
     * <p/>
     * <h1>Prepare Stat of Selected Numbers</h1>
     * <p/>
     * When create digital game instances, M.Lottery will insert statOfSelectedNumbers records in advance. For example a
     * 3D game, there should be total 30 records, just as below,
     * <table border="1">
     * <tr>
     * <td>gameInstance</td>
     * <td>selecedNumber</td>
     * <td>count</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>0</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>...</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>9</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>00</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>...</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>90</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>000</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>...</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>900</td>
     * <td>0</td>
     * </tr>
     * </table>
     * <ul>
     * <li>The selected number 0..9 represents the 1st number of a selected number</li>
     * <li>The selected number 00..90 represents the 2nd number of a selected number</li>
     * <li>the selected number 000..900 represented the 3rd number of a selected number
     * <li>
     * </ul>
     * For example a selected number '1,9,7', the 1st number is 7, 2nd number is 9 and the 3rd number is 1.
     * <p/>
     * <h1>Update Stat of Selected Numbers</h1>
     * When make sale of digital game, the selected number of each sale will be parsed and update the stat. For example
     * a sale with below entries:
     * <ul>
     * <li>2,8,7</li>
     * <li>3,8,6</li>
     * <li>3,8,7
     * <li>
     * </ul>
     * and the stat will be updated as
     * <table>
     * <tr>
     * <td>gameInstance</td>
     * <td>selecedNumber</td>
     * <td>count</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>0</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>...</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>7</td>
     * <td>2</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>8</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>9</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>00</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>...</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>80</td>
     * <td>3</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>90</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>000</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>...</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>200</td>
     * <td>1</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>300</td>
     * <td>2</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>...</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>Draw-1</td>
     * <td>900</td>
     * <td>0</td>
     * </tr>
     * </table>
     * <p/>
     * <h1>Generate QP Numbers</h1>
     * <p/>
     * This service will generate QP number based on stat of selected number. If client requests a QP numbers of 3D,
     * this service will parse the stat of selected numbers of given game instance. If the game instance is 'Draw-1',
     * then
     * <ol>
     * <li>Retrieve the list of selected numbers of 3rd position(0..9), and sort them by count. Construct a new array
     * based on, for example the last 8 selected numbers(8 is a configurable value), then pick one number randomly from
     * this array.</li>
     * <li>Determine the 2nd number following the same algorithm as the 1st step</li>
     * <li>Determine the 3rd number following the same algorithm as the 1st step</li>
     * </ol>
     * Finally you get a QP numbers.
     */
    @Override
    protected String doGeneratingQP(Context<?> respCtx, BaseGameInstance gameInstance, int countOfQPNumber)
            throws ApplicationException {
        // TODO no need to enquiry the list of StatOfSelectedNumbers for each QP
        // entry.
        List<StatOfSelectedNumber> stats = this.getStatOfSelectedNumberDao().findByGameInstance(gameInstance.getId());
        // organize into map, key is the position
        Map<Integer, List<StatOfSelectedNumber>> statMap = new HashMap<Integer, List<StatOfSelectedNumber>>();
        for (StatOfSelectedNumber stat : stats) {
            Integer key = stat.getSelecteNumber().length();
            List<StatOfSelectedNumber> positionStat = statMap.get(key);
            if (positionStat == null) {
                positionStat = new ArrayList<StatOfSelectedNumber>();
                statMap.put(key, positionStat);
            }
            positionStat.add(stat);
        }

        StringBuffer qp = new StringBuffer();
        for (int i = countOfQPNumber; i > 0; i--) {
            int number = this.determineNumber(statMap.get(i), i);
            if (i != countOfQPNumber) {
                qp.append(SelectedNumber.DELEMETER_NUMBER);
            }
            qp.append(number + "");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Generate QP number:" + qp.toString() + " of game instance(id=" + gameInstance.getId() + ").");
        }
        return qp.toString();
    }

    /**
     * Determine the number of given index. For example if QP a 3D selected number, and wanna determine the number of
     * 2nd position, the procedure will be follow:
     * <ol>
     * <li>Lookup all StatOfSelectedNumber range from '00' to '90'(if X/Y is 0/9)</li>
     * <li>Sort the list of StatOfSelectedNumbers, and find the 5 less used numbers</li>
     * <li>Pick one number randomly from those 5 samples</li>
     * </ol>
     * 
     * If X/Y is 0/9, there should be 10 StatOfSelectedNumber records for each position(the 1st position is '0' to '9',
     * the 2nd position is '00' to '90', and so on). Then for a 3D selected number, for example '1,9,7', the 1st
     * position is 7, the 2nd position is 9 and the 3rd position is 1.
     * 
     * @param positionStats
     *            The list of all <code>StatOfSelectedNumber</code>s of a given game instance.
     * @param position
     *            THe index at which position the number will be generated.
     * @return a number to given position.
     */
    private int determineNumber(List<StatOfSelectedNumber> positionStats, int position) throws ApplicationException {
        if (positionStats == null || positionStats.size() == 0) {
            throw new SystemException("No any StatOfSelectedNumber found of position:" + position);
        }
        // sort list in the order of StatOfSelectedNumber.count
        Collections.sort(positionStats, new Comparator<StatOfSelectedNumber>() {

            @Override
            public int compare(StatOfSelectedNumber o1, StatOfSelectedNumber o2) {
                return o1.getCount() - o2.getCount();
            }
        });
        int qpSampleRate = MLotteryContext.getInstance().getInt("digital.qp.rate", 90);
        int countOfSample = (positionStats.size() * qpSampleRate / 100 == 0)
                ? 1
                : (positionStats.size() * qpSampleRate / 100);
        List<StatOfSelectedNumber> sampleStats = new ArrayList<StatOfSelectedNumber>();
        for (int i = 0; i < countOfSample; i++) {
            sampleStats.add(positionStats.get(i));
        }
        Collections.shuffle(sampleStats);

        int number = Integer.parseInt(sampleStats.get(0).getSelecteNumber().substring(0, 1));
        if (logger.isDebugEnabled()) {
            logger.debug(qpSampleRate + "% of total " + positionStats.size() + " of position[" + position
                    + "] will be put in QP sample, " + number + " is picked.");
        }
        return number;
    }

    private int determineCountOfQPNumber(BaseEntry entry) throws ApplicationException {
        if (entry.getSelectNumber() != null && !"".equalsIgnoreCase(entry.getSelectNumber())) {
            // no need to generate QP, as client has provided the numbers.
            return 0;
        } else {
            return entry.getBetOption();
        }
    }

    public DigitalStatOfSelectedNumberDao getStatOfSelectedNumberDao() {
        return statOfSelectedNumberDao;
    }

    public void setStatOfSelectedNumberDao(DigitalStatOfSelectedNumberDao statOfSelectedNumberDao) {
        this.statOfSelectedNumberDao = statOfSelectedNumberDao;
    }

}
