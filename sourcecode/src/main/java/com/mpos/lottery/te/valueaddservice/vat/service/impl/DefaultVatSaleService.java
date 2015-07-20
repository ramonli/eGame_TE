package com.mpos.lottery.te.valueaddservice.vat.service.impl;

import com.mpos.lottery.te.common.dao.BaseJpaDao;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.MessageFormatException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gameimpl.raffle.sale.RaffleTicket;
import com.mpos.lottery.te.gamespec.game.BaseOperationParameter;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.game.service.GameInstanceService;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.service.TicketService;
import com.mpos.lottery.te.merchant.dao.MerchantDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.domain.TicketSerialSpec;
import com.mpos.lottery.te.sequence.service.UUIDService;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.service.TransactionService;
import com.mpos.lottery.te.valueaddservice.vat.OperatorBizType;
import com.mpos.lottery.te.valueaddservice.vat.VAT;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Game;
import com.mpos.lottery.te.valueaddservice.vat.Vat2Merchant;
import com.mpos.lottery.te.valueaddservice.vat.VatCompany;
import com.mpos.lottery.te.valueaddservice.vat.VatOperatorBalance;
import com.mpos.lottery.te.valueaddservice.vat.VatSaleTransaction;
import com.mpos.lottery.te.valueaddservice.vat.dao.OperatorBizTypeDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2GameDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.Vat2MerchantDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatCompanyDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatOperatorBalanceDao;
import com.mpos.lottery.te.valueaddservice.vat.dao.VatSaleTransactionDao;
import com.mpos.lottery.te.valueaddservice.vat.service.VatSaleService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import javax.annotation.Resource;

@Service("vatSaleService")
public class DefaultVatSaleService implements VatSaleService {
    private static Log logger = LogFactory.getLog(DefaultVatSaleService.class);
    @Resource(name = "transService")
    private TransactionService transactionService;
    @Resource(name = "operatorBizTypeDao")
    private OperatorBizTypeDao operatorBizTypeDao;
    @Resource(name = "vatDao")
    private VatDao vatDao;
    @Resource(name = "vat2MerchantDao")
    private Vat2MerchantDao vat2MerchantDao;
    @Resource(name = "vat2GameDao")
    private Vat2GameDao vat2GameDao;
    @Resource(name = "raffleTicketService")
    private TicketService raffleTicketService;
    @Resource(name = "magic100SaleService")
    private TicketService magic100TicketService;
    @Resource(name = "raffleGameInstanceService")
    private GameInstanceService raffleGameInstanceService;
    @Resource(name = "magic100GameInstanceService")
    private GameInstanceService magic100GameInstanceService;
    @Resource(name = "baseJpaDao")
    private BaseJpaDao baseJpaDao;
    @Resource(name = "uuidManager")
    private UUIDService uuidService;
    @Resource(name = "merchantDao")
    private MerchantDao merchantDao;
    @Resource(name = "vatCompanyDao")
    private VatCompanyDao vatCompanyDao;
    @Resource(name = "vatOperatorBalanceDao")
    private VatOperatorBalanceDao vatOperatorBalanceDao;
    @Resource(name = "vatSaleTransactionDao")
    private VatSaleTransactionDao vatSaleTransactionDao;

    @Override
    public VatSaleTransaction sell(Context<?> respCtx, VatSaleTransaction clientSale) throws ApplicationException {
        VatSaleTransaction vatClientTrans = (VatSaleTransaction) clientSale;
        // lookup VAT
        VAT vat = this.getVatDao().findByCode(vatClientTrans.getVatCode());
        if (vat == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOFOUND, "No valid VAT found by code("
                    + vatClientTrans.getVatCode() + ")");
        }

        // verify whether VAT has been allocated to merchant
        Vat2Merchant vat2Merchant = this.getVat2MerchantDao().findByVatAndMerchant(vat.getId(),
                respCtx.getTransaction().getMerchantId());
        if (vat2Merchant == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOT_ALLOCATED_TO_MERCHANT, "VAT(id=" + vat.getId()
                    + ") hasn't been allocated to merchant(id=" + respCtx.getTransaction().getMerchantId() + ") yet.");
        }

        // determine the business type of operator first
        OperatorBizType operatorBizType = this.getOperatorBizTypeDao().findByOperator(
                respCtx.getTransaction().getOperatorId());
        if (operatorBizType == null) {
            throw new SystemException("No operator BizType definition found by operatorId="
                    + respCtx.getTransaction().getOperatorId());
        }
        // lookup Game allocated to given VAT
        Vat2Game vat2Game = this.getVat2GameDao().findByVatAndBizType(vat.getId(), operatorBizType.getBusinessType());
        if (vat2Game == null) {
            throw new ApplicationException(SystemException.CODE_VAT_NOT_GAME_ALLOCATED,
                    "No Game has been allocated to Vat(id=" + vat.getId() + ") yet.");
        }
        vatClientTrans.setVatRate(vat2Game.getRate());

        // prepare VAT sale transaction
        vatClientTrans.mergeTransaction(respCtx.getTransaction());
        vatClientTrans.setVatRefNo(this.getUuidService().getReferenceNo(TicketSerialSpec.ONLINE_MODE));
        vatClientTrans.setVatId(vat.getId());
        vatClientTrans.setBusinessType(operatorBizType.getBusinessType());

        BaseTicket soldTicket = null;
        // B2B business mode
        if (OperatorBizType.BIZ_B2B.equalsIgnoreCase(operatorBizType.getBusinessType())) {
            if (vatClientTrans.getBuyerTaxNo() == null) {
                throw new MessageFormatException("For B2B operator, the buyerTaxNo must be provided.");
            }
            soldTicket = this.doSale(respCtx, vat, vat2Game, vatClientTrans);

            // lookup seller and buyer company information
            VatCompany seller = this.getVatCompanyDao().findByMerchant(respCtx.getTransaction().getMerchantId());
            VatCompany buyer = this.getVatCompanyDao().findByTaxNo(vatClientTrans.getBuyerTaxNo());
            if (buyer == null) {
                throw new ApplicationException(SystemException.CODE_NO_MERCHANT, "No merchant found by taxNo="
                        + vatClientTrans.getBuyerTaxNo());
            }
            // assemble vat sale transaction
            vatClientTrans.setBuyerCompanyId(buyer.getId());
            vatClientTrans.setSellerCompanyId(seller.getId());
        } else if (OperatorBizType.BIZ_B2C.equalsIgnoreCase(operatorBizType.getBusinessType())) {
            // B2C business mode
            soldTicket = this.doSale(respCtx, vat, vat2Game, vatClientTrans);
        } else {
            throw new SystemException("Unsupported business type:" + operatorBizType.getBusinessType());
        }

        // assemble vat sale transaction after sale
        if (soldTicket != null) { // maybe no ticket will be sold
            vatClientTrans.setTicket(soldTicket);
            if (logger.isDebugEnabled()) {
                logger.debug("Operator(id=" + operatorBizType.getOperatorId() + ") is in "
                        + (operatorBizType.getBizTypeString()) + ", has bought ticket(serialNO="
                        + soldTicket.getRawSerialNo() + ") of game Type: "
                        + GameType.fromType(vat2Game.getGame().getType()));
            }

            vatClientTrans.setSaleTotalAmount(soldTicket.getTotalAmount());
            vatClientTrans.setGameInstanceId(soldTicket.getGameInstance().getId());
            vatClientTrans.setTicketSerialNo(soldTicket.getSerialNo());

        }
        this.getBaseJpaDao().insert(vatClientTrans);

        // update Vat balance...retailer has to pass those VAT amount to
        // government
        VatOperatorBalance operatorBalance = this.getVatOperatorBalanceDao().findByOperatorIdForUpdate(
                respCtx.getTransaction().getOperatorId());
        operatorBalance.setSaleBalance(operatorBalance.getSaleBalance().add(vatClientTrans.getVatTotalAmount()));
        this.getBaseJpaDao().update(operatorBalance);

        return vatClientTrans;
    }

    @Override
    public void refundVat(Context<?> respCtx, VatSaleTransaction vatTrans) throws ApplicationException {
        // look up VAT sale transaction first
        VatSaleTransaction hostVatTrans = this.getVatSaleTransactionDao().findByRefNo(vatTrans.getVatRefNo());
        if (hostVatTrans == null) {
            throw new ApplicationException(SystemException.CODE_NO_TRANSACTION,
                    "No VAT sale transaction found by refNo(" + vatTrans.getVatRefNo() + ").");
        }
        if (VatSaleTransaction.STATUS_VALID != hostVatTrans.getStatus()) {
            throw new ApplicationException(SystemException.CODE_4D_ENTRY_AMOUNT_ERROR,
                    "can Only refund VALID VAT sale transaction, however the status of vat transaction(refNo="
                            + vatTrans.getVatRefNo() + ") is " + hostVatTrans.getStatus());
        }
        // call cancelByTransaction to avoid duplicated code.
        Transaction targetTrans = this.getBaseJpaDao().findById(Transaction.class, hostVatTrans.getTransactionId());
        if (targetTrans == null) {
            throw new ApplicationException(SystemException.CODE_NO_TRANSACTION, "No transaction found by id("
                    + hostVatTrans.getTransactionId() + ").");
        }
        this.getTransactionService().reverseOrCancel(respCtx, targetTrans);
    }

    // --------------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------------

    /**
     * Buy a ticket based on supplied VAT. No matter B2B or B2C, both can join either Raffle or Magic100 game.
     * 
     * @param respCtx
     *            The context of VAT sale.
     * @param vat
     *            The associated VAT.
     * @param vat2Game
     *            The association between VAT and game. It determines which game the VAT can join.
     * @param vatSaleTransaction
     *            THe VAT sale transaction which represents the VAT sale.
     * @return THe RAFFLE or MAGIC100 ticket, it depends on which game VAT has joined, or null if no need to buy a
     *         ticket.
     */
    protected BaseTicket doSale(Context respCtx, VAT vat, Vat2Game vat2Game, VatSaleTransaction vatSaleTransaction)
            throws ApplicationException {
        BaseTicket soldTicket = null;
        Game game = this.getBaseJpaDao().findById(Game.class, vat2Game.getGameId());
        vat2Game.setGame(game);
        vatSaleTransaction.setGameType(game.getType());

        /**
         * We have to use game type+protocolVersion to locate the XML mapping file. as the response of vat sale may
         * return either Raffle or Magic100 ticket, that says the response maybe have to be serialized by different XML
         * mapping files(B2B raffle, or B2C magic100)
         */
        respCtx.getTransaction().setGameId(game.getId());
        respCtx.setGameTypeId(vatSaleTransaction.getGameType() + "");
        // set the version to locate ReversalOrCancelStrategy if got cancel
        // request
        respCtx.getTransaction().setVersion(vatSaleTransaction.getGameType() * -1);
        // set this protocol to diff from original raffle/magic100 sale response
        // mapping file.
        respCtx.setProtocalVersion(respCtx.getTransaction().getVersion() + "");

        // check the minimal threshold amount
        if (vatSaleTransaction.getVatTotalAmount().compareTo(vat2Game.getMinThresholdAmount()) < 0) {
            if (logger.isInfoEnabled()) {
                logger.info("THe vat amount(" + vatSaleTransaction.getVatTotalAmount()
                        + ") is under minial threshold amount of sale: " + vat2Game.getMinThresholdAmount());
            }
            return null;
        }

        if (GameType.RAFFLE.getType() == game.getType()) {
            soldTicket = this.sellRaffle(respCtx, vat, vat2Game, vatSaleTransaction);
        } else if (GameType.LUCKYNUMBER.getType() == game.getType()) {
            soldTicket = this.sellMagic100(respCtx, vat, vat2Game, vatSaleTransaction);
        } else {
            throw new ApplicationException(SystemException.CODE_UNSUPPORTED_GAME_TYPE,
                    "Unsupported game type in VAT business:" + game.getType());
        }

        if (soldTicket != null) {
            soldTicket.getGameInstance().setGameType(vatSaleTransaction.getGameType());
        }
        return soldTicket;
    }

    protected BaseTicket sellRaffle(Context respCtx, VAT vat, Vat2Game vat2Game, VatSaleTransaction vatSaleTransaction)
            throws ApplicationException {
        GameType gameType = GameType.RAFFLE;

        RaffleTicket raffleClientTicket = RaffleTicket.defaultTicket();
        // determine total amount first...no matter what is the VAT amount, only
        // a single raffle ticket will be sold.
        BigDecimal baseAmount = this.lookupOperationParameter(vat2Game.getGameId(), gameType).getBaseAmount();
        raffleClientTicket.setTotalAmount(baseAmount);
        // lookup active game instance
        raffleClientTicket.setGameInstance(this.getRaffleGameInstanceService()
                .enquirySaleReady(respCtx, vat2Game.getGameId()).get(0));
        this.getRaffleTicketService().sell(respCtx, raffleClientTicket);

        vatSaleTransaction.setVatRateTotalAmount(raffleClientTicket.getTotalAmount());
        return raffleClientTicket;
    }

    protected BaseTicket sellMagic100(Context respCtx, VAT vat, Vat2Game vat2Game, VatSaleTransaction vatSaleTrans)
            throws ApplicationException {
        GameType gameType = GameType.LUCKYNUMBER; // magic100
        Magic100Ticket magicClientTicket = Magic100Ticket.defaultTicket();

        // determine total amount
        BigDecimal baseAmount = this.lookupOperationParameter(vat2Game.getGameId(), gameType).getBaseAmount();
        int totalBets = this.determineB2CTotalBets(respCtx, vat, vatSaleTrans, baseAmount);
        if (totalBets == 0) {
            return null;
        }

        magicClientTicket.setTotalAmount(SimpleToolkit.mathMultiple(baseAmount, new BigDecimal(totalBets)));
        magicClientTicket.getEntries().get(0).setTotalBets(totalBets);

        // determine game instance
        magicClientTicket.setGameInstance(this.getMagic100GameInstanceService()
                .enquirySaleReady(respCtx, vat2Game.getGameId()).get(0));

        this.getMagic100TicketService().sell(respCtx, magicClientTicket);

        vatSaleTrans.setVatRateTotalAmount(SimpleToolkit.mathMultiple(vatSaleTrans.getVatTotalAmount(),
                vat2Game.getRate()));
        return magicClientTicket;
    }

    /**
     * Determine the total bets of B2C game(magic100). The general formula of calculating total bets is as below:
     * 
     * <pre>
     * double roughBets = (vat.totalAmount * vat.rate) / baseAmount
     * </pre>
     * 
     * For example, we have a case
     * 
     * <pre>
     * vat.totalAmount = $940
     * vat.rate = 10%
     * baseAmount = $100
     * </pre>
     * 
     * in this case, the roughBets is <code> (940 * 0.1) / 100 = 0.94</code>
     * 
     * <h1>If Round Up</h1> the total bets will be
     * 
     * <pre>
     * int(roughBets) + 1
     * </pre>
     * 
     * and the totalBets in our example will be <code> int(0.94) + 1 = 1 </code>
     * 
     * <h1>If Round Down</h1> the total bets will be
     * 
     * <pre>
     * int(roughBets)
     * </pre>
     * 
     * and the totalBets in our example will be <code> int(0.94)= 0 </code>
     * 
     * @param respCtx
     *            The vat sale context which represents the response
     * @param vat
     *            The VAT definition.
     * @param vatTrans
     *            The VAT sale transaction.
     * @param baseAmount
     *            The baseAmount of B2C game.
     * @return how many bets can be bought by player.
     * @throws ApplicationException
     *             if any business exception encountered.
     */
    protected int determineB2CTotalBets(Context respCtx, VAT vat, VatSaleTransaction vatTrans, BigDecimal baseAmount)
            throws ApplicationException {
        BigDecimal roughBets = vatTrans.getVatTotalAmount().multiply(vatTrans.getVatRate()).divide(baseAmount);

        int totalBets = 0;
        if (VAT.ROUND_UP == vat.getRoundUpDown()) {
            totalBets = ((int) roughBets.doubleValue()) + 1;
        } else {
            totalBets = (int) roughBets.doubleValue();
        }
        if (totalBets == 0) {
            logger.info("Vat( totalAmount=" + vatTrans.getVatTotalAmount() + ", rate=" + vatTrans.getVatRate()
                    + ",mode:" + (VAT.ROUND_UP == vat.getRoundUpDown() ? "RoundUp" : "RoundDown")
                    + ") for wager(roughBets=" + roughBets + ") isn't enough for a single ticket.");
        }
        return totalBets;
    }

    protected BaseOperationParameter lookupOperationParameter(String gameId, GameType gameType) {
        Game game = this.getBaseJpaDao().findById(Game.class, gameId, false);
        return this.getBaseJpaDao().findById(gameType.getOperationParametersType(), game.getOperatorParameterId(),
                false);
    }

    // --------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------------------------

    public VatDao getVatDao() {
        return vatDao;
    }

    public OperatorBizTypeDao getOperatorBizTypeDao() {
        return operatorBizTypeDao;
    }

    public void setOperatorBizTypeDao(OperatorBizTypeDao operatorBizTypeDao) {
        this.operatorBizTypeDao = operatorBizTypeDao;
    }

    public void setVatDao(VatDao vatDao) {
        this.vatDao = vatDao;
    }

    public Vat2MerchantDao getVat2MerchantDao() {
        return vat2MerchantDao;
    }

    public void setVat2MerchantDao(Vat2MerchantDao vat2MerchantDao) {
        this.vat2MerchantDao = vat2MerchantDao;
    }

    public Vat2GameDao getVat2GameDao() {
        return vat2GameDao;
    }

    public void setVat2GameDao(Vat2GameDao vat2GameDao) {
        this.vat2GameDao = vat2GameDao;
    }

    public TicketService getRaffleTicketService() {
        return raffleTicketService;
    }

    public void setRaffleTicketService(TicketService raffleTicketService) {
        this.raffleTicketService = raffleTicketService;
    }

    public TicketService getMagic100TicketService() {
        return magic100TicketService;
    }

    public void setMagic100TicketService(TicketService magic100TicketService) {
        this.magic100TicketService = magic100TicketService;
    }

    public BaseJpaDao getBaseJpaDao() {
        return baseJpaDao;
    }

    public void setBaseJpaDao(BaseJpaDao baseJpaDao) {
        this.baseJpaDao = baseJpaDao;
    }

    public GameInstanceService getRaffleGameInstanceService() {
        return raffleGameInstanceService;
    }

    public void setRaffleGameInstanceService(GameInstanceService raffleGameInstanceService) {
        this.raffleGameInstanceService = raffleGameInstanceService;
    }

    public GameInstanceService getMagic100GameInstanceService() {
        return magic100GameInstanceService;
    }

    public void setMagic100GameInstanceService(GameInstanceService magic100GameInstanceService) {
        this.magic100GameInstanceService = magic100GameInstanceService;
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public MerchantDao getMerchantDao() {
        return merchantDao;
    }

    public void setMerchantDao(MerchantDao merchantDao) {
        this.merchantDao = merchantDao;
    }

    public VatCompanyDao getVatCompanyDao() {
        return vatCompanyDao;
    }

    public void setVatCompanyDao(VatCompanyDao vatCompanyDao) {
        this.vatCompanyDao = vatCompanyDao;
    }

    public VatOperatorBalanceDao getVatOperatorBalanceDao() {
        return vatOperatorBalanceDao;
    }

    public void setVatOperatorBalanceDao(VatOperatorBalanceDao vatOperatorBalanceDao) {
        this.vatOperatorBalanceDao = vatOperatorBalanceDao;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public VatSaleTransactionDao getVatSaleTransactionDao() {
        return vatSaleTransactionDao;
    }

    public void setVatSaleTransactionDao(VatSaleTransactionDao vatSaleTransactionDao) {
        this.vatSaleTransactionDao = vatSaleTransactionDao;
    }

}
