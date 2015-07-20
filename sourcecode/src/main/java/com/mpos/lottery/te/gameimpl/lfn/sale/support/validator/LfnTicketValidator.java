package com.mpos.lottery.te.gameimpl.lfn.sale.support.validator;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gamespec.game.Game;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.gamespec.sale.support.validator.DefaultTicketValidator;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;

public class LfnTicketValidator extends DefaultTicketValidator {

    /**
     * whether the total amount is matched with sum of entry amount
     */
    @Override
    protected void validateAmount(Context respCtx, BaseTicket clientTicket, Game game) throws ApplicationException {
        BigDecimal expectedTotalAmount = clientTicket.getTotalAmount();
        BigDecimal actualAmount = new BigDecimal("0");
        for (BaseEntry entry : clientTicket.getEntries()) {
            actualAmount = actualAmount.add(entry.getEntryAmount());
        }
        // no base amount
        actualAmount = actualAmount.multiply(new BigDecimal(clientTicket.getMultipleDraws()));

        if (expectedTotalAmount.compareTo(actualAmount) != 0) {
            throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "The total amount("
                    + clientTicket.getTotalAmount() + ") of client ticket is "
                    + "unmatched with the sum of entry amount(" + actualAmount + ")");
        }
    }
}
