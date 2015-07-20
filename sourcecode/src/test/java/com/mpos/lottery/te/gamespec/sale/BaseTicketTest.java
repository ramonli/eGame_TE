package com.mpos.lottery.te.gamespec.sale;

import static org.junit.Assert.*;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class BaseTicketTest {

    @Test
    public void testGenerateExtendText() throws Exception {
        List<BaseEntry> entries = new LinkedList<BaseEntry>();
        for (int i = 0; i < 4; i++) {
            LottoEntry entry = new LottoEntry();
            entry.setSelectNumber("1,2,3,6,7,13");
            entries.add(entry);
        }
        for (int i = 0; i < 3; i++) {
            LottoEntry entry = new LottoEntry();
            entry.setSelectNumber("3,11,14,16,22,25,36");
            entries.add(entry);
        }
        String extendTxt = BaseTicket.generateExtendText(entries);
        System.out.println(extendTxt);
        assertEquals("e21bd03ffb7b0507d5e4e35b9b336871", extendTxt);
    }

}
