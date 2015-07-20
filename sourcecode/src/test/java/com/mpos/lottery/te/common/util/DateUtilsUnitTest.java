package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Date;

public class DateUtilsUnitTest {

    @Test
    public void testGetBeginAndEndOfDay() {
        Date anytime = SimpleToolkit.parseDate("20140915093012", DateUtils.DEFAULT_TIMEFORMAT);
        Date[] result = DateUtils.getBeginAndEndOfDay(anytime);
        assertEquals("20140915000000", SimpleToolkit.formatDate(result[0], DateUtils.DEFAULT_TIMEFORMAT));
        assertEquals("20140916000000", SimpleToolkit.formatDate(result[1], DateUtils.DEFAULT_TIMEFORMAT));

        anytime = SimpleToolkit.parseDate("20140915193012", DateUtils.DEFAULT_TIMEFORMAT);
        result = DateUtils.getBeginAndEndOfDay(anytime);
        assertEquals("20140915000000", SimpleToolkit.formatDate(result[0], DateUtils.DEFAULT_TIMEFORMAT));
        assertEquals("20140916000000", SimpleToolkit.formatDate(result[1], DateUtils.DEFAULT_TIMEFORMAT));

        anytime = SimpleToolkit.parseDate("20140915000000", DateUtils.DEFAULT_TIMEFORMAT);
        result = DateUtils.getBeginAndEndOfDay(anytime);
        assertEquals("20140915000000", SimpleToolkit.formatDate(result[0], DateUtils.DEFAULT_TIMEFORMAT));
        assertEquals("20140916000000", SimpleToolkit.formatDate(result[1], DateUtils.DEFAULT_TIMEFORMAT));

        anytime = SimpleToolkit.parseDate("20140915235959", DateUtils.DEFAULT_TIMEFORMAT);
        result = DateUtils.getBeginAndEndOfDay(anytime);
        assertEquals("20140915000000", SimpleToolkit.formatDate(result[0], DateUtils.DEFAULT_TIMEFORMAT));
        assertEquals("20140916000000", SimpleToolkit.formatDate(result[1], DateUtils.DEFAULT_TIMEFORMAT));
    }

}
