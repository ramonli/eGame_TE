package com.mpos.lottery.te.gameimpl.magic100.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.magic100.sale.LuckyNumber;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Entry;
import com.mpos.lottery.te.gameimpl.magic100.sale.Magic100Ticket;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.service.TaxService;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseEntryDao;
import com.mpos.lottery.te.gamespec.sale.service.AbstractTicketService;
import com.mpos.lottery.te.merchant.service.balance.BalanceService;
import com.mpos.lottery.te.merchant.service.commission.CommissionBalanceService;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.trans.domain.TransactionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

/**
 * Make a sale of magic100 game. For magic100, only single-draw sale option is supported, and only single entry can be
 * carried in sale request. For example, below is a sale request:
 * 
 * <pre>
 * {@code
 * <Ticket PIN="!!!!" multipleDraws="1" totalAmount="300.0">
 *    <GameDraw gameId="LK-1" number="001"/>
 *    <Entry betOption="1" inputChannel="0" selectedNumber="PLAY" totalBets="3"/>
 * </Ticket>
 * }
 * </pre>
 * 
 * Only one single entry whose 'totalBets' is 3, in this case, the backend will generate 3 'BaseEntry' records instead.
 * That says the 'totalBets' in sale request is telling that how many entries should be generated at the backend, and
 * the total bets of each entry is simply 1.
 * 
 * @author Ramon
 */
public class Magic100SaleService extends AbstractTicketService {
    private Log logger = LogFactory.getLog(Magic100SaleService.class);
    // SPRING DEPENDENCIES
    private LuckyNumberService luckyNumberService;
    private BaseEntryDao baseEntryDao;
    private PayoutDao payoutDao;
    private TaxService taxService;
    @Resource(name = "payoutCommissionBalanceService")
    private CommissionBalanceService payoutCommissionBalanceService;
    @Resource(name = "defaultBalanceService")
    private BalanceService balanceService;

    @Override
    protected void doSuccessfulSale(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        // generate payout if wines a prize
        if (clientTicket.isWinning() && clientTicket.getGameInstance().getGame().isNeedAutoPayout()) {
            Payout payout = this.generatePayout(respCtx, clientTicket);

            // update operator's payout balance and calculate commission
            Context payoutCtx = (Context) respCtx.clone();
            payoutCtx.getTransaction().setTotalAmount(payout.getTotalAmount());
            payoutCtx.getTransaction().setGameId(clientTicket.getGameInstance().getGameId());
            payoutCtx.getTransaction().setType(TransactionType.PAYOUT.getRequestType());
            Object operatorOrMerchant = this.getBalanceService().balance(payoutCtx, payoutCtx.getTransaction(),
                    BalanceService.BALANCE_TYPE_PAYOUT, payoutCtx.getTransaction().getOperatorId(), true);
            // generate balance logs
            this.getPayoutCommissionBalanceService().calCommission(payoutCtx, operatorOrMerchant);
        }
    }

    @Override
    protected void customizeAssembleEntry(BaseTicket clientTicket, BaseEntry entry) {
        if (clientTicket.getGameInstance().getGame().isNeedAutoPayout()) {
            Magic100Entry mEntry = (Magic100Entry) entry;
            BigDecimal tmpTaxAmount = new BigDecimal("0");
            if (mEntry.isWinning() && Game.TAXMETHOD_PAYOUT == clientTicket.getGameInstance().getGame().getTaxMethod()) {

                // calculate tax based on prize level amount.
                tmpTaxAmount = this.getTaxService().tax(mEntry.getPrizeAmount(),
                        clientTicket.getGameInstance().getGame().getId());
                if (logger.isDebugEnabled()) {
                    logger.debug("the tax amount of entry(" + entry + ") is " + tmpTaxAmount);
                }
            }
            mEntry.setTaxAmount(tmpTaxAmount);
        }
    }

    @Override
    protected void customAssembleClientTicket(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        Magic100Ticket ticket = (Magic100Ticket) clientTicket;
        ticket.getGameInstance().setGameId(ticket.getGameInstance().getGame().getId());
        // assemble entries with lucky numbers
        List<LuckyNumber> luckyNumbers = this.getLuckyNumberService().determine(respCtx, ticket);
        // clear original entry(selected number is 'PLAY')
        ticket.getEntries().clear();
        for (LuckyNumber luckyNumber : luckyNumbers) {
            // generate entry
            Magic100Entry entry = new Magic100Entry();
            entry.setTicketSerialNo(clientTicket.getSerialNo());
            entry.setBetOption(BaseEntry.BETOPTION_SINGLE);
            entry.setSelectNumber(luckyNumber.getLuckyNumber());
            entry.setSequenceOfNumber(luckyNumber.getSequenceOfNumber());
            entry.setPrizeAmount(luckyNumber.getPrizeAmount());
            entry.setTaxAmount(luckyNumber.getTaxAmount());
            entry.setWinning(entry.getPrizeAmount().compareTo(new BigDecimal("0")) > 0);
            clientTicket.getEntries().add(entry);
            if (entry.isWinning()) {
                clientTicket.setWinning(true);
            }
        }
    }

    /**
     * Lookup Sale-ready game instances for sale.
     */
    @Override
    protected List<? extends BaseGameInstance> lookupSaleReadyGameInstance(Context<?> respCtx, BaseTicket clientTicket)
            throws ApplicationException {
        List<? extends BaseGameInstance> gameInstances = null;
        if (clientTicket.getGameInstance() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No game draw provided by client, the service side will randomly "
                        + "pick a active game instance of Magic100.");
            }
            gameInstances = this.getGameInstanceService().enquirySaleReady(respCtx);
        } else {
            // verify game instance
            gameInstances = this.getGameInstanceService().enquirySaleReady(respCtx,
                    clientTicket.getGameInstance().getGameId(), clientTicket.getGameInstance().getNumber(),
                    clientTicket.getMultipleDraws());
        }
        return gameInstances;
    }

    @Override
    protected void doSuccessfulCancel(Context<?> respCtx, BaseTicket soldTicket) throws ApplicationException {
        Magic100Ticket calcelTicket = (Magic100Ticket) soldTicket;
        // update cancel counter
        List<Magic100Entry> entries = this.getBaseEntryDao().findByTicketSerialNo(Magic100Entry.class,
                soldTicket.getSerialNo(), false);
        for (Magic100Entry entry : entries) {
            soldTicket.getEntries().add(entry);
        }
        this.getLuckyNumberService().cancel(respCtx, calcelTicket);
        // invalid payout
        if (soldTicket.isWinning() && soldTicket.getGameInstance().getGame().isNeedAutoPayout()) {
            List<Payout> payouts = this.getPayoutDao().getByTicketSerialNoAndStatus(soldTicket.getSerialNo(),
                    Payout.STATUS_PAID);
            BigDecimal actualPrizeAmount = new BigDecimal("0");
            for (Payout payout : payouts) {
                payout.setUpdateTime(respCtx.getTransaction().getUpdateTime());
                payout.setStatus(Payout.STATUS_REVERSED);
                this.getPayoutDao().update(payout);
                actualPrizeAmount = actualPrizeAmount.add(payout.getTotalAmount());
            }

            // reverse operator's payout balance and calculate commission
            Context payoutCtx = (Context) respCtx.clone();
            payoutCtx.getTransaction().setTotalAmount(actualPrizeAmount);
            payoutCtx.getTransaction().setGameId(soldTicket.getGameInstance().getGame().getId());
            Transaction targetTrans = (Transaction) payoutCtx.getTransaction().clone();
            targetTrans.setType(TransactionType.PAYOUT.getRequestType());
            Object operatorOrMerchant = this.getBalanceService().balance(payoutCtx, payoutCtx.getTransaction(),
                    BalanceService.BALANCE_TYPE_PAYOUT, payoutCtx.getTransaction().getOperatorId(), false);
            /**
             * Fix bug#7126 No need to call CommissionBalanceService().cancelCommission() here, as the
             * AbstractTiccketService will call it. the CommissionBalanceService().cancelCommission() will handle all
             * commission logs, includes both sale and payout, so we can't call it twice.
             */
            // // generate balance logs
            // this.getPayoutCommissionBalanceService().cancelCommission(payoutCtx,
            // targetTrans,
            // operatorOrMerchant);
        }
    }

    @Override
    public GameType supportedGameType() {
        return GameType.LUCKYNUMBER;
    }

    @Override
    protected void customizeAssembleTicket(BaseTicket generatedTicket, BaseTicket clientTicket) {
        if (generatedTicket.isWinning() && clientTicket.getGameInstance().getGame().isNeedAutoPayout()) {
            generatedTicket.setStatus(BaseTicket.STATUS_PAID);
        }
    }

    protected Payout generatePayout(Context<?> respCtx, BaseTicket clientTicket) throws ApplicationException {
        Payout payout = new Payout();
        payout.setGameInstanceId(clientTicket.getGameInstance().getId());
        payout.setGameId(clientTicket.getGameInstance().getGame().getId());
        payout.setId(this.getUuidService().getGeneralID());
        payout.setCreateTime(new Date());
        payout.setUpdateTime(payout.getCreateTime());
        payout.setTransaction(respCtx.getTransaction());
        payout.setTicketSerialNo(clientTicket.getSerialNo());
        payout.setType(Payout.TYPE_WINNING);
        payout.setValid(true);
        payout.setStatus(Payout.STATUS_PAID);
        BigDecimal[] amounts = this.calTax((Magic100Ticket) clientTicket);
        payout.setBeforeTaxTotalAmount(amounts[0]);
        payout.setTotalAmount(amounts[0].subtract(amounts[1]));
        payout.setInputChannel(Payout.INPUT_CHANNEL_MANUAL);
        payout.setOperatorId(respCtx.getTransaction().getOperatorId());
        payout.setDevId((int) respCtx.getTransaction().getDeviceId());
        payout.setMerchantId((int) respCtx.getTransaction().getMerchantId());

        this.getPayoutDao().insert(payout);
        return payout;
    }

    /**
     * Calculate the total prize amount and tax amount of a ticket.
     * 
     * @param clientTicket
     *            The magic100 ticket.
     * @return A array in which 1st is total prize amount and the 2nd is total tax amount.
     */
    protected BigDecimal[] calTax(Magic100Ticket clientTicket) throws ApplicationException {
        BigDecimal prizeAmount = new BigDecimal("0");
        BigDecimal taxAmount = new BigDecimal("0");
        for (BaseEntry e : clientTicket.getEntries()) {
            Magic100Entry entry = (Magic100Entry) e;
            prizeAmount = prizeAmount.add(entry.getPrizeAmount());
            taxAmount = taxAmount.add(entry.getTaxAmount());
        }
        return new BigDecimal[] { prizeAmount, taxAmount };
    }

    // --------------------------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------------------------

    public LuckyNumberService getLuckyNumberService() {
        return luckyNumberService;
    }

    public void setLuckyNumberService(LuckyNumberService luckyNumberService) {
        this.luckyNumberService = luckyNumberService;
    }

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public TaxService getTaxService() {
        return taxService;
    }

    public void setTaxService(TaxService taxService) {
        this.taxService = taxService;
    }

    public BaseEntryDao getBaseEntryDao() {
        return baseEntryDao;
    }

    public void setBaseEntryDao(BaseEntryDao baseEntryDao) {
        this.baseEntryDao = baseEntryDao;
    }

    public CommissionBalanceService getPayoutCommissionBalanceService() {
        return payoutCommissionBalanceService;
    }

    public void setPayoutCommissionBalanceService(CommissionBalanceService payoutCommissionBalanceService) {
        this.payoutCommissionBalanceService = payoutCommissionBalanceService;
    }

    public BalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

}
