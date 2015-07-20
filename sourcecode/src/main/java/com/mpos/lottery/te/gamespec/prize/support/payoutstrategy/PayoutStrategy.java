package com.mpos.lottery.te.gamespec.prize.support.payoutstrategy;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.GameType;
import com.mpos.lottery.te.gamespec.prize.web.PrizeDto;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;

import java.util.List;

/**
 * <code>PayoutStrategy</code> for 2 payout strategy:
 * <ol>
 * <li>print new ticket</li>
 * <li>return</li>
 * </ol>
 * In general, you should implement this interface directly, the better choice is inheriting from
 * <code>AbstractPayoutStrategy</code>.
 * 
 * @author Ramon
 * 
 */
public interface PayoutStrategy {

    /**
     * Perform the real payout. This method will update ticket and generate payout log according to the payout mode.
     * 
     * @param respCtx
     *            The context of transaction.
     * @param prize
     *            The detailed prize information.
     * @param hostTickets
     *            The host tickets.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    void payout(Context<?> respCtx, GameType supportedGameType, PrizeDto prize, List<? extends BaseTicket> hostTickets)
            throws ApplicationException;

    void confirm(Context respCtx, GameType supportedGameType, List<? extends BaseTicket> hostTickets)
            throws ApplicationException;

    void reverse(Context respCtx, GameType supportedGameType, List<? extends BaseTicket> hostTickets,
            Transaction targetTrans) throws ApplicationException;
}
