package com.mpos.lottery.te.gameimpl.lotto.sale.support;

import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.gameimpl.lotto.draw.LottoOperationParameter;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.support.validator.AbstractSelectedNumberValidator;
import com.mpos.lottery.te.gamespec.sale.support.validator.DefaultTicketValidator;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;

public class LottoTicketValidator extends DefaultTicketValidator {

    /**
     * Now player can specify the betting amount of a entry, and TE may need to split a entry whose entry amount is
     * multiple times of base amount into multiple entries.
     */
    protected void calculateEntryAmount(Context respCtx, BaseEntry entry,
            AbstractSelectedNumberValidator selectedNumberValidator) throws ApplicationException {
        BigDecimal entryAmount = selectedNumberValidator.calEntryAmount(entry);
        if (entry.getEntryAmount() == null) {
            entry.setEntryAmount(entryAmount);
        } else {
            // check whether the client supplied entryAmount is integer multiple times of above
            // 'entryAmount'.
            if (entry.getEntryAmount().remainder(entryAmount).compareTo(new BigDecimal("0")) == 0) {
                int multipleCount = entry.getEntryAmount().divide(entryAmount).intValue();
                // check whether the multipleCount is greater than allowed setting
                int allowedMaxMultipleCount = ((LottoOperationParameter) selectedNumberValidator.getOperationParam())
                        .getMaxMultipleCount();
                if (multipleCount > allowedMaxMultipleCount || multipleCount < 1) {
                    throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "THe multiple count("
                            + multipleCount + ") of entry(" + entry.getSelectNumber()
                            + ") is either greater than max setting(" + allowedMaxMultipleCount + ") or less than 1");
                } else {
                    ((LottoEntry) entry).setMultipleCount(multipleCount);
                    entry.setTotalBets(entry.getTotalBets() * multipleCount);
                }
            } else {
                throw new ApplicationException(SystemException.CODE_UNMATCHED_SALEAMOUNT, "The entry amount("
                        + entry.getEntryAmount() + ") of entry(" + entry.getSelectNumber()
                        + ") isn't integer multiple times of base amount("
                        + selectedNumberValidator.getOperationParam().getBaseAmount());
            }
        }
    }
}
