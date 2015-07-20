package com.mpos.lottery.te.gameimpl.lotto.prize.domain.logic;

import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gamespec.prize.support.payoutstrategy.NewPrintPayoutStrategy;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.port.Context;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class LottoNewPrintPayoutStrategy extends NewPrintPayoutStrategy {

    /**
     * As Lotto let player specify entry amount(times betting), in which case a single entry in request will be split
     * into multiple entries in database. When print new ticket, we have to merge those entries.
     */
    protected void customizePrintedPhysicalTicket(Context<?> respCtx, BaseTicket newPrintTicket) {
        List<BaseEntry> entries = newPrintTicket.getEntries();
        List<BaseEntry> mergedEntries = new LinkedList<BaseEntry>();
        for (BaseEntry entry : entries) {
            LottoEntry lottoEntry = (LottoEntry) entry;
            if (lottoEntry.getMultipleCount() > 0) {
                // if use 'entry' which is a JPA managed entity, there is risk that JPA context will commit the change
                // into underlying database even we don't mean that.
                LottoEntry userEntry = (LottoEntry) lottoEntry.clone();
                userEntry.setEntryAmount(SimpleToolkit.mathMultiple(userEntry.getEntryAmount(), new BigDecimal(
                        userEntry.getMultipleCount())));
                mergedEntries.add(userEntry);
            }
        }
        newPrintTicket.setEntries(mergedEntries);
    }
}
