package com.mpos.lottery.te.gameimpl.lotto.sale.service.impl;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.AbstractTicketService;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

/**
 * LottoTicket manager.
 */
public class LottoTicketServiceImpl extends AbstractTicketService {
    private Log logger = LogFactory.getLog(LottoTicketServiceImpl.class);
    @Resource(name = "jpaLottoEntryDao")
    private LottoEntryDao lottoEntryDao;

    @Override
    public GameType supportedGameType() {
        return GameType.LOTTO;
    }

    protected List<? extends BaseEntry> lookupEntries(Context respCtx, BaseTicket clientTicket)
            throws ApplicationException {
        List<LottoEntry> entries = this.getLottoEntryDao().findBySerialNoAndMultiCount(clientTicket.getSerialNo());
        List<LottoEntry> respEntries = new LinkedList<LottoEntry>();
        for (LottoEntry entry : entries) {
            /**
             * Why we clone a new LottoEntry instead of simply using the existing one? The existing one is a JPA context
             * managed entity, we will do some calculation and updating on its fields, however this calculation is only
             * for responding to client, shouldn't commit to underlying database. If we do calcuation on JPA entity, it
             * will be merged to database automatically when transaction committed.
             */
            LottoEntry respEntry = (LottoEntry) entry.clone();
            respEntry.setTotalBets(entry.getTotalBets() * entry.getMultipleCount());
            respEntry.setEntryAmount(entry.getEntryAmount().multiply(new BigDecimal(entry.getMultipleCount())));
            respEntries.add(respEntry);
        }
        return respEntries;
    }

    @Override
    protected void customizeAssembleTicket(BaseTicket generatedTicket, BaseTicket clientTicket) {
        super.customizeAssembleTicket(generatedTicket, clientTicket);
    }

    /**
     * Assemble multiple-betting entries. Now player can specify the betting amount of a entry, and TE may need to split
     * a entry whose entry amount is multiple times of base amount into multiple entries.
     * <p/>
     * Lets suppose that a LOTTO game 5/16, and base amount is $0.25. Player buys a ticket as below,
     * 
     * <pre>
     * {@code
     *  <Ticket multipleDraws="1" totalAmount="4.0" PIN="!!!!!!">
     *     <GameDraw number="20090301" gameId="79d78c12392b23a5238" />
     *     <Entry selectedNumber="2,3,7,8,15" betOption="1" inputChannel="1">
     *     <Entry selectedNumber="1,3,5,8,12" betOption="1" inputChannel="1" entryAmount="0.75"/>
     *     <Entry selectedNumber="1,2,6,10,14,15" betOption="2" inputChannel="0" entryAmount="3.0"/>
     * </Ticket>}
     * </pre>
     * 
     * TE will generate below ticket entries(table 'TE_LOTTO_ENTRY'),
     * <table border="1">
     * <tr>
     * <td>Entry</td>
     * <td>Entry ID</td>
     * <td>Selected Number</td>
     * <td>Bet Option</td>
     * <td>Entry No.</td>
     * <td>Total Bets</td>
     * <td>Entry Amount</td>
     * <td>Multiple Count</td>
     * </tr>
     * <tr>
     * <td>2,3,7,8,15</td>
     * <td>1</td>
     * <td>2,3,7,8,15</td>
     * <td>1</td>
     * <td>1</td>
     * <td>1</td>
     * <td>0.25</td>
     * <td>1</td>
     * </tr>
     * <tr>
     * <td>1,3,5,8,12</td>
     * <td>2</td>
     * <td>1,3,5,8,12</td>
     * <td>1</td>
     * <td>2</td>
     * <td>1</td>
     * <td>0.25</td>
     * <td>3</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td>3</td>
     * <td>1,3,5,8,12</td>
     * <td>1</td>
     * <td>2</td>
     * <td>1</td>
     * <td>0.25</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td>4</td>
     * <td>1,3,5,8,12</td>
     * <td>1</td>
     * <td>2</td>
     * <td>1</td>
     * <td>0.25</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>1,2,6,10,14,15</td>
     * <td>5</td>
     * <td>1,2,6,10,14,15</td>
     * <td>2</td>
     * <td>3</td>
     * <td>6</td>
     * <td>1.5</td>
     * <td>2</td>
     * </tr>
     * <tr>
     * <td></td>
     * <td>6</td>
     * <td>1,2,6,10,14,15</td>
     * <td>2</td>
     * <td>3</td>
     * <td>6</td>
     * <td>1.5</td>
     * <td>0</td>
     * </tr>
     * </table>
     */
    @Override
    protected List<BaseEntry> assembleEntryEntities(Context respCtx, BaseTicket clientTicket)
            throws ApplicationException {
        List<BaseEntry> persistedEntries = new LinkedList<BaseEntry>();
        // prepare entries first
        for (int i = 0; i < clientTicket.getEntries().size(); i++) {
            LottoEntry entry = (LottoEntry) clientTicket.getEntries().get(i);
            if (entry.getMultipleCount() > 1) {
                for (int j = 0; j < entry.getMultipleCount(); j++) {
                    LottoEntry newEntry = (LottoEntry) entry.clone();
                    newEntry.setId(this.getUuidService().getGeneralID());
                    newEntry.setTicketSerialNo(clientTicket.getSerialNo());
                    newEntry.setEntryNo((i + 1) + "");
                    newEntry.setTotalBets(entry.getTotalBets() / entry.getMultipleCount());
                    newEntry.setEntryAmount(entry.getEntryAmount().divide(new BigDecimal(entry.getMultipleCount())));
                    newEntry.setMultipleCount(j == 0 ? entry.getMultipleCount() : 0);
                    persistedEntries.add(newEntry);
                }
            } else {
                entry.setId(this.getUuidService().getGeneralID());
                entry.setTicketSerialNo(clientTicket.getSerialNo());
                entry.setEntryNo((i + 1) + "");
                persistedEntries.add(entry);
            }
        }
        return persistedEntries;
    }

    public LottoEntryDao getLottoEntryDao() {
        return lottoEntryDao;
    }

    public void setLottoEntryDao(LottoEntryDao lottoEntryDao) {
        this.lottoEntryDao = lottoEntryDao;
    }

}
