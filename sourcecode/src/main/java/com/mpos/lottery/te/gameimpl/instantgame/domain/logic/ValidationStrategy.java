package com.mpos.lottery.te.gameimpl.instantgame.domain.logic;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gameimpl.instantgame.domain.InstantTicket;
import com.mpos.lottery.te.gameimpl.instantgame.domain.dto.PrizeLevelDto;

public interface ValidationStrategy {

    /**
     * Confirm if a instant ticket win a prize. Different implementation can support different algorithm.
     * 
     * @param ticket
     *            The instant ticket retrieved from the backend.
     * @param virn
     *            The virn of ticket.
     * @param isEnquiry
     *            Whether this transaction is enquriy(read-only)?
     * @return a InstantPayoutDto instance if win a prize, or null will be returned.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    PrizeLevelDto validate(InstantTicket ticket, String virn, boolean isEnquiry) throws ApplicationException;

    /**
     * Confirm if a instant ticket win a prize temporarily. Different implementation can support different algorithm.
     * 
     * @param ticket
     *            The instant ticket retrieved from the backend.
     * @param virn
     *            The virn of ticket.
     * @param isEnquiry
     *            Whether this transaction is enquriy(read-only)?
     * @return a InstantPayoutDto instance if win a prize, or null will be returned.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    // PrizeLevelDto tempValidate(InstantTicket ticket, String virn, boolean isEnquiry) throws
    // ApplicationException;
}
