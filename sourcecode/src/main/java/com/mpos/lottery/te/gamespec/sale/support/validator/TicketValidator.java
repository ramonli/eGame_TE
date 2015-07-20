package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

/**
 * Validate whether a ticket request is legal. Every game type must provide a dedicated implementation.
 * 
 * @author Ramon
 * 
 */
public interface TicketValidator {
    /**
     * Validate ticket according to the fundamental type(operation parameter, racing...).
     * 
     * @param clientTicket
     *            The ticket will be validated.
     * @param game
     *            The game associated with ticket.
     * @param respCtx
     *            The transaction context.
     * @throws ApplicationException
     *             when encounter any business exception.
     */
    void validate(Context respCtx, BaseTicket clientTicket, Game game) throws ApplicationException;
}
