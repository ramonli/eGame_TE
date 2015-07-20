package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.OfflineTicketLog;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.merchant.service.CreditService;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.service.TransactionService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

import javax.annotation.Resource;

public class DefaultOfflineTicketService implements OfflineTicketService {
    private Log logger = LogFactory.getLog(DefaultOfflineTicketService.class);
    // SPRING DEPENDENCIES
    @Resource(name = "uuidManager")
    private UUIDService uuidService;
    @Resource(name = "transService")
    private TransactionService transactionService;
    @Resource(name = "creditService")
    private CreditService creditService;
    @Resource(name = "merchantService")
    private MerchantService merchantService;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    // setup the dependency in XML file
    private GameInstanceService gameInstanceService;
    @Resource(name = "baseTicketDao")
    private BaseTicketDao baseTicketDao;
    @Resource(name = "baseEntryDao")
    private BaseEntryDao baseEntryDao;

    @Override
    public BaseTicket sync(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        BaseTicket hostTicket = null;
        clientTicket.setTransaction(respCtx.getTransaction());
        // verify the status of game instance...cant be 'in progress of winner
        // analysis'.
        List<? extends BaseGameInstance> gameInstances = this.getGameInstanceService().enquiryMultiDraw(respCtx,
                clientTicket.getGameInstance().getGameId(), clientTicket.getGameInstance().getNumber(),
                clientTicket.getMultipleDraws());
        clientTicket.setGameInstance(gameInstances.get(0));
        // lookup current active game instance
        List<? extends BaseGameInstance> currentGameInstances = this.getGameInstanceService().enquirySaleReady(respCtx,
                clientTicket.getGameInstance().getGameId());
        if (logger.isDebugEnabled()) {
            logger.debug("The ID of current active game instance is " + currentGameInstances.get(0).getId()
                    + " of gameType:" + currentGameInstances.get(0).getGame().getType());
        }

        for (int i = 0; i < gameInstances.size(); i++) {
            // can't upload offline sales when a game instance is in winer
            // analysis
            if (BaseGameInstance.STATE_WINANALYSIS_STARTED == gameInstances.get(i).getState()) {
                throw new ApplicationException(SystemException.CODE_INPROGRESSOF_WINNINGANALYSIS, "Game instance("
                        + gameInstances.get(i).getKey() + " is under winner analysis, can't perform synchrotion now.");
            }

            BaseTicket generatedTicket = (BaseTicket) clientTicket.clone();
            // assemble ticket entity
            generatedTicket.setId(this.getUuidService().getGeneralID());
            generatedTicket.setCreateTime(respCtx.getTransaction().getCreateTime());
            generatedTicket.setUpdateTime(generatedTicket.getCreateTime());
            generatedTicket.setGameInstance(gameInstances.get(0));
            generatedTicket.setTotalAmount(generatedTicket.calculateMultipleDrawAmount());
            generatedTicket.setMultipleDraws(i == 0 ? generatedTicket.getMultipleDraws() : 0);
            generatedTicket.setOperatorId(respCtx.getTransaction().getOperatorId());
            generatedTicket.setMerchantId((int) respCtx.getTransaction().getMerchantId());
            generatedTicket.setDevId((int) respCtx.getTransaction().getDeviceId());
            generatedTicket.setOffline(true);
            generatedTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
            // if (clientTicket.getUser() != null) {
            // clientTicket.setMobile(clientTicket.getUser().getMobile());
            // clientTicket.setCreditCardSN(clientTicket.getUser().getCreditCardSN());
            // clientTicket.setUserId(clientTicket.getUser().getId());
            // }
            generatedTicket.setTicketFrom(respCtx.getGpe().getTicketFrom());
            this.customizeAssembleTicket(respCtx, generatedTicket, clientTicket);
            if (i == 0) {
                hostTicket = generatedTicket;
            }

            this.getBaseTicketDao().insert(generatedTicket);

            // generate Offline Ticket Log
            OfflineTicketLog ot = this.generateOfflineTicketLog(respCtx, currentGameInstances, generatedTicket);
            this.getBaseJpaDao().insert(ot);
        }

        // persist entries
        if (clientTicket.getEntries().size() > 0) {
            for (int i = 0; i < clientTicket.getEntries().size(); i++) {
                BaseEntry generatedEntry = clientTicket.getEntries().get(i);
                generatedEntry.setId(this.getUuidService().getGeneralID());
                generatedEntry.setTicketSerialNo(clientTicket.getSerialNo());
                generatedEntry.setEntryNo((i + 1) + "");
                this.customizeAssembleEntry(respCtx, clientTicket, generatedEntry);

                this.getBaseEntryDao().insert(generatedEntry);
            }
        }

        // update the sale balance of operator
        // check and update the credit level
        if (BaseTicket.TICKET_TYPE_NORMAL == clientTicket.getTicketType()) {
            long merchantId = respCtx.getTransaction().getMerchantId();
            String gameId = clientTicket.getGameInstance().getGame().getId();
            this.getCreditService().credit(respCtx.getTransaction().getOperatorId(), merchantId,
                    clientTicket.getTotalAmount(), gameId, false, true, clientTicket.isSoldByCreditCard());
        }
        return hostTicket;
    }

    // -----------------------------------------------------------
    // HELP METHODS
    // -----------------------------------------------------------

    protected OfflineTicketLog generateOfflineTicketLog(Context<?> respCtx,
            List<? extends BaseGameInstance> currentGameInstances, BaseTicket generatedTicket)
            throws ApplicationException {
        OfflineTicketLog ot = new OfflineTicketLog();
        ot.setId(this.getUuidService().getGeneralID());
        ot.setTransactionId(respCtx.getTransaction().getId());
        ot.setSerialNo(generatedTicket.getSerialNo());
        ot.setGameInstanceId(generatedTicket.getGameInstance().getId());
        ot.setGameId(generatedTicket.getGameInstance().getGame().getId());
        ot.setGameType(generatedTicket.getGameInstance().getGame().getType());
        ot.setUploadedGameInstanceId(currentGameInstances.get(0).getId());
        ot.setCreateTime(respCtx.getTransaction().getCreateTime());
        ot.setUpdateTime(ot.getCreateTime());
        ot.setOperatorId(respCtx.getTransaction().getOperatorId());
        ot.setMerchantId(respCtx.getTransaction().getMerchantId());
        ot.setDevId(respCtx.getTransaction().getDeviceId());
        ot.setTotalAmount(generatedTicket.getTotalAmount());
        ot.setTicketType(generatedTicket.getTicketType());
        ot.setTicketFrom(generatedTicket.getTicketFrom());
        return ot;
    }

    // -----------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------

    /**
     * Template method for subclass to implement specific operations.
     */
    protected void customizeAssembleTicket(Context<?> respCtx, BaseTicket generatedTicket, BaseTicket clientTicket) {
    }

    /**
     * Template method for subclass to implement specific operations.
     */
    protected void customizeAssembleEntry(Context<?> respCtx, BaseTicket clientTicket, BaseEntry genereatedEntry) {

    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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

    public CreditService getCreditService() {
        return creditService;
    }

    public void setCreditService(CreditService creditService) {
        this.creditService = creditService;
    }

}
