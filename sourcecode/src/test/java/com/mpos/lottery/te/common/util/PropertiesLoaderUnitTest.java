package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.MLotteryContext;

import org.junit.Test;

import java.math.BigDecimal;

public class PropertiesLoaderUnitTest {

    @Test
    public void testGetInstance() throws Exception {
        MLotteryContext p1 = MLotteryContext.getInstance();
        MLotteryContext p2 = MLotteryContext.getInstance();
        MLotteryContext p3 = MLotteryContext.getInstance();

        assertEquals(p1, p2);
        assertEquals(p2, p3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInt() throws Exception {
        MLotteryContext p = MLotteryContext.getInstance();
        int redoTimes = p.getInt("redo.times");
        assertEquals(3, redoTimes);

        int i = p.getInt("redo.times.no");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDouble() throws Exception {
        MLotteryContext p = MLotteryContext.getInstance();
        double version = p.getInt("protocal.version");
        assertEquals(1.0, version, 0);

        p.getDouble("redo.times.no");
    }

    @Test
    public void testGet() throws Exception {
        MLotteryContext p = MLotteryContext.getInstance();
        String df = p.get("dataformat.trace_message_id");
        assertEquals("\\d{9}", df);

        BigDecimal value = new BigDecimal("10").divide(new BigDecimal("3"), 1, BigDecimal.ROUND_HALF_DOWN);
        System.out.println(value);
    }

}
