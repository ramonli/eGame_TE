package com.mpos.lottery.te.gamespec.sale.service;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.BaseGameInstance;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.util.List;

public interface RiskControlService {

    /**
     * The basic idea of risk control is to check whether the total prize amount of a given selected number will exceed
     * the ability to pay of operation company. That says once the given selected number wins, and many people buy that
     * selected number, operation company may have to pay too much prize amount and fall in loss situation.
     * <p>
     * <b>The prize amount of a given selected number means the max prize amount a selected number may win, in general
     * it is the 1st prize. </b>
     * <p>
     * System should support 2 kinds of risk control methods:
     * 
     * <h2>1. Maximum LOSS Method</h2>
     * <p>
     * Operation company will set a limit, once the prize amount of a given selected number exceeds this limit, the sale
     * request will be rejected.
     * <p>
     * For example, the bet amount of a selected number(single bet option) of '1,2,3,4,5' is 50, its odd is 40 times,
     * and the limit is 2060.
     * <ol>
     * <li>A sale of this selected number comes, the bet amount is 1, the prize amount will be 51*40=2040, it is ok, the
     * sale will be accepted.</li>
     * <li>One more sale of this selected number comes, the bet amount is 1 too, the prize amount will be 52*40=2080
     * which is greater than the limit 2060, this sale will be rejected.</li>
     * </ol>
     * 
     * <h2>2. Dynamic Method</h2>
     * <p>
     * Operation company will set a limit of allowed prize amount of a single selected number ,same with the 1st method
     * 'Maximum LOSS Method', and also will set a percentage of turnover. The final limit will be
     * <b>max(preconfigured_limit, turnover*percentage)</b>, once the final limit is confirmed, the possible max prize
     * amount of a given selected number will be compared with this limit, just like the 1st method.
     * <p>
     * Lets say the pre-configured prize limit is 2060, the turnover of current game instance is 5000, the percentage is
     * 42%,(5000*0.42=2100), the bet amount of a given selected number is 50 now, and the odd is 40 times.
     * <ol>
     * <li>New sale comes, its bet amount is 2, the limit of turnover percentage is (5000+2)*0.42=2100.84 which is
     * greater than the pre-configured limit 2060, so the final limit will be 2100.84. The possible prize amount is
     * (50+2)*40=2080, it is less than the final limit, sale is accepted.</li>
     * <li>New sale comes, its bet amount is 10, the limit of turnover percentage is (5000+2+10)*0.42=2105.04 which is
     * greater than the pre-configured limit 2060, so the final limit will be 2105.04. The possible prize amount is
     * (50+2+10)*40=2480, it is greater than the final limit, sale is rejected.
     * <p>
     * Note that in this step the bet amount 10 is the amount of ticket, not a entry.</li>
     * </ol>
     * 
     * @param respCtx
     *            The context of current transaction.
     * @param ticket
     *            The sale instance.
     * @param gameInstances
     *            The game instances associate with the sold ticket, maybe multiple-draw ticket.
     * @throws ApplicationException
     *             when fail to pass risk control checking.
     */
    void riskControl(Context respCtx, BaseTicket ticket, List<? extends BaseGameInstance> gameInstances)
            throws ApplicationException;

    /**
     * If client request to cancel the sale, the risk statistics(
     * {@link com.mpos.lottery.te.gamespec.sale.RiskControlLog}) must be rolled back as well.
     * 
     * @param respCtx
     *            The context of current transaction
     * @param hostTickets
     *            The list of cancelled tickets.
     * @throws ApplicationException
     *             if any exception
     */
    void cancelRiskControl(Context respCtx, List<? extends BaseTicket> hostTickets) throws ApplicationException;
}
