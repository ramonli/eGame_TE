package com.mpos.lottery.te.gamespec.sale.support.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoEntry;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;

import org.junit.Test;

import java.util.Arrays;

public class SelectedNumberUnitTest {

    @Test
    public void test_1() {
        BaseEntry entry = mockEntry();
        SelectedNumber sn = new SelectedNumber(entry);
        assertEquals(6, sn.getAllNumbers().length);
        assertEquals(sn.getSelectedNumber(), sn.getBaseNumber());
        assertNull(sn.getSpecialNumber());
        assertEquals(6, sn.getBaseNumbers().length);
        assertEquals(0, sn.getSpecialNumbers().length);
        assertEquals("[2, 3, 11, 14, 15, 20]", Arrays.toString(sn.getBaseNumbers()));
        assertEquals("[]", Arrays.toString(sn.getSpecialNumbers()));
        assertEquals("11,20,3,14,15,2", entry.getSelectNumber());
    }

    @Test
    public void test_2() {
        BaseEntry entry = mockEntry();
        entry.setSelectNumber("11,20,3,14,15,2-15,9");
        SelectedNumber sn = new SelectedNumber(entry);
        assertEquals(8, sn.getAllNumbers().length);
        assertEquals("11,20,3,14,15,2", sn.getBaseNumber());
        assertEquals("15,9", sn.getSpecialNumber());
        assertEquals(6, sn.getBaseNumbers().length);
        assertEquals(2, sn.getSpecialNumbers().length);
        assertEquals("[2, 3, 11, 14, 15, 20]", Arrays.toString(sn.getBaseNumbers()));
        assertEquals("[9, 15]", Arrays.toString(sn.getSpecialNumbers()));
    }

    @Test
    public void test_3() {
        LottoEntry entry = new LottoEntry();
        entry.setSelectNumber("011,20,03,14,15,02-15,09");
        SelectedNumber sn = new SelectedNumber(entry);
        assertEquals(8, sn.getAllNumbers().length);
        assertEquals("011,20,03,14,15,02", sn.getBaseNumber());
        assertEquals("15,09", sn.getSpecialNumber());
        assertEquals(6, sn.getBaseNumbers().length);
        assertEquals(2, sn.getSpecialNumbers().length);
        assertEquals("[2, 3, 11, 14, 15, 20]", Arrays.toString(sn.getBaseNumbers()));
        assertEquals("[9, 15]", Arrays.toString(sn.getSpecialNumbers()));
        assertEquals("11,20,3,14,15,2-15,9", entry.getSelectNumber());
    }

    public static BaseEntry mockEntry() {
        LottoEntry entry = new LottoEntry();
        entry.setSelectNumber("11,20,03,14,15,02");
        return entry;
    }
}
