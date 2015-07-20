package com.mpos.lottery.te.gamespec.sale.support.validator;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;

public interface SelectedNumberValidator {

    /**
     * Validate the format of selected number:
     * <ul>
     * <li>The number can not be duplicated.</li>
     * <li>The minimal number can not be less than 1</li>
     * <li>The maximal number can not bet greater than N.</li>
     * <li>The verification associates to specified bet option</li>
     * </ul>
     * Other constraints will be checked in concrete implementation class.
     */
    void validate(Context respCtx, BaseTicket ticket, Game game, BaseEntry entry) throws ApplicationException;

    /**
     * Get the total bets of a selected number.
     */
    long calTotalBets(BaseEntry entry) throws ApplicationException;

    /**
     * Calculate the entry amount.
     */
    BigDecimal calEntryAmount(BaseEntry entry) throws ApplicationException;

}
