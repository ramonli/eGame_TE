package com.mpos.lottery.te.gameimpl.extraball.prize.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.PrizeItem;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDao;
import com.mpos.lottery.te.gamespec.prize.dao.PayoutDetailDao;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.dao.BaseTicketDao;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.sequence.service.UUIDService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

public abstract class AbstractPayoutStrategy implements PayoutStrategy, InitializingBean {
    protected Log logger = LogFactory.getLog(AbstractPayoutStrategy.class);
    // Spring dependencies
    private PayoutStrategyFactory payoutStrategyFactory;
    private BaseTicketDao baseTicketDao;
    protected PayoutDao payoutDao;
    protected PayoutDetailDao payoutDetailDao;
    protected UUIDService uuidService;

    @Override
    public final void afterPropertiesSet() throws Exception {
        this.getPayoutStrategyFactory().register(this.key(), this);
    }

    @Override
    public void payout(Context<?> respCtx, Prize prize) throws ApplicationException {
        // update ticket status
        this.updateTicket(prize);
        // gerneate new tickets if needed.
        this.generateTicket(respCtx, prize);
        // genrate payout entities
        this.generatePayout(respCtx, prize);
    }

    // --------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------

    /**
     * Generate payout entities.
     */
    private void generatePayout(Context<?> respCtx, Prize prize) throws ApplicationException {
        Iterator<PrizeItem> iterator = prize.iterator();
        while (iterator.hasNext()) {
            PrizeItem prizeItem = iterator.next();
            if (prizeItem.getPrizeAmount().compareTo(new BigDecimal("0")) <= 0) {
                continue;
            }

            Payout payout = new Payout();
            payout.setGameInstanceId(prizeItem.getGameInstance().getId());
            payout.setId(this.getUuidService().getGeneralID());
            payout.setCreateTime(new Date());
            payout.setUpdateTime(payout.getCreateTime());
            payout.setTransaction(respCtx.getTransaction());
            payout.setTicketSerialNo(prize.getWinningTicket().getSerialNo());
            payout.setTotalAmount(prizeItem.getActualAmount());
            payout.setBeforeTaxTotalAmount(prizeItem.getPrizeAmount());
            payout.setType(Payout.TYPE_WINNING);
            payout.setValid(true);
            payout.setStatus(Payout.STATUS_PAID);
            payout.setOperatorId(respCtx.getTransaction().getOperatorId());
            payout.setDevId((int) respCtx.getTransaction().getDeviceId());
            payout.setMerchantId((int) respCtx.getTransaction().getMerchantId());
            payout.setGameId(prizeItem.getGameInstance().getGame().getId());
            // assemble payout entity in customized way
            this.assemblePayout(respCtx, prize, prizeItem, payout);
            // generate payout details
            this.doGeneratePayoutDetails(respCtx, prize, prizeItem, payout);

            this.getPayoutDao().insert(payout);
        }
        if (prize.getFutureTickets().size() > 0) {
            this.handleFutureTickets(respCtx, prize);
        }
    }

    /**
     * Handle the future tickets.
     */
    protected void handleFutureTickets(Context<?> respCtx, Prize prize) throws ApplicationException {
        // Template method for subclass
    }

    /**
     * Subimplementation can override this method to assemble <code>Payout</code> entity in its own requirement.
     * 
     * @param respCtx
     *            The context of transaction.
     * @param prize
     *            The prize information.
     * @param prizeItem
     *            The prize item of given game instance.
     * @param payout
     *            The generated payout base on current prize item.
     */
    protected void assemblePayout(Context<?> respCtx, Prize prize, PrizeItem prizeItem, Payout payout)
            throws ApplicationException {
        // Template method for subclass
    }

    /**
     * Subimplementation can override this method to generate payout details.
     * 
     * @param respCtx
     *            The context of transaction.
     * @param prize
     *            The prize information.
     * @param prizeItem
     *            The prize item of given game instance.
     * @param payout
     *            The generated payout base on current prize item.
     */
    protected void doGeneratePayoutDetails(Context<?> respCtx, Prize prize, PrizeItem prizeItem, Payout payout)
            throws ApplicationException {
        // Template method for subclass
    }

    /**
     * Generate new tickets for future if needed.
     */
    protected void generateTicket(Context<?> respCtx, Prize prize) throws ApplicationException {
        // Template method for sub-implementation
    }

    /**
     * Update the ticket status.
     */
    protected final void updateTicket(Prize prize) throws ApplicationException {
        Date now = new Date();
        for (BaseTicket ticket : prize.getPaidTickets()) {
            ticket.setStatus(BaseTicket.STATUS_PAID);
            ticket.setUpdateTime(now);
            this.getBaseTicketDao().update(ticket);
        }
        this.doUpdateTicket(prize);
    }

    protected void doUpdateTicket(Prize prize) throws ApplicationException {
        // Template method for sub-implementation
    }

    // --------------------------------------------------
    // SPRING DEPENDENCIES INJECTION
    // --------------------------------------------------

    public PayoutDao getPayoutDao() {
        return payoutDao;
    }

    public void setPayoutDao(PayoutDao payoutDao) {
        this.payoutDao = payoutDao;
    }

    public UUIDService getUuidService() {
        return uuidService;
    }

    public void setUuidService(UUIDService uuidService) {
        this.uuidService = uuidService;
    }

    public PayoutDetailDao getPayoutDetailDao() {
        return payoutDetailDao;
    }

    public void setPayoutDetailDao(PayoutDetailDao payoutDetailDao) {
        this.payoutDetailDao = payoutDetailDao;
    }

    public PayoutStrategyFactory getPayoutStrategyFactory() {
        return payoutStrategyFactory;
    }

    public void setPayoutStrategyFactory(PayoutStrategyFactory payoutStrategyFactory) {
        this.payoutStrategyFactory = payoutStrategyFactory;
    }

    public BaseTicketDao getBaseTicketDao() {
        return baseTicketDao;
    }

    public void setBaseTicketDao(BaseTicketDao baseTicketDao) {
        this.baseTicketDao = baseTicketDao;
    }

}
