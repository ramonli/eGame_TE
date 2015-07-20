package com.mpos.lottery.te.gameimpl.bingo.sale.service;

import com.mpos.lottery.te.common.dao.BaseEntity;
import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoFunType;
import com.mpos.lottery.te.gameimpl.bingo.game.BingoGameInstance;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoEntryRef;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicket;
import com.mpos.lottery.te.gameimpl.bingo.sale.BingoTicketRef;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoEntryRefDao;
import com.mpos.lottery.te.gameimpl.bingo.sale.dao.BingoTicketRefDao;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.port.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

/**
 * If player doesn't pick any BINGO numbers, system will pick in-advance generated tickets.
 * 
 * @author Ramon
 */
@Service("pregeneratedTicketRefLookupService")
public class PregeneratedTicketRefLookupService implements TicketRefLookupService {
    private static Log logger = LogFactory.getLog(PregeneratedTicketRefLookupService.class);
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "bingoTicketRefDao")
    private BingoTicketRefDao bingoTicketRefDao;
    @Resource(name = "bingoEntryRefDao")
    private BingoEntryRefDao bingoEntryRefDao;
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lookup a in-advance generated ticket for client. Only those tickets/entries of 'new' status can be picked, and
     * the count of entries must obey the setting of operation parameters, refer to
     * {@link BingoFunType#getMaxEntriesInTicket()}.
     * 
     * @param respCtx
     *            The context of current bingo sale transaction.
     * @param clientTicket
     *            The client sale request. The following components must be assembled by the backend: gameInstance
     * @param pickEntries
     *            Whether need to pick entries for player? As player donesn't have to picker numbers, in this case the
     *            backend will pick those pre-generated numbers automatically.
     * @return A in-advance generated ticket of 'new' status.
     * @throws ApplicationException
     *             if any business exception encountered.
     */
    @Override
    public BingoTicketRef lookupTicket(Context respCtx, BingoTicket clientTicket, boolean pickEntries)
            throws ApplicationException {
        // lookup a new ticket first
        BingoTicketRef ticketRef = this.lockTicket(respCtx, clientTicket);
        if (logger.isDebugEnabled()) {
            logger.debug("Locked referenced ticket(id=" + ticketRef.getId() + ",serialNo="
                    + ticketRef.getImportTicketSerialNo() + ") successfully.");
        }

        if (pickEntries) {
            // lookup 'new' entries then
            List<BaseEntity> entries = this.lockEntries(respCtx, clientTicket);
            for (BaseEntity entry : entries) {
                BingoEntryRef entryRef = (BingoEntryRef) entry;
                if (logger.isDebugEnabled()) {
                    logger.debug("Lock referenced entry(id=" + entryRef.getId() + ",selectedNumber:"
                            + entryRef.getSelectedNumber() + ") successfully.");
                }
                ticketRef.getEntryRefs().add(entryRef);
            }
        }

        return ticketRef;
    }

    /**
     * Lock a new {@code BingoTicketRef}. As service is under concurrent access, we must ensure that a ticket can be
     * only sold to a single player.
     * <p/>
     * If fail to lock a entity of {@code BingoTicketRef}, the next entity will be tried immediately, and will continue
     * this process until reach the last available entity or lock a entity successfully. By locking a entity, we ensure
     * that no other threads can get the same entity instantaneously.
     */
    protected BingoTicketRef lockTicket(Context respCtx, BingoTicket clientTicket) throws ApplicationException {
        BingoGameInstance gameInstance = (BingoGameInstance) clientTicket.getGameInstance();

        // lock game instance first
        this.getEntityManager().lock(gameInstance, LockModeType.PESSIMISTIC_READ);

        BingoTicketRef ticketRef = this.getBingoTicketRefDao().findByGameInstanceAndSequence(gameInstance.getId(),
                gameInstance.getCurrentSequence());
        if (ticketRef == null) {
            throw new SystemException("No entity(" + BingoTicketRef.class + ") found by gameInstanceId="
                    + gameInstance.getId() + ",currentSequence=" + gameInstance.getCurrentSequence());
        }

        // update current sequence
        gameInstance.setCurrentSequence(gameInstance.getCurrentSequence() + 1);

        return ticketRef;
    }

    /**
     * Lookup 'new' referenced bingo entries. Refer to {@link #lockTicket(Context, BingoTicket)} for concurrent access
     * restriction.
     */
    protected List<BaseEntity> lockEntries(Context respCtx, BingoTicket clientTicket) throws ApplicationException {
        BaseGameInstance gameInstance = clientTicket.getGameInstance();
        BingoFunType bingoFunType = this.getBaseJpaDao().findById(BingoFunType.class,
                gameInstance.getGame().getFunTypeId());

        int countOfLock = bingoFunType.getMaxEntriesInTicket();
        // lookup all ticket of new status
        List<BingoEntryRef> entryRefs = this.getBingoEntryRefDao().findByGameInstanceAndState(gameInstance.getId(),
                BingoTicketRef.STATUS_NEW);
        if (entryRefs.size() < countOfLock) {
            throw new ApplicationException(SystemException.CODE_NO_ENOUGH_REF_ENTRY,
                    "No 'new' reference entry found of given game instance(id=" + gameInstance.getId() + "), expected:"
                            + countOfLock + ", actual:" + entryRefs.size());
        }

        List<BaseEntity> locks = this.lock(entryRefs, countOfLock);
        if (locks.size() < countOfLock) {
            throw new ApplicationException(SystemException.CODE_NO_ENOUGH_REF_ENTRY,
                    "No enough 'new' reference entry found of given game instance(id=" + gameInstance.getId()
                            + "), expected:" + countOfLock + ", actual:" + locks.size() + ", however total "
                            + (entryRefs.size() - locks.size()) + " have been locked by other players.");
        }
        return locks;
    }

    protected List<BaseEntity> lock(List<? extends BaseEntity> entities, int countOfLock) {
        Map<String, Object> hints = new HashMap<String, Object>();
        /**
         * Regarding what standard hints properties are supported, refer to "#3.4.4.3 Lock Mode Properties and Use" of
         * JPA2.1 specification document.
         * <p/>
         * If no hint 'javax.persistence.lock.timeout' supplied, the underlying SQL will be:
         * <p/>
         * select ID from XX where ID =111 for update
         * <p/>
         * If provide this hint and set value to 0, the SQL will be:
         * <p/>
         * select ID from XX where ID =111 for update for update nowait
         * <p/>
         * If provide this hint and set a value which is greater than 0(the measurement unit for hint is millisecond,
         * however it is second in SQL, that says if your provide hint with value 10 milliseconds, the SQL will tell you
         * wait 0 seconds ), the SQL will be:
         * <p/>
         * select ID from XX where ID =111 for update for update wait XX
         * <p/>
         */
        hints.put("javax.persistence.lock.timeout", 0);
        List<BaseEntity> locks = new LinkedList<BaseEntity>();
        int index = 0;
        for (BaseEntity entity : entities) {
            // check whether the entity is managed first.
            if (!this.getEntityManager().contains(entity)) {
                throw new SystemException("Entity(" + entity + ",id=" + entity.getId()
                        + ") isn't managed, can't be locked.");
            }
            try {
                this.getEntityManager().lock(entity, LockModeType.PESSIMISTIC_READ, hints);
                if (index < countOfLock) {
                    locks.add(entity);
                    index++;
                } else {
                    // got enough locked entities successfully, break then
                    break;
                }
            } catch (Exception e) {
                Throwable rootCause = SimpleToolkit.getRootCause(e);
                if (rootCause instanceof SQLException) {
                    SQLException sqlRoot = (SQLException) rootCause;
                    // ORA-00054: resource busy and acquire with NOWAIT
                    // specified or timeout expired
                    if (54 == sqlRoot.getErrorCode()) {
                        logger.warn("Entity(" + entity + ",id=" + entity.getId()
                                + ") has been locked by other threads, will try other entitiy.");
                        continue;
                    }
                }

                // throwout any other exception
                throw new SystemException(e);
            }
        }

        return locks;
    }

    // ----------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // ----------------------------------------------------------------

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public BingoTicketRefDao getBingoTicketRefDao() {
        return bingoTicketRefDao;
    }

    public void setBingoTicketRefDao(BingoTicketRefDao bingoTicketRefDao) {
        this.bingoTicketRefDao = bingoTicketRefDao;
    }

    public BingoEntryRefDao getBingoEntryRefDao() {
        return bingoEntryRefDao;
    }

    public void setBingoEntryRefDao(BingoEntryRefDao bingoEntryRefDao) {
        this.bingoEntryRefDao = bingoEntryRefDao;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

}
