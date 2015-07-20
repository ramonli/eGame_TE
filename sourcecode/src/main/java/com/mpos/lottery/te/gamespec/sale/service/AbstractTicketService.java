package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.Barcoder;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTamperProofTicket;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.DummyTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.gamespec.sale.support.TicketHelper;
import com.mpos.lottery.te.gamespec.sale.support.validator.TicketValidator;
import com.mpos.lottery.te.gamespec.sale.web.QPEnquiryDto;
import com.mpos.lottery.te.merchant.domain.Merchant;
import com.mpos.lottery.te.merchant.service.MerchantService;
import com.mpos.lottery.te.merchant.service.balance.BalanceService;
import com.mpos.lottery.te.merchant.service.balance.SaleBalanceStrategy;
import com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.port.domain.router.RoutineKey;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.thirdpartyservice.amqp.MessagePack;
import com.mpos.lottery.te.thirdpartyservice.amqp.TeTransactionMessageSerializer;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.User;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.trans.domain.logic.AbstractReversalOrCancelStrategy;
import com.mpos.lottery.te.trans.service.TransactionService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class AbstractTicketService extends AbstractReversalOrCancelStrategy implements CompositeTicketService {
    private Log logger = LogFactory.getLog(AbstractTicketService.class);
    // can overwrite this value by spring injection
    private String exchangeName = MessagePack.PREFIX + ".200";
    // SPRING DEPENDENCIES
    private UUIDService uuidService;
    private TransactionService transactionService;
    private MerchantService merchantService;
    private BaseJpaDao baseJpaDao;
    private TicketValidator ticketValidator;
    private GameInstanceService gameInstanceService;
    private BaseTicketDao baseTicketDao;
    private BaseEntryDao baseEntryDao;
    @PersistenceContext
    private EntityManager entityManager;
    // @PersistenceContext(unitName = "lottery_te")
    // protected EntityManager entityManager;
    private RiskControlService riskControlService = new NoRiskControlService();
    @Resource(name = "saleCommissionBalanceService")
    private CommissionBalanceService saleCommissionBalanceService;
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        // register a TicketService handler to enquiry ticket...If it is a sale
        // transaction, ticket will be returned
        this.getTransactionService().registerTicketEnquiry(this.supportedGameType(), this);
    }

    @Override
    public RoutineKey supportedReversalRoutineKey() {
        return new RoutineKey(this.supportedGameType().getType(), TransactionType.SELL_TICKET.getRequestType(), null);
    }

    @Override
    public BaseTicket enquiry(Context<?> respCtx, BaseTicket clientTicket, boolean fetchEntries)
            throws ApplicationException {
        // Lookup ticket information
        List<? extends BaseTicket> tickets = this.lookupTickets(respCtx, clientTicket);

        // lookup entry information
        List<? extends BaseEntry> entries = null;
        if (fetchEntries) {
            // lookup Entries
            entries = this.lookupEntries(respCtx, clientTicket);
        }
        BaseTicket ticket = TicketHelper.assemblePhysicalTicket(tickets, entries);

        if (ticket.getUserId() != null || ticket.getMobile() != null || ticket.getCreditCardSN() != null) {
            ticket.setUser(new User(ticket.getUserId(), ticket.getMobile(), ticket.getCreditCardSN()));
        }

        this.doEnquiryTicket(respCtx, tickets, ticket);

        respCtx.getTransaction().setGameId(ticket.getGameInstance().getGame().getId());
        respCtx.getTransaction().setTotalAmount(ticket.getTotalAmount());
        respCtx.getTransaction().setTicketSerialNo(ticket.getSerialNo());

        return ticket;
    }

    @Override
    public final List<? extends BaseTicket> sell(Context<?> respCtx, BaseTicket baseClientTicket)
            throws ApplicationException {
        if (baseClientTicket == null) {
            throw new IllegalArgumentException("Argument 'baseClientTicket' can NOT be null.");
        }
        BaseTicket clientTicket = baseClientTicket;
        if (clientTicket.getMultipleDraws() <= 0) {
            throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY, "THe multi-draw must be at least 1");
        }

        // Lookup ready-for-sale game instance and validate game instance
        List<? extends BaseGameInstance> gameInstances = this.lookupSaleReadyGameInstance(respCtx, clientTicket);

        // Assemble client ticket request
        clientTicket.setGameInstance(gameInstances.get(0));
        clientTicket.setRawSerialNo(this.getUuidService().getTicketSerialNo(
                clientTicket.getGameInstance().getGame().getType()));
        // set barcode
        clientTicket.setBarcode(new Barcoder(clientTicket.getGameInstance().getGame().getType(), clientTicket
                .getRawSerialNo()).getBarcode());
        clientTicket.setLastDrawNo(gameInstances.get(gameInstances.size() - 1).getNumber());
        clientTicket.setPIN(SimpleToolkit.md5(clientTicket.getPIN()));
        clientTicket.setValidationCode(BaseTicket.generateValidationCode());
        // custom DTO
        this.customAssembleClientTicket(respCtx, clientTicket);
        this.generateQPWhenSale(respCtx, clientTicket);

        // Validate ticket request
        this.validateTicketRequest(respCtx, clientTicket);
        // Verify whether merchant can make sale
        this.getMerchantService().allowSale(respCtx.getTransaction(), clientTicket);
        // Verify risk control
        this.getRiskControlService().riskControl(respCtx, clientTicket, gameInstances);

        // generate tickets
        List<BaseTicket> tickets = this.generateMultiplDrawsTickets(respCtx, clientTicket, gameInstances);
        // save tickets and entries
        if (tickets.size() > 0) {
            this.getBaseJpaDao().insert(tickets);
            if (tickets.get(0).getEntries().size() > 0) {
                this.getBaseJpaDao().insert(tickets.get(0).getEntries());
            }
        }

        this.doSuccessfulSale(respCtx, clientTicket);

        // update transaction
        respCtx.getTransaction().setGameId(clientTicket.getGameInstance().getGame().getId());
        respCtx.getTransaction().setTotalAmount(clientTicket.getTotalAmount());
        respCtx.getTransaction().setTicketSerialNo(clientTicket.getSerialNo());

        // this.getEntityManager().flush();

        // check and update the credit level
        if (BaseTicket.TICKET_TYPE_NORMAL == clientTicket.getTicketType()) {
            this.updateCredit(respCtx, clientTicket, true);
        }
        // update instantaneous sale
        // this.snapshotSale(respCtx, tickets, true);

        if (MLotteryContext.getInstance().getBoolean("amqp.messagepublish.enable", false)) {
            // assemble TE transaction message for data publication
            respCtx.setTransMessage(new MessagePack(this.getExchangeName(), respCtx.generateAmqpRoutingKey(),
                    ((TeTransactionMessageSerializer) baseClientTicket).toProtoMessage(respCtx)));
        }
        return tickets;
    }

    @Override
    public QPEnquiryDto enquiryQP(Context<?> respCtx, QPEnquiryDto dto) throws ApplicationException {
        if (dto.getCountOfEntries() <= 0 || dto.getCountOfNumbers() <= 0) {
            throw new ApplicationException(SystemException.CODE_WRONG_MESSAGEBODY,
                    "Both the count of QP numbers and count of entries must be greater than 0");
        }
        for (int i = 0; i < dto.getCountOfEntries(); i++) {
            String qp = this.doGeneratingQP(
                    respCtx,
                    this.getGameInstanceService().enquiry(respCtx, dto.getGameInstance().getGameId(),
                            dto.getGameInstance().getNumber()), dto.getCountOfNumbers());
            BaseEntry entry = new BaseEntry();
            entry.setSelectNumber(qp);
            dto.getEntries().add(entry);
        }
        return dto;
    }

    @Override
    public final boolean cancelByTicket(Context<?> respCtx, BaseTicket baseClientTicket) throws ApplicationException {
        List<? extends BaseTicket> hostTickets = this.getBaseTicketDao().findBySerialNo(
                this.supportedGameType().getTicketType(), baseClientTicket.getSerialNo(), false);
        // check whether ticket exists and whether it has been cancelled
        BaseTicket boughtTicket = hostTickets.get(0);
        if (BaseTicket.STATUS_CANCELED == boughtTicket.getStatus()
                || BaseTicket.STATUS_CANCEL_DECLINED == boughtTicket.getStatus()) {
            throw new ApplicationException(SystemException.CODE_CANCELLED_TRANS,
                    "Can NOT cancel a cancel/cancel declined ticket(status=" + boughtTicket.getStatus()
                            + ") repeatedly.");
        }

        // validate game instance
        BaseTicket soldTicket = hostTickets.get(0);
        BaseGameInstance soldGameInstance = soldTicket.getGameInstance();
        boolean isCancelDecline = !(soldGameInstance.canCancelNormally());

        if (respCtx.getTransaction().isManualCancel()) {
            this.allowManualCancellation(soldGameInstance);
        }

        // update ticket
        for (BaseTicket ticket : hostTickets) {
            ticket.setStatus(isCancelDecline ? BaseTicket.STATUS_CANCEL_DECLINED : BaseTicket.STATUS_CANCELED);
            // transType shouldn't be influenced by cancel status.
            ticket.setTransType(respCtx.getTransaction().isManualCancel() ? TransactionType.CANCEL_BY_CLIENT_MANUALLY
                    .getRequestType() : respCtx.getTransaction().getType());
            ticket.setCountInPool(isCancelDecline ? true : false);
            ticket.setUpdateTime(respCtx.getTransaction().getCreateTime());
            this.customCancelTicket(respCtx, ticket);
            this.getBaseTicketDao().update(ticket);
        }

        // FIX#5032
        // If is automatical transactions, such as 'cancel by transaction', and
        // the operator hasn't been allocated to any merchant, in this case, the
        // merchant can be null... set the original merchant to this field. Why?
        // make dataXchange works
        if (!respCtx.getTransaction().isManualCancel() && respCtx.getMerchant() == null) {
            Merchant retailer = this.getBaseJpaDao().findById(Merchant.class, soldTicket.getMerchantId());
            if (retailer == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "merchant(id="
                        + soldTicket.getMerchantId() + ") doesn't exist.");
            }
            respCtx.setMerchant(retailer);
        }

        // update credit
        if (BaseTicket.TICKET_TYPE_NORMAL == soldTicket.getTicketType()) {
            respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, soldTicket.isSoldByCreditCard());
            // restore the credit of merchants
            Transaction saleTrans = soldTicket.getTransaction();
            // Object operatorOrMerchant =
            // this.getCreditService().credit(saleTrans.getOperatorId(),
            // saleTrans.getMerchantId(),
            // soldTicket.getTotalAmount().multiply(new
            // BigDecimal(hostTickets.size())),
            // saleTrans.getGameId(), true, true,
            // soldTicket.isSoldByCreditCard());
            Object operatorOrMerchant = this.getBalanceService().balance(respCtx, saleTrans,
                    BalanceService.BALANCE_TYPE_SALE, saleTrans.getOperatorId(), true);
            // generate balance logs
            this.getSaleCommissionBalanceService().cancelCommission(respCtx, soldTicket.getTransaction(),
                    operatorOrMerchant);
        }

        // Verify risk control
        this.getRiskControlService().cancelRiskControl(respCtx, hostTickets);

        this.doSuccessfulCancel(respCtx, soldTicket);

        return isCancelDecline;
    }

    @Override
    public final boolean cancelOrReverse(Context<?> respCtx, Transaction targetTrans) throws ApplicationException {
        if (targetTrans.getTicketSerialNo() == null) {
            throw new ApplicationException(SystemException.CODE_NO_TICKET,
                    "No ticket associates with transaction transaction(terminalId=" + targetTrans.getDeviceId()
                            + ",traceMessageId=" + targetTrans.getTraceMessageId() + ").");
        }

        DummyTicket ticket = new DummyTicket();
        ticket.setSerialNo(targetTrans.getTicketSerialNo());
        boolean isCancelDecline = this.cancelByTicket(respCtx, ticket);
        if (isCancelDecline) {
            respCtx.setResponseCode(SystemException.CODE_FAILTO_CANCEL);
        }

        /**
         * Refer to {@code cancelByTicket}, avoid publish 2 duplicated cancellation message.
         */
        respCtx.setTransMessage(null);
        return isCancelDecline;
    }

    // ---------------------------------------------------------------------
    // HELP METHODS
    // ---------------------------------------------------------------------

    /**
     * Generate the QP selected number. The specific sub-implementation should override this method.
     */
    protected void generateQPWhenSale(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        // template method
    }

    /**
     * Generate QP number according to the request.
     */
    protected String doGeneratingQP(Context<?> respCtx, BaseGameInstance gameInstance, int countOfQPNumber)
            throws ApplicationException {
        throw new UnsupportedOperationException("Unsupported method");
    }

    protected void doEnquiryTicket(Context<?> respCtx, List<? extends BaseTicket> hostTickets, BaseTicket physicalTicket)
            throws ApplicationException {
        // template method of enquiry ticket
    }

    protected void doSuccessfulSale(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        // template method for subclass to implement game-type specific logic
        // when a successful sale.
    }

    protected void customAssembleClientTicket(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        // template method
    }

    protected void doSuccessfulCancel(Context<?> respCtx, BaseTicket soldTicket) throws ApplicationException {
        // template method for subclass to implement game-type specific
        // cancellation logic.
    }

    /**
     * Lookup Sale-ready game instances for sale.
     */
    protected List<? extends BaseGameInstance> lookupSaleReadyGameInstance(Context<?> respCtx, BaseTicket clientTicket)
            throws ApplicationException {
        if (clientTicket.getGameInstance() == null) {
            throw new SystemException("NO game instance provided.");
        }
        if (clientTicket.getMultipleDraws() == 1 && this.entityManager.contains(clientTicket.getGameInstance())) {
            // if game instance entity is managed by entity manger, no need to
            // load it again.
            List gameInstances = new LinkedList<BaseGameInstance>();
            gameInstances.add(clientTicket.getGameInstance());
            return gameInstances;
        } else {
            List<? extends BaseGameInstance> gameInstances = this.getGameInstanceService().enquirySaleReady(respCtx,
                    clientTicket.getGameInstance().getGameId(), clientTicket.getGameInstance().getNumber(),
                    clientTicket.getMultipleDraws());
            return gameInstances;
        }
    }

    /**
     * Template method for subclass to customize the process of update a cancelled ticket
     */
    protected void customCancelTicket(Context<?> respCtx, BaseTicket ticket) {

    }

    protected void allowManualCancellation(BaseGameInstance soldGameInstance) throws ApplicationException {
        // check whether manual cancellation is allowed...only sold game
        // instance will be checked
        if (soldGameInstance.isSuspendManualCancel()) {
            throw new ApplicationException(SystemException.CODE_MANUAL_CANCEL_DISABLED,
                    "No manual cancellation allowed of game instance(gameId=" + soldGameInstance.getGame().getId()
                            + ",number=" + soldGameInstance.getNumber() + ").");
        }
        if (this.supportedGameType().getOperationParametersType() != null) {
            // verify whether manual cancellation is allowed of game
            BaseOperationParameter opParam = this.getBaseJpaDao().findById(
                    this.supportedGameType().getOperationParametersType(),
                    soldGameInstance.getGame().getOperatorParameterId());
            if (!opParam.isAllowManualCancellation()) {
                throw new ApplicationException(SystemException.CODE_MANUAL_CANCEL_DISABLED,
                        "No manual cancellation allowed of game (gameId=" + soldGameInstance.getGame().getId() + ").");
            }
        }
    }

    /**
     * Validate the ticket request, guarantee that all data are legal.
     */
    protected void validateTicketRequest(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        this.getTicketValidator().validate(respCtx, clientTicket, clientTicket.getGameInstance().getGame());
        // calculate the total bets of ticket
        int totalBets = 0;
        for (BaseEntry entry : clientTicket.getEntries()) {
            totalBets += entry.getTotalBets();
        }
        clientTicket.setTotalBets(totalBets * clientTicket.getMultipleDraws());
    }

    protected List<? extends BaseTicket> lookupTickets(Context respCtx, BaseTicket clientTicket)
            throws ApplicationException {
        return this.getBaseTicketDao().findBySerialNo(this.supportedGameType().getTicketType(),
                clientTicket.getSerialNo(), false);
    }

    protected List<? extends BaseEntry> lookupEntries(Context respCtx, BaseTicket clientTicket)
            throws ApplicationException {
        return this.getBaseEntryDao().findByTicketSerialNo(this.supportedGameType().getTicketEntryType(),
                clientTicket.getSerialNo(), false);
    }

    /**
     * Check and update credit level.
     */
    protected void updateCredit(Context respCtx, BaseTicket clientTicket, boolean isOnline) throws ApplicationException {
        respCtx.setProperty(SaleBalanceStrategy.PROP_SOLD_BY_CREDIT_CARD, clientTicket.isSoldByCreditCard());
        // long merchantId = respCtx.getTransaction().getMerchantId();
        // String gameId = clientTicket.getGameInstance().getGame().getId();
        // Object operatorOrMerchant =
        // this.getCreditService().credit(respCtx.getTransaction().getOperatorId(),
        // merchantId, clientTicket.getTotalAmount(), gameId, false, true,
        // clientTicket.isSoldByCreditCard());
        Object operatorOrMerchant = this.getBalanceService().balance(respCtx, respCtx.getTransaction(),
                BalanceService.BALANCE_TYPE_SALE, respCtx.getTransaction().getOperatorId(), false);
        // generate balance logs
        this.getSaleCommissionBalanceService().calCommission(respCtx, operatorOrMerchant);
    }

    /**
     * Generate multiple-draw tickets based on client sale request.
     */
    protected List<BaseTicket> generateMultiplDrawsTickets(Context respCtx, BaseTicket clientTicket,
            List<? extends BaseGameInstance> gameInstances) throws ApplicationException {
        List<BaseTicket> tickets = new ArrayList<BaseTicket>(0);
        if (clientTicket.getMultipleDraws() > gameInstances.size()) {
            throw new ApplicationException(SystemException.CODE_NOENOUGH_FUTUREGAMEDRAW, "There only "
                    + gameInstances.size() + " future draws, expected:" + clientTicket.getMultipleDraws());
        }

        List<BaseEntry> entries = assembleEntryEntities(respCtx, clientTicket);

        for (int i = 0; i < gameInstances.size(); i++) {
            // must new a instance
            BaseTicket t = (BaseTicket) clientTicket.clone();
            t.setId(this.getUuidService().getGeneralID());
            t.setStatus(BaseTicket.STATUS_ACCEPTED);
            t.setTotalAmount(clientTicket.calculateMultipleDrawAmount());
            t.setMultipleDraws(i == 0 ? clientTicket.getMultipleDraws() : 0);

            Transaction trans = respCtx.getTransaction();
            t.setTransaction(trans);
            t.setGameInstance(gameInstances.get(i));

            t.setOperatorId(trans.getOperatorId());
            t.setMerchantId((int) trans.getMerchantId());
            t.setDevId((int) trans.getDeviceId());
            if (clientTicket.getUser() != null) {
                t.setMobile(clientTicket.getUser().getMobile());
                t.setCreditCardSN(clientTicket.getUser().getCreditCardSN());
                t.setUserId(clientTicket.getUser().getId());
            }
            t.setTicketFrom(respCtx.getGpe().getTicketFrom());
            t.setTotalBets(clientTicket.getTotalBets() / clientTicket.getMultipleDraws());
            t.setEntries(entries);

            this.customizeAssembleTicket(t, clientTicket);

            tickets.add(t);
        }

        return tickets;
    }

    protected List<BaseEntry> assembleEntryEntities(Context respCtx, BaseTicket clientTicket)
            throws ApplicationException {
        // prepare entries first
        for (int i = 0; i < clientTicket.getEntries().size(); i++) {
            BaseEntry entry = clientTicket.getEntries().get(i);
            entry.setId(this.getUuidService().getGeneralID());
            entry.setTicketSerialNo(clientTicket.getSerialNo());
            entry.setEntryNo((i + 1) + "");
            this.customizeAssembleEntry(clientTicket, entry);
        }
        return clientTicket.getEntries();
    }

    protected void customizeAssembleEntry(BaseTicket clientTicket, BaseEntry entry) {
        // template method for subclass the customize the assembling process.
    }

    protected void customizeAssembleTicket(BaseTicket generatedTicket, BaseTicket clientTicket) {
        // generate extendTxt
        if (generatedTicket instanceof BaseTamperProofTicket) {
            ((BaseTamperProofTicket) generatedTicket).setExtendText(BaseTicket.generateExtendText(generatedTicket
                    .getEntries()));
        }
        // template method for subclass the customize the assembling process.
        // for example to generate HMAC for ticket entries.
    }

    // -----------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // -----------------------------------------------------------

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

    public TicketValidator getTicketValidator() {
        return ticketValidator;
    }

    public void setTicketValidator(TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
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

    public static void main(String args[]) {
        System.out.println(SimpleToolkit.md5("!!!!"));
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public BalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public RiskControlService getRiskControlService() {
        return riskControlService;
    }

    public void setRiskControlService(RiskControlService riskControlService) {
        this.riskControlService = riskControlService;
    }

    public CommissionBalanceService getSaleCommissionBalanceService() {
        return saleCommissionBalanceService;
    }

    public void setSaleCommissionBalanceService(CommissionBalanceService commissionBalanceService) {
        this.saleCommissionBalanceService = commissionBalanceService;
    }

}
