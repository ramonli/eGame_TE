package com.mpos.lottery.te.gameimpl.magic100.sale.service;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.game.Magic100GameInstance;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumber;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumberSequence;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.magic100.sale.RequeuedNumbers;
import com.mpos.lottery.te.gameimpl.magic100.sale.RequeuedNumbersItem;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.LuckyNumberSequenceDao;
import com.mpos.lottery.te.gameimpl.magic100.sale.dao.RequeuedNumbersDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DefaultLuckyNumberService implements LuckyNumberService {
    private Log logger = LogFactory.getLog(DefaultLuckyNumberService.class);
    private LuckyNumberDao luckyNumberDao;
    private RequeuedNumbersDao requeuedNumbersDao;
    private LuckyNumberSequenceDao luckyNumberSequenceDao;
    private BaseJpaDao baseJpaDao;
    private UUIDService uuidService;

    /**
     * Determine which range of lucky numbers should be sold. <code>LuckyNumberSequence</code> will be used to record
     * the next sequence number of main cycle. At the initial status, <code>LuckyNumberSequence.nextSequence</code> will
     * be 1, and once sold a range, for example 1~3, the LuckyNumberSequence.nextSequence will be updated to 4, even
     * this sale is cancelled later, the next sequence will stay as 4.
     * <p/>
     * If a sale is cancelled, the corresponding range will be saved as <code>RequeueNumbers</code>, and will be sold at
     * next cycle(aims to make sure all numbers should be sold, and their prize amount should be paid as well, it is a
     * fair game).
     * <p/>
     * Below rules must be followed:
     * <ol>
     * <li>Sold requeued ranges as much as possible first, if no matched range numbers, get numbers from main cycle</li>
     * <li>The numbers sold in a single request must be sequential.</li>
     * </ol>
     * <p/>
     * Below gives a user scenario:
     * <h2>Case #1(LuckyNumberSequence.nextSequence=1)</h2>
     * <ol>
     * <li>Player A requests to buy a range(countOfNumber=3).</li>
     * <li>System lookup all requeued ranges by (coungOfValidNumber>=3) first, if found, use the requeued range, and
     * mark them as invalid. If numbers are retrieved from requeued ranges, no LuckyNumberSequence.nextSequence will be
     * updated.</li>
     * <li>If no matched requeued ranges found, then lookup main cycle(beginNumber=LuckyNumberSequence.nextSequence,
     * countOfNumber=3), and update LuckyNumberSequence.nextSequence to 4</li>
     * </ol>
     * At a give time point, only one thread can enter this method, other threads will be blocked, otherwise different
     * player may get same numbers in a same cycle.
     * 
     * @param respCtx
     *            The context of transaction.
     * @param clientTicket
     *            THe client sale request.
     * @return the list of lucky numbers which will be sold to current player.
     */
    public List<LuckyNumber> determine(Context respCtx, Magic100Ticket clientTicket) throws ApplicationException {
        if (clientTicket.getEntries().size() == 0) {
            throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY,
                    "There is no any entry in ticket(serialNo=" + clientTicket.getSerialNo() + ").");
        }
        // there should be only one entry in raffle ticket...
        if (clientTicket.getEntries().size() > 1) {
            logger.warn("There should be only one entry in ticket, however total " + clientTicket.getEntries().size()
                    + " entries found of ticket(serialNo=" + clientTicket.getSerialNo() + ").");
        }
        Magic100Entry mEntry = (Magic100Entry) clientTicket.getEntries().get(0);
        // lookup current sequence first, and lock it to prevent
        // multi-access...this is a global lock
        LuckyNumberSequence luckyNumberSequence = this.lookupSequence((Magic100GameInstance) clientTicket
                .getGameInstance());

        List<LuckyNumber> luckyNumbers = new LinkedList<LuckyNumber>();
        // lookup requeued ranges first, the rule is sold requeued range first
        List<RequeuedNumbers> ranges = this.getRequeuedNumbersDao().findByGameInstanceAndCountOfValidNumber(
                clientTicket.getGameInstance().getId(), (int) mEntry.getTotalBets());
        if (ranges.size() > 0) {
            // use a requeued range if found
            RequeuedNumbers range = ranges.get(0);
            if (logger.isDebugEnabled()) {
                logger.debug("Use lucky number from REQUEUED RANGE(id=" + range.getId() + ") instead of main cycle.");
            }
            List<RequeuedNumbersItem> validItems = range.lookupValidItems((int) mEntry.getTotalBets());

            // update requeued range to prevent repeated usage.
            range.setCountOfValidNumbers(range.getCountOfValidNumbers() - (int) mEntry.getTotalBets());
            range.setUpdateTime(respCtx.getTransaction().getUpdateTime());
            this.getBaseJpaDao().update(range);

            for (RequeuedNumbersItem item : validItems) {
                luckyNumbers.add(new LuckyNumber(item));
                item.setState(RequeuedNumbersItem.STATE_INVALID);
                item.setUpdateTime(respCtx.getTransaction().getUpdateTime());
            }
            this.getBaseJpaDao().update(validItems);
        } else {
            // lookup numbers from main cycle
            luckyNumbers = this.getLuckyNumberDao().findBySeuqnce(luckyNumberSequence.getNextSequence(),
                    clientTicket.getGameInstance().getId(), (int) mEntry.getTotalBets());

            // update the sequence log
            luckyNumberSequence.setNextSequence(this.determineNextSequence(luckyNumbers.get(luckyNumbers.size() - 1),
                    (Magic100GameInstance) clientTicket.getGameInstance()));
            luckyNumberSequence.setLastestPlayer(clientTicket.getUser() != null
                    ? clientTicket.getUser().getMobile()
                    : null);
            this.getLuckyNumberSequenceDao().update(luckyNumberSequence);
        }
        return luckyNumbers;
    }

    @Override
    public void cancel(Context respCtx, Magic100Ticket ticket) throws ApplicationException {
        Long[] sequences = new Long[ticket.getEntries().size()];
        for (int i = 0; i < ticket.getEntries().size(); i++) {
            Magic100Entry entry = (Magic100Entry) ticket.getEntries().get(i);
            sequences[i] = entry.getSequenceOfNumber();
        }
        // fix bug#6244
        List<LuckyNumber> cancelNumbers = this.getLuckyNumberDao().findBySeuqnces(ticket.getGameInstance().getId(),
                sequences);

        RequeuedNumbers cancelRequeueNum = new RequeuedNumbers();
        cancelRequeueNum.setId(this.getUuidService().getGeneralID());
        cancelRequeueNum.setCreateTime(respCtx.getTransaction().getCreateTime());
        cancelRequeueNum.setUpdateTime(cancelRequeueNum.getCreateTime());
        cancelRequeueNum.setTransactionId(respCtx.getTransaction().getId());
        // cancelRequeueNum.setBeginOfValidNumbers(beginOfValidNumbers)
        cancelRequeueNum.setCountOfNumbers(cancelNumbers.size());
        cancelRequeueNum.setCountOfValidNumbers(cancelNumbers.size());
        cancelRequeueNum.setGameInstanceId(ticket.getGameInstance().getId());

        this.getRequeuedNumbersDao().insert(cancelRequeueNum);

        List<RequeuedNumbersItem> requeuedItems = new ArrayList<RequeuedNumbersItem>();
        for (LuckyNumber luckyNumber : cancelNumbers) {
            RequeuedNumbersItem requeueItem = new RequeuedNumbersItem(luckyNumber);
            requeueItem.setId(this.getUuidService().getGeneralID());
            requeueItem.setCreateTime(respCtx.getTransaction().getCreateTime());
            requeueItem.setUpdateTime(requeueItem.getCreateTime());
            requeueItem.setRequeuedNumbers(cancelRequeueNum);
            requeueItem.setState(RequeuedNumbersItem.STATE_VALID);
            requeuedItems.add(requeueItem);
        }
        this.getBaseJpaDao().insert(requeuedItems);
    }

    // -------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------

    protected LuckyNumberSequence lookupSequence(Magic100GameInstance gameInstance) throws ApplicationException {
        return this.getLuckyNumberSequenceDao().lookup(gameInstance.getGameId());
    }

    /**
     * Determine what is the next sequence? The sequence must be in range of <code>gameInstance.startOfSequence</code>
     * and <code>gameInstance.endOfSequence</code>
     */
    protected long determineNextSequence(LuckyNumber lastLuckyNumber, Magic100GameInstance gameInstance) {
        long nextSeq = lastLuckyNumber.getSequenceOfNumber() + 1;
        if (nextSeq > gameInstance.getEndOfSequence()) {
            nextSeq = gameInstance.getStartOfSequence();
        }
        return nextSeq;
    }

    // -------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -------------------------------------------------------------
    public LuckyNumberDao getLuckyNumberDao() {
        return luckyNumberDao;
    }

    public void setLuckyNumberDao(LuckyNumberDao luckyNumberDao) {
        this.luckyNumberDao = luckyNumberDao;
    }

    public LuckyNumberSequenceDao getLuckyNumberSequenceDao() {
        return luckyNumberSequenceDao;
    }

    public void setLuckyNumberSequenceDao(LuckyNumberSequenceDao luckyNumberSequenceDao) {
        this.luckyNumberSequenceDao = luckyNumberSequenceDao;
    }

    public RequeuedNumbersDao getRequeuedNumbersDao() {
        return requeuedNumbersDao;
    }

    public void setRequeuedNumbersDao(RequeuedNumbersDao requeuedNumbersDao) {
        this.requeuedNumbersDao = requeuedNumbersDao;
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

}
