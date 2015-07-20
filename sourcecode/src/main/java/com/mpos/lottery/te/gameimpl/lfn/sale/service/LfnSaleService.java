package com.mpos.lottery.te.gameimpl.lfn.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lfn.game.LfnFunType;
import com.mpos.lottery.te.gameimpl.lfn.sale.LfnEntry;
import com.mpos.lottery.te.gameimpl.lfn.sale.dao.LfnStatOfSelectedNumberDao;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LfnSaleService extends AbstractTicketService {
    private Log logger = LogFactory.getLog(LfnSaleService.class);
    private LfnStatOfSelectedNumberDao statOfSelectedNumberDao;

    @Override
    protected void customizeAssembleTicket(BaseTicket generatedTicket, BaseTicket clientTicket) {
        super.customizeAssembleTicket(generatedTicket, clientTicket);
    }

    @Override
    public GameType supportedGameType() {
        return GameType.LFN;
    }

    @Override
    protected void generateQPWhenSale(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        for (BaseEntry entry : clientTicket.getEntries()) {
            if (entry.isQP()) {
                // generate QP selected numbers.
                int countOfQPNumber = this.determineCountOfQPNumber((LfnEntry) entry);
                if (countOfQPNumber > 0) {
                    StopWatch sw = new Log4JStopWatch();
                    entry.setSelectNumber(doGeneratingQP(respCtx, clientTicket.getGameInstance(), countOfQPNumber));
                    sw.stop("Generate_QP", "Generate QP number for ticket(" + clientTicket.getSerialNo() + ")");
                }
            }
        }
    }

    @Override
    protected String doGeneratingQP(Context<?> respCtx, BaseGameInstance gameInstance, int countOfQPNumber)
            throws ApplicationException {
        LfnFunType funType = this.getBaseJpaDao().findById(LfnFunType.class, gameInstance.getGame().getFunTypeId());
        if (countOfQPNumber > funType.getN()) {
            throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY, "The required count of QP number("
                    + countOfQPNumber + ") has exceeded max allowed count:" + funType.getN());
        }
        // pick range of latest sold numbers, and then pick some to
        // form a QP number
        int countOfSample = countOfQPNumber + (funType.getN() - countOfQPNumber) / 3;
        if (logger.isDebugEnabled()) {
            logger.debug("Ready to pick " + countOfQPNumber + " from total " + countOfSample
                    + " same numbers to form QP");
        }
        List<StatOfSelectedNumber> stats = this.getStatOfSelectedNumberDao().findByGameInstance(gameInstance.getId(),
                countOfSample);
        if (stats.size() < countOfQPNumber) {
            throw new SystemException("No enough stat of selected number found:" + stats.size() + ", at least "
                    + countOfQPNumber);
        }
        // shuffle the sample, and pick from the head
        Collections.shuffle(stats);
        int[] numbers = new int[countOfQPNumber];
        for (int i = 0; i < countOfQPNumber; i++) {
            numbers[i] = Integer.parseInt(stats.get(i).getSelecteNumber().trim());
        }
        Arrays.sort(numbers);
        StringBuffer qp = new StringBuffer();
        for (int i = 0; i < countOfQPNumber; i++) {
            if (i != 0) {
                qp.append(SelectedNumber.DELEMETER_NUMBER);
            }
            qp.append(numbers[i]);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Generate QP number:" + qp.toString() + " of game instance(id=" + gameInstance.getId() + ").");
        }
        return qp.toString();
    }

    protected int determineCountOfQPNumber(LfnEntry entry) throws ApplicationException {
        if (entry.getSelectNumber() != null && !"".equalsIgnoreCase(entry.getSelectNumber())) {
            // no need to generate QP, as client has provided the numbers.
            return 0;
        } else {
            if (!entry.isP()) {
                return entry.getBetOption();
            } else {
                int count = entry.getCountOfQPNumber();
                if (count < entry.getBetOption() - LfnEntry.BETOPTION_INTERVAL) {
                    throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY,
                            "The count of QP number must be greater than or equal with "
                                    + (entry.getBetOption() - LfnEntry.BETOPTION_INTERVAL));
                }
                return count;
            }
        }
    }

    public LfnStatOfSelectedNumberDao getStatOfSelectedNumberDao() {
        return statOfSelectedNumberDao;
    }

    public void setStatOfSelectedNumberDao(LfnStatOfSelectedNumberDao statOfSelectedNumberDao) {
        this.statOfSelectedNumberDao = statOfSelectedNumberDao;
    }

}
