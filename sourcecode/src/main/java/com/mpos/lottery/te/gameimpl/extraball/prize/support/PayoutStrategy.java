package com.mpos.lottery.te.gameimpl.extraball.prize.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.extraball.prize.web.Prize;
import com.mpos.lottery.te.port.Context;

public interface PayoutStrategy {

    /**
     * Perform the real payout. This method will update ticket and generate payout log accorindg to the payout mode.
     * 
     * @param respCtx
     *            The context of transaction.
     * @param prize
     *            The detailed prize information.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    void payout(Context<?> respCtx, Prize prize) throws ApplicationException;

    /**
     * The key of <code>PayoutStrategy</code>, it must follow the format: {gameType}-{payoutmode}. If a strategy
     * implementation supports all game type, simply mark game type as Game#TYPE_UNDEF
     */
    String key();
}
